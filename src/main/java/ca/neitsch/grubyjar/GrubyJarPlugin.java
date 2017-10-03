package ca.neitsch.grubyjar;

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar;
import com.google.common.collect.ImmutableMap;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Map;

public class GrubyJarPlugin
        implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        Map<String, Object> config = ImmutableMap.of(
                "type", ShadowJar.class);

        ShadowJar grubyJarTask = (ShadowJar)project.task(config, "grubyjar");
    }
}
