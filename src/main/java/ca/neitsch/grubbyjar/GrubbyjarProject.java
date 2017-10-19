package ca.neitsch.grubbyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ApplicationPluginConvention;

import java.io.File;
import java.util.List;

import static ca.neitsch.grubbyjar.GradleUtil.addTask;

/**
 * A gradle project to which Grubbyjar has been applied
 */
public class GrubbyjarProject {
    @VisibleForTesting
    public static final String GRUBBYJAR_MAIN_RB = "grubbyjar_main.rb";
    static final String GRUBBYJAR_MAIN = "GrubbyjarMain";

    private Project _project;
    private ShadowJar _shadowJar;
    private File _workDir;
    private GrubbyjarPrepTask _grubbyjarPrep;
    private List<Gem> _gems;

    public GrubbyjarProject(Project project) {
        _project = project;
        _workDir = new File(_project.getBuildDir(),"grubbyjar");
    }

    public void configure() {
        setGrubbyjarMainAsApplicationMain();

        _shadowJar = applyShadowPlugin();
        _shadowJar.setArchiveName(getArchiveName());

        _grubbyjarPrep = addTask(_project, GrubbyjarPrepTask.class);
        _grubbyjarPrep.setGrubbyjarProject(this);

        addTask(_project, GrubbyjarRequireTask.class);

        _shadowJar.dependsOn(_grubbyjarPrep);

        // Include the work directory contents in the jar
        _shadowJar.from(_workDir);
    }

    ShadowJar getShadowJar() {
        return _shadowJar;
    }

    File getWorkDir() {
        return _workDir;
    }

    List<Gem> getGems() {
        if (_gems == null) {
            _gems = Gem.loadGemDeps(_project);
        }
        return _gems;
    }

    /* * If the project directory is a gem, return the Gem object, otherwise
     * return null. */
    Gem getSourceGem() {
        for (Gem g: getGems()) {
            if (g.getInstallPath().equals(_project.getRootDir().toString())) {
                return g;
            }
        }
        return null;
    }

    private ShadowJar applyShadowPlugin() {
        _project.apply(ImmutableMap.of("plugin", "com.github.johnrengelman.shadow"));
        ShadowJar shadowJar = (ShadowJar)_project.getTasks().getByName("shadowJar");

        Task grubbyJarTask = _project.task("grubbyjar");
        grubbyJarTask.dependsOn(shadowJar);
        return shadowJar;
    }


    private void setGrubbyjarMainAsApplicationMain() {
        _project.apply(ImmutableMap.of("plugin", "application"));
        ApplicationPluginConvention
                pluginConvention = (ApplicationPluginConvention)_project.getConvention().getPlugins().get("application");
        pluginConvention.setMainClassName(GRUBBYJAR_MAIN);
    }

    private String getArchiveName() {
        return _project.getName() + ".jar";
    }
}
