package ca.neitsch.grubyjar;

interface Exceptionable<T extends Throwable> {
    void run() throws T;
}
