package ca.neitsch.grubbyjar;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

public class GrubbyjarPluginIntegTest {
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();
    @Rule
    public TemporaryFolder _folder2 = new TemporaryFolder();

    @Rule
    public ExpectedException _thrown = ExpectedException.none();

    private File _gradleBuildFile;

    private static final String HELLO_CONCURRENT_RB = textFromLines(
            "require 'concurrent'",
            "x = Concurrent::Event.new",
            "x.set",
            "puts x",
            "puts x.set",
            "raise 'Version Mismatch' unless Concurrent::VERSION == '1.0.5'");

    @Before
    public void createBuildFileObject()
    throws IOException
    {
        _gradleBuildFile = _folder.newFile("build.gradle");
    }

    @Test
    public void testHelloWorld()
    throws Exception
    {
        Util.writeTextToFile(TestUtil.readResource("hello-world-script.gradle"),
                _gradleBuildFile);
        Util.writeTextToFile("puts 'hello world'.upcase",
                _folder.newFile("hello.rb"));

        runGradle();

        assertThat(runJar(), containsString("HELLO WORLD"));
    }

    @Test
    public void testAccessingUnbundledSystemGemShouldFail()
    throws Exception
    {
        new SystemGem("concurrent-ruby", "concurrent").ensureInstalled();

        Util.writeTextToFile(TestUtil.readResource("hello-world-script.gradle"),
                _gradleBuildFile);

        textFile("hello.rb", HELLO_CONCURRENT_RB);

        runGradle();

        _thrown.expect(InvalidExitValueException.class);

        runJar();
    }

    @Test
    public void testAccessBundledGemShouldSucceed() throws Exception {
        Util.writeTextToFile(TestUtil.readResource("hello-world-script.gradle"), _gradleBuildFile
        );

        Util.writeTextToFile(TestUtil.readResource("concurrent-ruby.Gemfile"), _folder.newFile("Gemfile")
        );

        Util.writeTextToFile(TestUtil.readResource("concurrent-ruby.Gemfile.lock"), _folder.newFile("Gemfile.lock")
        );

        textFile("hello.rb", HELLO_CONCURRENT_RB);

        runGradle();

        String output = runJar();
        assertThat(output, containsString("#<Concurrent::Event"));
        assertThat(output, endsWith("\ntrue\n"));
    }

    @Test
    public void testGemspecHelloWorld() throws Exception {
        copyResourcesToDirectory("gemspec1", _folder.getRoot(),
                "Gemfile",
                "Gemfile.lock",
                "bin/gemspec1",
                "build.gradle",
                "gemspec1.gemspec",
                "lib/gemspec1.rb");

        runGradle();

        String output = runJar();
        assertThat(output, containsString("#<Concurrent::Event"));
    }

    @Test
    public void testJardepHelloWorld() throws Exception {
        copyResourcesToDirectory("jardep1", _folder.getRoot(),
                ".ruby-version",
                "Gemfile",
                "Gemfile.lock",
                "bin/jardep1",
                "build.gradle",
                "jardep1.gemspec",
                "lib/jardep1.rb",
                "settings.gradle");

        runGradle("grubbyjarRequire");

        String output = runCommandInBuildDir("ruby", "-G", "bin/jardep1");
        assertThat(output, containsString("#<Concurrent::Event"));
        assertThat(output, containsString("hellohellohello"));
    }

    @Test
    public void testUsingGemThatIncludesJar()
    throws Exception
    {
        copyResourcesToDirectory("b64wrapper", _folder2.getRoot(),
                "b64wrapper.gemspec",
                "build.gradle",
                "lib/b64wrapper.rb",
                "Gemfile",
                "Gemfile.lock"
        );

        runGradle(_folder2, "dep");

        Path b64wrapperPath = Paths.get(_folder2.getRoot().toString());
        Path jardep2Path = Paths.get(_folder.getRoot().toString());
        String relativePath = jardep2Path.relativize(b64wrapperPath).toString();

        copyResourcesToDirectory("jardep2", _folder.getRoot(),
                ImmutableMap.of("REPLACE_ME", relativePath),
                ".ruby-version",
                "Gemfile",
                "Gemfile.lock",
                "build.gradle",
                "hello.rb");

        runGradle();

        String output = runJar();
        assertThat(output, containsString("hello base64 gem"));
    }

    void copyResourcesToDirectory(String resourcePrefix, File target, String... fileNames) {
        copyResourcesToDirectory(resourcePrefix, target, Collections.emptyMap(), fileNames);
    }

    void copyResourcesToDirectory(String resourcePrefix, File target,
                                  Map<String, String> replacements, String... fileNames)
    {
        for (String fileName: fileNames) {
            File outputFile = new File(target, fileName);
            if (!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdir();
            String contents = TestUtil.readResource(resourcePrefix + "/" + fileName);
            for (Map.Entry<String, String> e: replacements.entrySet()) {
                contents = contents.replace(e.getKey(), e.getValue());
            }
            Util.writeTextToFile(contents, outputFile);
        }
    }

    BuildResult runGradle(String... arguments) {
        return runGradle(_folder, arguments);
    }

    /**
     * Run gradle, using "grubbyjar" as the task if none is specified
     */
    BuildResult runGradle(TemporaryFolder folder, String... arguments) {
        List<String> argumentList = Lists.newArrayList();
        argumentList.add("--stacktrace");

        if (arguments.length == 0) {
            argumentList.add("grubbyjar");
        } else {
            argumentList.addAll(Arrays.asList(arguments));
        }

        return GradleRunner.create()
                .withProjectDir(folder.getRoot())
                .withPluginClasspath()
                .withArguments(argumentList.toArray(new String[0]))
                .withDebug(debugEnabled())
                .forwardOutput().build();
    }

    private boolean debugEnabled() {
        // https://stackoverflow.com/a/6865049
        return ManagementFactory.getRuntimeMXBean()
                .getInputArguments().toString().contains("-agentlib:jdwp");
    }

    private void textFile(String name, String... lines)
    throws IOException
    {
        Util.writeTextToFile(textFromLines(lines), _folder.newFile(name));
    }

    private static String textFromLines(String... lines) {
        return Joiner.on("\n").join(lines) + "\n";
    }

    String runJar()
    throws IOException, InterruptedException, TimeoutException
    {
        return runCommandInBuildDir("java", "-jar",
                "build/libs/" + _folder.getRoot().getName() + ".jar");
    }

    String runCommandInBuildDir(String... command)
    throws IOException, InterruptedException, TimeoutException
    {
        return new ProcessExecutor()
                .command(command)
                .directory(_folder.getRoot())
                .readOutput(true)
                .exitValueNormal()
                .execute()
                .outputUTF8();
    }
}
