package edu.lab.testing;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/** Registers the listener with the runner, as in the laboratory example. */
public class LoggingRunner extends BlockJUnit4ClassRunner {
    public LoggingRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override public void run(RunNotifier notifier) {
        EventLogListener listener = new EventLogListener();
        notifier.addListener(listener);
        try {
            super.run(notifier);
        } finally {
            notifier.removeListener(listener);
        }
    }
}
