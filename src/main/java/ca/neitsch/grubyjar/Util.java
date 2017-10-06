package ca.neitsch.grubyjar;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Util {
    private Util() {
        throw new UnsupportedOperationException("static class");
    }

    static String getStringResource(Object o, String path)
            throws IOException
    {
        URL url = o.getClass().getResource(path);
        return IOUtils.toString(url, UTF_8);
    }
}
