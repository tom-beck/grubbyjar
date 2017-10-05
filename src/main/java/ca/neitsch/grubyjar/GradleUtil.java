package ca.neitsch.grubyjar;

import com.google.common.collect.ImmutableMap;
import org.gradle.api.Project;
import org.gradle.api.Task;

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
        return addTask(project, clazz, taskName);
    }

    /** Convenience method to add a task to a Gradle project */
    static <T extends Task> T addTask(Project project, Class<T> clazz, String name) {
        return clazz.cast(project.task(ImmutableMap.of("type", clazz), name));
    }
}
