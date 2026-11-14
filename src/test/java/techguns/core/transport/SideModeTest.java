package techguns.core.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SideModeTest {
    @Test
    void wrenchCyclesAllModes() {
        assertEquals(SideMode.OUTPUT, SideMode.NEUTRAL.next());
        assertEquals(SideMode.INPUT, SideMode.OUTPUT.next());
        assertEquals(SideMode.DISABLED, SideMode.INPUT.next());
        assertEquals(SideMode.NEUTRAL, SideMode.DISABLED.next());
    }

    @Test
    void modeRules() {
        assertTrue(SideMode.NEUTRAL.acceptsPush() && SideMode.NEUTRAL.delivers() && !SideMode.NEUTRAL.pulls());
        assertTrue(!SideMode.OUTPUT.acceptsPush() && SideMode.OUTPUT.delivers());
        assertTrue(SideMode.INPUT.pulls() && SideMode.INPUT.acceptsPush() && !SideMode.INPUT.delivers());
        assertFalse(SideMode.DISABLED.connects() || SideMode.DISABLED.delivers() || SideMode.DISABLED.acceptsPush());
    }
}
