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
}
