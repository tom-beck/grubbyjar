package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
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
import java.util.Arrays;
import java.util.Set;

import static ca.neitsch.grubyjar.TaskUtil.doLast2;
import static ca.neitsch.grubyjar.TaskUtil.doLastRethrowing;
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

public class GrubyjarPrepTask
    extends DefaultTask
{
    private GrubyjarProject _grubyjarProject;

    public GrubyjarPrepTask() {
        doLast2(this, this::verifyJrubyInClasspath);
        doLastRethrowing(this, this::createCleanWorkDir);
        doLastRethrowing(this, this::copyGrubyjarMainClassIntoWorkDir);
        doLastRethrowing(this, this::copyRubyMain);

        doLastRethrowing(this, this::configureGemDeps);
    }

    void setGrubyjarProject(GrubyjarProject grubyjarProject) {
        _grubyjarProject = grubyjarProject;
    }

    private void configureGemDeps()
    throws IOException
    {
        _grubyjarProject.getGems().forEach(
                gem -> gem.configure(getShadowJar(), getWorkDir()));
    }

    private void copyRubyMain()
    throws IOException
    {
        String scriptFile = determineScriptFile(getExtension(),
                getProject().getRootDir());

        FileUtils.copyFile(getProject().file(scriptFile),
                new File(getWorkDir(), GrubyjarProject.GRUBYJAR_MAIN_RB));
    }

    String determineScriptFile(GrubyjarExtension extension, File rootDir) {
        String script = extension.getScript();
        if (script == null) {
            Gem sourceGem = _grubyjarProject.getSourceGem();
            if (sourceGem != null && sourceGem.getExecutable() != null) {
                return sourceGem.getExecutable();
            }

            String[] rbFiles = rootDir.list(
                    (dir, name) -> name.endsWith(".rb"));
            if (rbFiles.length == 1) {
                script = rbFiles[0];
            } else {
                String errorPrefix = "No grubyjar script specified and";

                if (rbFiles.length == 0) {
                    throw new GradleException(errorPrefix
                            + " no .rb files found in "
                            + rootDir);
                } else {
                    throw new GradleException(errorPrefix
                            + " multiple .rb files "
                            + Arrays.toString(rbFiles)
                            + " found in"
                            + rootDir);
                }
            }
        }
        return script;
    }

    private GrubyjarExtension getExtension() {
        return (GrubyjarExtension)getProject().getExtensions()
                .getByName(GrubyjarPlugin.GRUBYJAR_EXTENSION_NAME);
    }

    void verifyJrubyInClasspath() {
        Configuration runtime = GradleUtil.getRuntimeConfiguration(getProject());

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
        FileUtils.deleteDirectory(getWorkDir());
        getWorkDir().mkdirs();
    }

    private void copyGrubyjarMainClassIntoWorkDir() throws ClassNotFoundException, IOException
    {
        // It is a compile time error to import a type from the unnamed package,
        // so we use reflection.
        // https://stackoverflow.com/a/2193298
        Class<?> grubyJarMainClass = null;
        grubyJarMainClass = Class.forName(GrubyjarProject.GRUBYJAR_MAIN);
        InputStream mainClass = getClassDefinition(grubyJarMainClass);

        File mainClassTarget = new File(getWorkDir(), GrubyjarProject.GRUBYJAR_MAIN + ".class");
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

    private File getWorkDir() {
        return _grubyjarProject.getWorkDir();
    }

    private ShadowJar getShadowJar() {
        return _grubyjarProject.getShadowJar();
    }
}
