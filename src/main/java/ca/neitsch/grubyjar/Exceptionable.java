package ca.neitsch.grubyjar;

public interface Exceptionable<E extends Exception> {
    void run() throws E;

    default void runRethrowing() {
        try {
            run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static <E extends Exception> void rethrowing(Exceptionable<E> e) {
        e.runRethrowing();
    }
}
