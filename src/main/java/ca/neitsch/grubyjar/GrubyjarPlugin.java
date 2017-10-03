package ca.neitsch.grubyjar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GrubyjarPlugin
        implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        new GrubyjarProject(project).configure();
    }
}
