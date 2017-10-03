import org.jruby.embed.ScriptingContainer;

import java.io.InputStream;

public class GrubyjarMain {
    static final String GRUBYJAR_MAIN_RB = "grubyjar_main.rb";

    public static void main(String[] args)
    throws Exception
    {
        ScriptingContainer s = new ScriptingContainer();

        s.setArgv(args);
        InputStream main = GrubyjarMain.class.getResourceAsStream(
                GRUBYJAR_MAIN_RB);

        if (main == null) {
            throw new RuntimeException(GRUBYJAR_MAIN_RB + " not found in jar");
        }
        s.runScriptlet(main, GRUBYJAR_MAIN_RB);
    }
}