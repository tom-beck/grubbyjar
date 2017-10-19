import ca.neitsch.grubbyjar.Gem;
import ca.neitsch.grubbyjar.GrubbyjarProject;
import org.junit.Test;

import static org.junit.Assert.*;

public class GrubbyjarMainTest {
    @Test
    public void grubbyjarMainRbConstantsEqual() {
        // There are two different constants because the runtime launcher
        // doesn’t have access to the plugin code, and the plugin code doesn’t
        // have access to the launcher because the launcher is in the default
        // package.
        assertEquals(GrubbyjarMain.GRUBBYJAR_MAIN_RB,
                GrubbyjarProject.GRUBBYJAR_MAIN_RB);
        assertEquals(GrubbyjarMain.GRUBBYJAR_JAR_PRELOAD_LIST,
                Gem.GRUBBYJAR_JAR_PRELOAD_LIST);
    }
}
