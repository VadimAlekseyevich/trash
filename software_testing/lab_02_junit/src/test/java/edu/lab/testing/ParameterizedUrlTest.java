package edu.lab.testing;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Reuses the very same T01–T57 data for JUnit 4 parameterization. */
@RunWith(Parameterized.class)
public class ParameterizedUrlTest {
    private final TestCases.UrlCase test;

    public ParameterizedUrlTest(String id, String input, boolean valid) {
        this.test = new TestCases.UrlCase(id, input, valid);
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> cases() {
        return TestCases.ALL.stream()
                .map(c -> new Object[] { c.id(), c.input(), c.valid() })
                .collect(Collectors.toList());
    }

    @Test public void verifiesValidityAndValidation() throws InvalidUrlException {
        UrlAssertions.verify(new UrlValidator(), test);
    }
}
