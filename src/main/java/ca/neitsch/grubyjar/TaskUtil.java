package ca.neitsch.grubyjar;

import org.gradle.api.Task;

class TaskUtil
{
    private TaskUtil() {
        throw new UnsupportedOperationException("static class");
    }

    /** Like {@code doLast}, but allows for cleaner instance methods that access
     * the task through {@code self} instead of a parameter. */
    static void doLast2(Task t, Runnable r) {
        t.doLast((t2) -> r.run());
    }

    static <E extends Exception> void doLastRethrowing(Task t, Exceptionable<E> r) {
        t.doLast((t2) -> r.runRethrowing());
    }
}
