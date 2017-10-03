import org.jruby.embed.ScriptingContainer;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class GrubyJarMain {
    private static final String GRUBY_MAIN_FILE = "grubyjar_main.rb";

    public static void main(String[] args)
    throws Exception
    {
        ScriptingContainer s = new ScriptingContainer();

        s.setArgv(args);
        InputStream main = GrubyJarMain.class.getResourceAsStream(
                GRUBY_MAIN_FILE);

        if (main == null) {
            throw new RuntimeException(GRUBY_MAIN_FILE + " not found in jar");
        }
        s.runScriptlet(main, GRUBY_MAIN_FILE);
    }
}
