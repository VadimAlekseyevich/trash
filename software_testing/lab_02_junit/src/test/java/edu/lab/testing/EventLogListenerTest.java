package edu.lab.testing;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import static org.junit.Assert.*;

/** Verifies event handling rather than merely printing simulated events. */
public class EventLogListenerTest {
    public static class SmallSample {
        @Test public void passingTest() {
            assertTrue(true);
        }
    }

    @Test public void listenerReceivesTestEvents() {
        JUnitCore core = new JUnitCore();
        EventLogListener listener = new EventLogListener();
        core.addListener(listener);
        Result result = core.run(SmallSample.class);
        assertTrue(result.wasSuccessful());
        assertEquals(1, result.getRunCount());
        assertEquals(1, listener.startedCount());
        assertEquals(1, listener.finishedCount());
    }
}
