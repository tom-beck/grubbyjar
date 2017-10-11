package ca.neitsch.grubyjar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Util {
    private Util() {
        throw new UnsupportedOperationException("static class");
    }

    static void writeTextToFile(String t, File f) {
        try {
            Files.write(f.toPath(), t.getBytes(UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
