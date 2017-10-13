package ca.neitsch.grubbyjar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GrubbyjarPlugin
        implements Plugin<Project> {
    public static final String GRUBYJAR_EXTENSION_NAME = "grubbyjar";

    @Override
    public void apply(Project project) {
        new GrubbyjarProject(project).configure();

        project.getExtensions().create(GRUBYJAR_EXTENSION_NAME, GrubbyjarExtension.class);
    }
}
