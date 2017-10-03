import ca.neitsch.grubyjar.GrubyjarPlugin;
import org.junit.Test;

import static org.junit.Assert.*;

public class GrubyjarMainTest {
    @Test
    public void grubyjarMainRbConstantsEqual() {
        // There are two different constants because the runtime launcher
        // doesn’t have access to the plugin code, and the plugin code doesn’t
        // have access to the launcher because the launcher is in the default
        // package.
        assertEquals(GrubyjarMain.GRUBYJAR_MAIN_RB,
                GrubyjarPlugin.GRUBYJAR_MAIN_RB);
    }
}
