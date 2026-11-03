package edu.lab.testing;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/** Constructor, invariants, exceptions, timeout, Assume and @Ignore. */
public class AnnotationExamplesTest {
    @Test public void publicConstructorCreatesStatelessValidator() {
        UrlValidator first = new UrlValidator();
        UrlValidator second = new UrlValidator();
        assertNotNull(first);
        assertNotSame(first, second);
        assertTrue(first.isValid("http://a.b"));
    }

    @Test public void parsedUrlConstructorEnforcesInvariants() {
        ParsedUrl ok = new ParsedUrl("https", "example.com", -1, "/", null, null);
        assertEquals("example.com", ok.host());
        assertThrows(IllegalArgumentException.class,
                () -> new ParsedUrl("ftp", "example.com", -1, "/", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ParsedUrl("https", "example.com", 0, "/", null, null));
        assertThrows(NullPointerException.class,
                () -> new ParsedUrl(null, "example.com", -1, "/", null, null));
    }

    @Test(expected = InvalidUrlException.class)
    public void validateNullThrowsCheckedException() throws InvalidUrlException {
        new UrlValidator().validate(null);
    }

    @Test(expected = InvalidUrlException.class)
    public void validateFtpThrowsCheckedException() throws InvalidUrlException {
        new UrlValidator().validate("ftp://example.com/file");
    }

    @Test(timeout = 1000)
    public void validationFinishesWithinTimeLimit() {
        assertFalse(new UrlValidator().isValid("https://example.com:65536"));
    }

    @Test public void prerequisiteWithAssume() {
        Assume.assumeTrue("Class tests require the 57 cases from lab #1", TestCases.ALL.size() == 57);
        assertEquals(17, TestCases.ALL.stream().filter(TestCases.UrlCase::valid).count());
    }

    @Ignore("Demonstration of the @Ignore annotation from the methodology")
    @Test public void intentionallyIgnoredExample() {
        fail("This test is intentionally ignored");
    }
}
