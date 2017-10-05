package ca.neitsch.grubyjar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestUtil {
    private TestUtil() {
        throw new UnsupportedOperationException("static class");
    }

    static void writeTextToFile(File f, String t) {
        try {
            Files.write(f.toPath(), t.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
