package ca.neitsch.grubbyjar;

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

import static ca.neitsch.grubbyjar.TaskUtil.doLast2;
import static ca.neitsch.grubbyjar.TaskUtil.doLastRethrowing;
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

public class GrubbyjarPrepTask
        extends DefaultTask
{
    private GrubbyjarProject _grubbyjarProject;

    public GrubbyjarPrepTask() {
        doLast2(this, this::verifyJrubyInClasspath);
        doLastRethrowing(this, this::createCleanWorkDir);
        doLastRethrowing(this, this::copyGrubbyjarMainClassIntoWorkDir);
        doLastRethrowing(this, this::copyRubyMain);

        doLastRethrowing(this, this::configureGemDeps);
    }

    void setGrubbyjarProject(GrubbyjarProject grubbyjarProject) {
        _grubbyjarProject = grubbyjarProject;
    }

    private void configureGemDeps()
    throws IOException
    {
        _grubbyjarProject.getGems().forEach(
                gem -> gem.configure(getShadowJar(), getWorkDir()));
    }

    private void copyRubyMain()
    throws IOException
    {
        Script scriptFile = determineScriptFile(getExtension(),
                getProject().getRootDir());

        String requirePath = scriptFile.getPath();
        if (scriptFile.getGem() != null) {
            requirePath = scriptFile.getGem().getTargetDir() + "/" + requirePath;
        }

        Util.writeTextToFile("begin\n"
                        + "load 'uri:classloader://" + requirePath + "'\n"
                        + "nil\n"
                        + "rescue SystemExit => e\n"
                        + "e.status\n"
                        + "end\n",
                new File(getWorkDir(), GrubbyjarProject.GRUBYJAR_MAIN_RB));

        getShadowJar().from(scriptFile.getPath());
    }

    Script determineScriptFile(GrubbyjarExtension extension, File rootDir) {
        String script = extension.getScript();
        if (script == null) {
            Gem sourceGem = _grubbyjarProject.getSourceGem();
            if (sourceGem != null && sourceGem.getExecutable() != null) {
                return new Script(sourceGem.getExecutable(), sourceGem);
            }

            String[] rbFiles = rootDir.list(
                    (dir, name) -> name.endsWith(".rb"));
            if (rbFiles.length == 1) {
                script = rbFiles[0];
            } else {
                String errorPrefix = "No grubbyjar script specified and";

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
        return new Script(script, null);
    }

    static class Script {
        private String _path;
        private Gem _gem;

        public Script(String path, Gem gem) {
            _path = path;
            _gem = gem;
        }

        String getPath() {
            return _path;
        }

        Gem getGem() {
            return _gem;
        }
    }

    private GrubbyjarExtension getExtension() {
        return (GrubbyjarExtension)getProject().getExtensions()
                .getByName(GrubbyjarPlugin.GRUBYJAR_EXTENSION_NAME);
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
     * Create an empty ‘grubbyjar’ directory in gradle’s build output directory.
     */
    void createCleanWorkDir() throws IOException {
        FileUtils.deleteDirectory(getWorkDir());
        getWorkDir().mkdirs();
    }

    private void copyGrubbyjarMainClassIntoWorkDir() throws ClassNotFoundException, IOException
    {
        // It is a compile time error to import a type from the unnamed package,
        // so we use reflection.
        // https://stackoverflow.com/a/2193298
        Class<?> grubyJarMainClass = null;
        grubyJarMainClass = Class.forName(GrubbyjarProject.GRUBYJAR_MAIN);
        InputStream mainClass = getClassDefinition(grubyJarMainClass);

        File mainClassTarget = new File(getWorkDir(), GrubbyjarProject.GRUBYJAR_MAIN + ".class");
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
        return _grubbyjarProject.getWorkDir();
    }

    private ShadowJar getShadowJar() {
        return _grubbyjarProject.getShadowJar();
    }
}
