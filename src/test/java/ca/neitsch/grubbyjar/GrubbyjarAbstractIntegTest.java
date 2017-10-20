package ca.neitsch.grubbyjar;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public abstract class GrubbyjarAbstractIntegTest {
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

    @Rule
    public ExpectedException _thrown = ExpectedException.none();

    protected File _gradleBuildFile;

    @Before
    public void createBuildFileObject()
    throws IOException
    {
        _gradleBuildFile = _folder.newFile("build.gradle");
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

    protected void textFile(String name, String... lines)
    {
        Exceptionable.rethrowing(() ->
                Util.writeTextToFile(textFromLines(lines),
                        new File(_folder.getRoot(), name)));
    }

    protected static String textFromLines(String... lines) {
        return Joiner.on("\n").join(lines) + "\n";
    }

    String runJar()
    {
        return new BuildDirCommand().run();
    }

    class BuildDirCommand {
        ProcessExecutor _process;

        BuildDirCommand(String... command) {
            if (command.length == 0)
                command = defaultCommand();

            _process = new ProcessExecutor().command(command)
                    .directory(_folder.getRoot());
            _process.exitValueNormal();
        }

        BuildDirCommand exitValues(int... values) {
            _process.exitValues(values);
            return this;
        }

        BuildDirCommand addJarArguments(String... arguments) {
            _process.command(ArrayUtils.addAll(defaultCommand(), arguments));
            return this;
        }

        BuildDirCommand environment(String key, String value) {
            _process.environment(key, value);
            return this;
        }

        private String[] defaultCommand() {
            return new String[] {
                    "java", "-jar",
                    "build/libs/" + _folder.getRoot().getName() + "-grubbyjar.jar"
            };
        }

        String run() {
            try {
                return _process.readOutput(true).execute().outputUTF8();
            } catch (IOException | InterruptedException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
