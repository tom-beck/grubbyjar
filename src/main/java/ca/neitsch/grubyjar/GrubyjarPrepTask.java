package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.Configuration;
import org.jruby.RubyArray;
import org.jruby.RubyHash;
import org.jruby.embed.ScriptingContainer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Set;

import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

public class GrubyjarPrepTask
        extends DefaultTask
{
    private static final String DETERMINE_GEM_FILES_RB = "determine_gem_files.rb";

    private GrubyjarProject _grubyjarProject;

    public GrubyjarPrepTask() {
        doLast2(this::verifyJrubyInClasspath);
        doLastRethrowing(this::createCleanWorkDir);
        doLastRethrowing(this::copyGrubyjarMainClassIntoWorkDir);
        doLastRethrowing(this::copyRubyMain);

        doLastRethrowing(this::configureGemDeps);
    }

    void setGrubyjarProject(GrubyjarProject grubyjarProject) {
        _grubyjarProject = grubyjarProject;
    }

    private void configureGemDeps()
    throws IOException
    {
        File gemfile = getProject().file("Gemfile");
        if (!gemfile.exists())
            return;

        File gemfileLock = getProject().file("Gemfile.lock");
        if (!gemfileLock.exists()) {
            throw new GradleException("Gemfile exists but Gemfile.lock does not; please run `bundle install`.");
        }

        ScriptingContainer s = new ScriptingContainer();

        s.runScriptlet(
                getClass().getResourceAsStream(DETERMINE_GEM_FILES_RB),
                DETERMINE_GEM_FILES_RB);

        RubyArray gems = (RubyArray)s.callMethod(null, "determine_gem_files",
                gemfile.toString(), gemfileLock.toString());

        for (Object o: gems) {
            RubyHash gemInfo = (RubyHash)o;
            getShadowJar().from(gemInfo.get("gemspec"),
                    copyspec -> copyspec.into("specifications"));
            getShadowJar().from(gemInfo.get("install_path"),
                    (copyspec) -> {
                        copyspec.into("gems/" + gemInfo.get("full_name"));
                        // Shadow’s getPath() and getName() are switched here,
                        // so we can’t rely on either in case it gets fixed.
                        copyspec.include((e) -> !e.getRelativePath().getLastName().equals("MANIFEST.MF"));
                    });
        }
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

    /** Like {@code doLast}, but allows for cleaner instance methods that access
     * the task through {@code self} instead of a parameter. */
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
