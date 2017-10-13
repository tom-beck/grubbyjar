import org.jruby.embed.ScriptingContainer;

import java.io.InputStream;

public class GrubbyjarMain {
    static final String GRUBYJAR_MAIN_RB = "grubbyjar_main.rb";

    public static void main(String[] args)
    throws Exception
    {
        ScriptingContainer s = new ScriptingContainer();

        disabledSharedGems(s);

        s.setArgv(args);
        InputStream main = GrubbyjarMain.class.getResourceAsStream(
                GRUBYJAR_MAIN_RB);

        if (main == null) {
            throw new RuntimeException(GRUBYJAR_MAIN_RB + " not found in jar");
        }
        s.runScriptlet(main, GRUBYJAR_MAIN_RB);
        System.exit(0);
    }

    @SuppressWarnings("unchecked")
    private static void disabledSharedGems(ScriptingContainer s) {
        s.getEnvironment().put("GEM_PATH", "");
        s.getEnvironment().put("RUBYLIB", "");
    }
}
