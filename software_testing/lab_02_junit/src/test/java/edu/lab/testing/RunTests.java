package edu.lab.testing;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/** JUnitCore entry point, records all suite events via EventLogListener. */
public final class RunTests {
    private RunTests() {}

    public static void main(String[] args) {
        JUnitCore core = new JUnitCore();
        core.addListener(new EventLogListener());
        Result result = core.run(AllTests.class);
        System.out.printf("JUnit 4: run=%d, failed=%d, ignored=%d, success=%s%n",
                result.getRunCount(), result.getFailureCount(),
                result.getIgnoreCount(), result.wasSuccessful());
        for (Failure failure : result.getFailures()) {
            System.err.println(failure.getTrace());
        }
        if (!result.wasSuccessful()) {
            System.exit(1);
        }
    }
}
