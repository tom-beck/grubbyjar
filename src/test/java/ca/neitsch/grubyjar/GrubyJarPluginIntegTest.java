package ca.neitsch.grubyjar;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class GrubyJarPluginIntegTest {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testHelloWorld()
            throws IOException
    {
        File gradleBuildFile = folder.newFile("build.gradle");
        Util.writeTextToFile(gradleBuildFile,
                "plugins { id 'ca.neitsch.grubyjar' }");

        BuildResult result = GradleRunner.create()
                .withProjectDir(folder.getRoot())
                .withPluginClasspath()
                .build();

        assertThat(result.getOutput(), containsString("Hello, world"));
    }
}
