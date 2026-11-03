package edu.lab.testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/** JUnit 4 listener. Logs real test events to target/junit-events.log. */
public class EventLogListener extends RunListener {
    private final Path log = Path.of("target", "junit-events.log");
    private int started;
    private int finished;

    private synchronized void write(String event, String info) throws IOException {
        Files.createDirectories(log.getParent());
        Files.writeString(log, event + ": " + info + System.lineSeparator(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public int startedCount() { return started; }
    public int finishedCount() { return finished; }

    @Override public void testRunStarted(Description description) throws Exception {
        write("RUN_STARTED", description.getDisplayName());
    }

    @Override public void testStarted(Description description) throws Exception {
        started++;
        write("STARTED", description.getDisplayName());
    }

    @Override public void testFinished(Description description) throws Exception {
        finished++;
        write("FINISHED", description.getDisplayName());
    }

    @Override public void testFailure(Failure failure) throws Exception {
        write("FAILED", failure.getDescription().getDisplayName() + " -> " + failure.getException());
    }

    @Override public void testIgnored(Description description) throws Exception {
        write("IGNORED", description.getDisplayName());
    }

    @Override public void testAssumptionFailure(Failure failure) {
        try {
            write("ASSUMPTION_FAILED", failure.getDescription().getDisplayName());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public void testRunFinished(Result result) throws Exception {
        write("RUN_FINISHED", "executed=" + result.getRunCount()
                + ", failed=" + result.getFailureCount()
                + ", ignored=" + result.getIgnoreCount()
                + ", durationMs=" + result.getRunTime()
                + ", success=" + result.wasSuccessful());
    }
}
