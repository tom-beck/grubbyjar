import org.jruby.Ruby;
import org.jruby.RubySystemExit;
import org.jruby.embed.ScriptingContainer;
import org.jruby.exceptions.RaiseException;

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
        try {
            s.runScriptlet(main, "uri:classloader://" + GRUBYJAR_MAIN_RB);
            System.exit(0);
        } catch (RaiseException e) {
            if (e.getException() instanceof RubySystemExit) {
                RubySystemExit ex = (RubySystemExit)e.getException();
                System.exit(ex.status().convertToInteger().getIntValue());
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void disabledSharedGems(ScriptingContainer s) {
        s.getEnvironment().put("GEM_PATH", "");
        s.getEnvironment().put("RUBYLIB", "");
    }
}
