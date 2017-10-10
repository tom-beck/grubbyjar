package ca.neitsch.grubyjar;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;

public class GrubyjarPluginIntegTest {
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

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
        TestUtil.writeTextToFile(_gradleBuildFile,
                TestUtil.readResource("hello-world-script.gradle"));
        TestUtil.writeTextToFile(_folder.newFile("hello.rb"),
                "puts 'hello world'.upcase");

        runGradle();

        assertThat(runJar(), containsString("HELLO WORLD"));
    }

    @Test
    public void testAccessingUnbundledSystemGemShouldFail()
    throws Exception
    {
        new SystemGem("concurrent-ruby", "concurrent").ensureInstalled();

        TestUtil.writeTextToFile(_gradleBuildFile,
                TestUtil.readResource("hello-world-script.gradle"));

        textFile("hello.rb", HELLO_CONCURRENT_RB);

        runGradle();

        _thrown.expect(InvalidExitValueException.class);

        runJar();
    }

    @Test
    public void testAccessBundledGemShouldSucceed() throws Exception {
        TestUtil.writeTextToFile(_gradleBuildFile,
                TestUtil.readResource("hello-world-script.gradle"));

        TestUtil.writeTextToFile(_folder.newFile("Gemfile"),
                TestUtil.readResource("concurrent-ruby.Gemfile"));

        TestUtil.writeTextToFile(_folder.newFile("Gemfile.lock"),
                TestUtil.readResource("concurrent-ruby.Gemfile.lock"));

        textFile("hello.rb", HELLO_CONCURRENT_RB);

        runGradle();

        String output = runJar();
        assertThat(output, containsString("#<Concurrent::Event"));
        assertThat(output, endsWith("\ntrue\n"));
    }

    @Test
    public void testGemspecHelloWorld() throws Exception {
        for (String fileName: new String[] {
                "Gemfile",
                "Gemfile.lock",
                "bin/gemspec1",
                "build.gradle",
                "gemspec1.gemspec",
                "lib/gemspec1.rb",
                "lib/don’t-put-in-gem"
        }) {
            File outputFile = new File(_folder.getRoot(), fileName);
            if (!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdir();
            TestUtil.writeTextToFile(outputFile,
                    TestUtil.readResource("gemspec1/" + fileName));
        }

        runGradle();

        String output = runJar();
        assertThat(output, containsString("#<Concurrent::Event"));
    }

    BuildResult runGradle() {
        return GradleRunner.create()
                .withProjectDir(_folder.getRoot())
                .withPluginClasspath()
                .withArguments("--stacktrace", "grubyjar")
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
        TestUtil.writeTextToFile(_folder.newFile(name),
                textFromLines(lines));
    }

    private static String textFromLines(String... lines) {
        return Joiner.on("\n").join(lines) + "\n";
    }

    String runJar()
    throws IOException, InterruptedException, TimeoutException
    {
        return new ProcessExecutor()
                .command("java", "-jar",
                        "build/libs/" + _folder.getRoot().getName() + ".jar")
                .directory(_folder.getRoot())
                .readOutput(true)
                .exitValueNormal()
                .execute()
                .outputUTF8();
    }
}
