package ca.neitsch.grubyjar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GrubyjarPlugin
        implements Plugin<Project> {
    public static final String GRUBYJAR_EXTENSION_NAME = "grubyjar";

    @Override
    public void apply(Project project) {
        new GrubyjarProject(project).configure();

        project.getExtensions().create(GRUBYJAR_EXTENSION_NAME, GrubyjarExtension.class);
    }
}
