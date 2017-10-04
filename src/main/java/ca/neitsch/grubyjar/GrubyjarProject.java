package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.ApplicationPluginConvention;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

/**
 * A gradle project to which Grubyjar has been applied
 */
public class GrubyjarProject {
    @VisibleForTesting
    public static final String GRUBYJAR_MAIN_RB = "grubyjar_main.rb";
    private static final String GRUBYJAR_MAIN = "GrubyjarMain";

    private Project _project;
    private File _workDir;
    private ShadowJar _shadowJar;


    public GrubyjarProject(Project project) {
        _project = project;
    }

    public void configure() {
        try {
            createCleanWorkDir();
            copyGrubyjarMainClassIntoWorkDir();
            FileUtils.copyFile(_project.file("foo.rb"),
                    new File(_workDir, GRUBYJAR_MAIN_RB));

            setGrubyjarMainAsApplicationMain();
            _shadowJar = applyShadowPlugin();
            _shadowJar.setArchiveName(getArchiveName());

            _shadowJar.doFirst(this::verifyJrubyInClasspath);

            // Include the work directory contents in the jar
            _shadowJar.from(_workDir);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void verifyJrubyInClasspath(Task task) {
        Configuration runtime = task.getProject().getConfigurations().getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME);

        Set<File> files = runtime.getResolvedConfiguration().getFiles();
        try {
            URL classpath[] = files.stream().map(f -> {
                try {
                    return f.toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(URL[]::new);

            URLClassLoader cl = new URLClassLoader(classpath, null);
            Class<?> scriptingContainer = cl.loadClass(ScriptingContainer.class.getName());
            if (scriptingContainer != null)
                return;
        } catch(ClassNotFoundException e) {
            raiseJrubyNotFound(files);
        }
        raiseJrubyNotFound(files);
    }

    private void raiseJrubyNotFound(Set<File> files) {
        throw new GradleException("JRuby not found in "
                + RUNTIME_CLASSPATH_CONFIGURATION_NAME
                + " configuration:\n  "
                + Joiner.on("\n  ").join(files));
    }

    private ShadowJar applyShadowPlugin() {
        _project.apply(ImmutableMap.of("plugin", "com.github.johnrengelman.shadow"));
        ShadowJar shadowJar = (ShadowJar)_project.getTasks().getByName("shadowJar");

        Task grubyJarTask = _project.task("grubyjar");
        grubyJarTask.dependsOn(shadowJar);
        return shadowJar;
    }

    /**
     * Create an empty ‘grubyjar’ directory in gradle’s build output directory.
     */
    private void createCleanWorkDir() throws IOException {
        _workDir = new File(_project.getBuildDir(),
                "grubyjar");
        FileUtils.deleteDirectory(_workDir);
        _workDir.mkdirs();
    }

    private void setGrubyjarMainAsApplicationMain() {
        _project.apply(ImmutableMap.of("plugin", "application"));
        ApplicationPluginConvention
                pluginConvention = (ApplicationPluginConvention)_project.getConvention().getPlugins().get("application");
        pluginConvention.setMainClassName(GRUBYJAR_MAIN);
    }

    private void copyGrubyjarMainClassIntoWorkDir() throws ClassNotFoundException, IOException
    {
        // It is a compile time error to import a type from the unnamed package,
        // so we use reflection.
        // https://stackoverflow.com/a/2193298
        Class<?> grubyJarMainClass = null;
        grubyJarMainClass = Class.forName(GRUBYJAR_MAIN);
        InputStream mainClass = getClassDefinition(grubyJarMainClass);

        File mainClassTarget = new File(_workDir, GRUBYJAR_MAIN + ".class");
        FileUtils.copyInputStreamToFile(mainClass, mainClassTarget);
    }

    private String getArchiveName() {
        return "foo.jar";
    }

    private InputStream getClassDefinition(Class<?> c) {
        String path = c.getName().replace(".", "/") + ".class";
        InputStream ret = c.getResourceAsStream(path);
        if (ret == null) {
            throw new RuntimeException("Could not find " + path);
        }
        return ret;
    }
}
