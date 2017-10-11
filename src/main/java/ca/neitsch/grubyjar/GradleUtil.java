package ca.neitsch.grubyjar;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

class GradleUtil {
    private GradleUtil() {
        throw new UnsupportedOperationException("static class");
    }

    /* * Convenience method to add a task to a Gradle project, using the last
     * component of the class name as that task name. */
    static <T extends Task> T addTask(Project project, Class<T> clazz) {
        String className = clazz.getSimpleName();
        String taskName = className.substring(0, 1).toLowerCase()
                + className.substring(1);
        if (taskName.endsWith("Task"))
            taskName = taskName.substring(0, taskName.length() - 4);
        return addTask(project, clazz, taskName);
    }

    /** Convenience method to add a task to a Gradle project */
    static <T extends Task> T addTask(Project project, Class<T> clazz, String name) {
        return clazz.cast(project.task(ImmutableMap.of("type", clazz), name));
    }

    static Configuration getRuntimeConfiguration(Project p) {
        return p.getConfigurations().getByName(
                RUNTIME_CLASSPATH_CONFIGURATION_NAME);
    }
}
