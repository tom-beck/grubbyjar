package ca.neitsch.grubyjar;

import com.google.common.collect.Sets;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;

public class GrubyjarPrepTaskTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testVerifyJrubyInClasspathExists() {
        Set<File> classpath = Sets.newHashSet();

        for (String s: System.getProperty("java.class.path").split(File.pathSeparator)) {
          classpath.add(new File(s));
        }

        GrubyjarPrepTask task = createTask(GrubyjarPrepTask.class);
        task.verifyJrubyInClasspath(classpath);
        // expect no error
    }

    @Test
    public void testVerifyJrubyInClasspathFailure() {
        thrown.expect(GradleException.class);
        thrown.expectMessage("JRuby");

        GrubyjarPrepTask task = createTask(GrubyjarPrepTask.class);
        task.verifyJrubyInClasspath(Sets.newHashSet());
        // expect no error
    }

    <T extends Task> T createTask(Class<T> clazz) {
      Project p = new ProjectBuilder()
              .withProjectDir(folder.getRoot())
              .build();
      return GradleUtil.addTask(p, clazz);
    }
}
