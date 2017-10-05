package ca.neitsch.grubyjar;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zeroturnaround.exec.ProcessExecutor;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class GrubyjarPluginIntegTest {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testHelloWorld()
    throws Exception
    {
        File gradleBuildFile = folder.newFile("build.gradle");

        TestUtil.class.getResource("GrubyjarMainTest.class");
        TestUtil.writeTextToFile(gradleBuildFile,
                TestUtil.readResource("hello-world-script.gradle"));
        TestUtil.writeTextToFile(folder.newFile("foo.rb"),
                "puts 'hello world'.upcase");

        GradleRunner.create()
                .withProjectDir(folder.getRoot())
                .withPluginClasspath()
                .withArguments("shadowJar")
                .forwardOutput()
                .build();

        String jarRunOutput = new ProcessExecutor().command("java", "-jar", "build/libs/foo.jar")
                .directory(folder.getRoot())
                .readOutput(true).execute()
                .outputUTF8();

        assertThat(jarRunOutput, containsString("HELLO WORLD"));
    }
}
