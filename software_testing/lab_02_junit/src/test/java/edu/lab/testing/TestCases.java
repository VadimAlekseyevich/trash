package edu.lab.testing;

import java.util.List;

/** Exact T01–T57 inputs and expected validity from laboratory #1. */
public final class TestCases {
    public record UrlCase(String id, String input, boolean valid) {}

    public static final List<UrlCase> ALL = List.of(
        new UrlCase("T01", "http://example.com", true),
        new UrlCase("T02", "https://example.com/catalog?id=1#top", true),
        new UrlCase("T03", "HtTpS://ExAmPlE.CoM", true),
        new UrlCase("T04", "https://example.com:1", true),
        new UrlCase("T05", "https://example.com:65535", true),
        new UrlCase("T06", "https://example.com:0", false),
        new UrlCase("T07", "https://example.com:65536", false),
        new UrlCase("T08", "https://example.com:abc", false),
        new UrlCase("T09", "https://example.com:", false),
        new UrlCase("T10", "https://example.com:-1", false),
        new UrlCase("T11", null, false),
        new UrlCase("T12", "", false),
        new UrlCase("T13", "   ", false),
        new UrlCase("T14", "example.com/path", false),
        new UrlCase("T15", "/catalog?id=1", false),
        new UrlCase("T16", "ftp://example.com/file", false),
        new UrlCase("T17", "https:///path", false),
        new UrlCase("T18", "https://user:pass@example.com", false),
        new UrlCase("T19", "http://localhost", false),
        new UrlCase("T20", "http://192.168.1.1", false),
        new UrlCase("T21", "http://[::1]", false),
        new UrlCase("T22", "https://пример.рф", false),
        new UrlCase("T23", "https://xn--e1afmkfd.xn--p1ai", true),
        new UrlCase("T24", "https://my-site.example", true),
        new UrlCase("T25", "https://-site.example", false),
        new UrlCase("T26", "https://site-.example", false),
        new UrlCase("T27", "https://my_site.example", false),
        new UrlCase("T28", "https://example..com", false),
        new UrlCase("T29", "https://example.com.", false),
        new UrlCase("T30", "https://" + "a".repeat(63) + ".com", true),
        new UrlCase("T31", "https://" + "a".repeat(64) + ".com", false),
        new UrlCase("T32", "https://" + "a".repeat(63) + "." + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(61), true),
        new UrlCase("T33", "https://" + "a".repeat(63) + "." + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(62), false),
        new UrlCase("T34", "https://example.com/" + "a".repeat(2027), true),
        new UrlCase("T35", "https://example.com/" + "a".repeat(2028), true),
        new UrlCase("T36", "https://example.com/" + "a".repeat(2029), false),
        new UrlCase("T37", "https://example.com/a b", false),
        new UrlCase("T38", " https://example.com", false),
        new UrlCase("T39", "https://example.com ", false),
        new UrlCase("T40", "https://example.com/\t", false),
        new UrlCase("T41", "https://example.com/\n", false),
        new UrlCase("T42", "https://example.com/путь", false),
        new UrlCase("T43", "https://example.com/%D0%BF", true),
        new UrlCase("T44", "https://example.com/a%20b", true),
        new UrlCase("T45", "https://example.com/%2", false),
        new UrlCase("T46", "https://example.com/%ZZ", false),
        new UrlCase("T47", "https://example.com/a\\b", false),
        new UrlCase("T48", "http://a.b", true),
        new UrlCase("T49", "http://ab", false),
        new UrlCase("T50", "https://example.com/?#", true),
        new UrlCase("T51", "https:example.com", false),
        new UrlCase("T52", "https://a.123", false),
        new UrlCase("T53", "https://example.com/a[]", false),
        new UrlCase("T54", "https://example.com/#a#b", false),
        new UrlCase("T55", "http://a.b/", true),
        new UrlCase("T56", "https://example.com:00001", true),
        new UrlCase("T57", "https://example.com:000001", false)
    );

    private TestCases() {}

    public static UrlCase byId(String id) {
        return ALL.stream().filter(c -> c.id().equals(id))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Missing case: " + id));
    }
}
