package ca.neitsch.grubyjar;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GrubyJarPlugin
        implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println("Hello, world");
    }
}
