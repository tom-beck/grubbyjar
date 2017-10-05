package ca.neitsch.grubyjar;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

public class GrubyjarPrepTask
    extends DefaultTask
{
    private File _workDir;

    public GrubyjarPrepTask() {
        doLast2(this::verifyJrubyInClasspath);
        doLastRethrowing(this::createCleanWorkDir);
        doLastRethrowing(this::copyGrubyjarMainClassIntoWorkDir);
        doLastRethrowing(this::copyRubyMain);
    }

    void setWorkDir(File workDir) {
      _workDir = workDir;
    }

    private void copyRubyMain()
            throws IOException
    {
        FileUtils.copyFile(getProject().file("foo.rb"),
                new File(_workDir, GrubyjarProject.GRUBYJAR_MAIN_RB));
    }

    void doLast2(Runnable r) {
        doLast((t) -> r.run());
    }

    <E extends Exception> void doLastRethrowing(Exceptionable<E> r) {
        doLast((t) -> r.runRethrowing());
    }

    void verifyJrubyInClasspath() {
        Configuration runtime = getProject().getConfigurations().getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME);

        Set<File> files = runtime.getResolvedConfiguration().getFiles();
        verifyJrubyInClasspath(files);
    }

    void verifyJrubyInClasspath(Set<File> files) {
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

    /**
     * Create an empty ‘grubyjar’ directory in gradle’s build output directory.
     */
    void createCleanWorkDir() throws IOException {
        FileUtils.deleteDirectory(_workDir);
        _workDir.mkdirs();
    }

    private void copyGrubyjarMainClassIntoWorkDir() throws ClassNotFoundException, IOException
    {
        // It is a compile time error to import a type from the unnamed package,
        // so we use reflection.
        // https://stackoverflow.com/a/2193298
        Class<?> grubyJarMainClass = null;
        grubyJarMainClass = Class.forName(GrubyjarProject.GRUBYJAR_MAIN);
        InputStream mainClass = getClassDefinition(grubyJarMainClass);

        File mainClassTarget = new File(_workDir, GrubyjarProject.GRUBYJAR_MAIN + ".class");
        FileUtils.copyInputStreamToFile(mainClass, mainClassTarget);
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
