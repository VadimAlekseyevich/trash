package edu.lab.testing;

import static org.junit.Assert.*;

/** Shared assertions used by both independent and parameterized test forms. */
public final class UrlAssertions {
    private UrlAssertions() {}

    public static void verify(UrlValidator validator, TestCases.UrlCase test) throws InvalidUrlException {
        assertEquals(test.id() + " / isValid", test.valid(), validator.isValid(test.input()));
        if (test.valid()) {
            ParsedUrl parsed = validator.validate(test.input());
            assertNotNull(test.id() + " / validate", parsed);
            assertNotNull(test.id() + " / parsed host", parsed.host());
            assertTrue(test.id() + " / host must have a dot", parsed.host().contains("."));
            assertTrue(test.id() + " / parsed scheme",
                    parsed.scheme().equals("http") || parsed.scheme().equals("https"));
        } else {
            try {
                validator.validate(test.input());
                fail(test.id() + " / invalid URL must throw InvalidUrlException");
            } catch (InvalidUrlException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }
}
