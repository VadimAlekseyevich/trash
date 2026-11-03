package edu.lab.testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        IndependentUrlTest.class,
        ParameterizedUrlTest.class,
        AnnotationExamplesTest.class,
        EventLogListenerTest.class
})
public class AllTests {
}
