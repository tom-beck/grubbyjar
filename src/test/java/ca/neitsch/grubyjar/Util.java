package ca.neitsch.grubyjar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Util {
    private Util() {
        throw new UnsupportedOperationException("static class");
    }

    public static void writeTextToFile(File f, String t) {
        try {
          Files.write(f.toPath(), t.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
