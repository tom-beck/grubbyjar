package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ApplicationPluginConvention;

import java.io.File;

import static ca.neitsch.grubyjar.GradleUtil.addTask;

/**
 * A gradle project to which Grubyjar has been applied
 */
public class GrubyjarProject {
    @VisibleForTesting
    public static final String GRUBYJAR_MAIN_RB = "grubyjar_main.rb";
    static final String GRUBYJAR_MAIN = "GrubyjarMain";

    private Project _project;
    private ShadowJar _shadowJar;
    private File _workDir;
    private GrubyjarPrepTask _grubyjarPrep;

    public GrubyjarProject(Project project) {
        _project = project;
        _workDir = new File(_project.getBuildDir(),"grubyjar");
    }

    public void configure() {
        setGrubyjarMainAsApplicationMain();

        _shadowJar = applyShadowPlugin();
        _shadowJar.setArchiveName(getArchiveName());

        _grubyjarPrep = addTask(_project, GrubyjarPrepTask.class);
        _grubyjarPrep.setGrubyjarProject(this);

        _shadowJar.dependsOn(_grubyjarPrep);

        // Include the work directory contents in the jar
        _shadowJar.from(_workDir);
    }

    ShadowJar getShadowJar() {
        return _shadowJar;
    }

    File getWorkDir() {
        return _workDir;
    }

    private ShadowJar applyShadowPlugin() {
        _project.apply(ImmutableMap.of("plugin", "com.github.johnrengelman.shadow"));
        ShadowJar shadowJar = (ShadowJar)_project.getTasks().getByName("shadowJar");

        Task grubyJarTask = _project.task("grubyjar");
        grubyJarTask.dependsOn(shadowJar);
        return shadowJar;
    }


    private void setGrubyjarMainAsApplicationMain() {
        _project.apply(ImmutableMap.of("plugin", "application"));
        ApplicationPluginConvention
                pluginConvention = (ApplicationPluginConvention)_project.getConvention().getPlugins().get("application");
        pluginConvention.setMainClassName(GRUBYJAR_MAIN);
    }

    private String getArchiveName() {
        return _project.getName() + ".jar";
    }
}
