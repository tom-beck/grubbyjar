package ca.neitsch.grubyjar;

import com.google.common.collect.Sets;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrubyjarPrepTaskTest {
    @Rule
    public ExpectedException _thrown = ExpectedException.none();
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

    private GrubyjarPrepTask _task;

    @Before
    public void createTask() {
        _task = createTask(GrubyjarPrepTask.class);
    }

    @Test
    public void testVerifyJrubyInClasspathExists() {
        Set<File> classpath = Sets.newHashSet();

        for (String s: System.getProperty("java.class.path").split(File.pathSeparator)) {
            classpath.add(new File(s));
        }

        _task.verifyJrubyInClasspath(classpath);
        // expect no error
    }

    @Test
    public void testVerifyJrubyInClasspathFailure() {
        expectGradleException("JRuby");

        _task.verifyJrubyInClasspath(Sets.newHashSet());
    }

    @Test
    public void testUsesGivenScriptFile() {
        GrubyjarExtension extension = new GrubyjarExtension();
        extension.script("xyz.rb");

        assertEquals("xyz.rb", _task.determineScriptFile(extension, null));
    }

    @Test
    public void infersScriptFileIfOnlyOne() {
        GrubyjarExtension extension = new GrubyjarExtension();
        File rootdir = mock(File.class);
        when(rootdir.list(any())).thenReturn(
                new String[] { "bar.rb" });

        assertEquals("bar.rb", _task.determineScriptFile(extension, rootdir));
    }

    @Test
    public void testErrorOnNoScriptFileGiven() {
        GrubyjarExtension extension = new GrubyjarExtension();
        File rootdir = mock(File.class);
        when(rootdir.list(any())).thenReturn(new String[0]);

        expectGradleException("no .rb files");

        _task.determineScriptFile(extension, rootdir);
    }

    @Test
    public void testErrorOnTooManyScriptFiles() {
        GrubyjarExtension extension = new GrubyjarExtension();
        File rootdir = mock(File.class);
        when(rootdir.list(any())).thenReturn(
                new String[] { "foo.rb", "bar.rb" });

        expectGradleException("multiple .rb files [foo.rb, bar.rb]");

        _task.determineScriptFile(extension, rootdir);
    }

    private void expectGradleException(String substring) {
        _thrown.expect(GradleException.class);
        _thrown.expectMessage(substring);
    }

    <T extends Task> T createTask(Class<T> clazz) {
        Project p = new ProjectBuilder()
                .withProjectDir(_folder.getRoot())
                .build();
        return GradleUtil.addTask(p, clazz);
    }
}
