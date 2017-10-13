package ca.neitsch.grubbyjar;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TestUtil {
    private TestUtil() {
        throw new UnsupportedOperationException("static class");
    }

    /** Assumes text file */
    public static String readResource(String path) {
        InputStream stream = inputResource(path, 3);
        try {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static InputStream inputResource(String path, int depth) {
        String className = getCallingClassName(depth);
        String packageName = className.substring(0,
                className.lastIndexOf("."));
        String lookupPath = "/"
                + packageName.replace(".", "/")
                + "/" + path;
        InputStream ret = TestUtil.class.getResourceAsStream(lookupPath);
        if (ret == null) {
            throw new RuntimeException(lookupPath + " not found in classpath");
        }
        return ret;
    }

    static String getCallingClassName(int depth) {
        StackTraceElement e[]
                = new Exception().fillInStackTrace().getStackTrace();
        return e[depth].getClassName();
    }
}
