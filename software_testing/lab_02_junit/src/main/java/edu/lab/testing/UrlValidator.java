package edu.lab.testing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * Educational URL field contract used in laboratory #1.
 * These are local requirements, NOT the general definition of a valid URL.
 */
public final class UrlValidator {
    public static final int MIN_LENGTH = 10; // "http://a.b"
    public static final int MAX_LENGTH = 2048;

    public UrlValidator() {
        // Stateless class, public constructor is intentionally testable.
    }

    public boolean isValid(String input) {
        try {
            validate(input);
            return true;
        } catch (InvalidUrlException e) {
            return false;
        }
    }

    /** Returns parsed URL or throws the checked exception on invalid input. */
    public ParsedUrl validate(String input) throws InvalidUrlException {
        if (input == null || input.length() < MIN_LENGTH || input.length() > MAX_LENGTH) {
            throw new InvalidUrlException("URL length is outside permitted limits");
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c < '!' || c > '~' || c == '\\') {
                throw new InvalidUrlException("Non-ASCII, whitespace or backslash");
            }
        }
        if (input.indexOf('#') != input.lastIndexOf('#')) {
            throw new InvalidUrlException("Multiple fragment separators");
        }

        final URI uri;
        try {
            uri = new URI(input);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URI syntax", e);
        }
        if (!uri.isAbsolute() || uri.isOpaque() || uri.getRawAuthority() == null) {
            throw new InvalidUrlException("An absolute hierarchical URL is required");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new InvalidUrlException("Only HTTP(S) is allowed");
        }
        if (uri.getRawUserInfo() != null) {
            throw new InvalidUrlException("User information is not allowed");
        }

        String host = uri.getHost();
        if (!validDomain(host)) {
            throw new InvalidUrlException("Invalid ASCII DNS hostname");
        }

        // URI.getPort() returns -1 for both absent and invalid ports. The suffix
        // check distinguishes these, including empty port and 6-digit values.
        String authority = uri.getRawAuthority();
        if (authority.length() < host.length()
                || !authority.regionMatches(true, 0, host, 0, host.length())) {
            throw new InvalidUrlException("Authority differs from hostname");
        }
        String suffix = authority.substring(host.length());
        int port = -1;
        if (!suffix.isEmpty()) {
            if (!suffix.matches(":[0-9]{1,5}")) {
                throw new InvalidUrlException("Malformed port");
            }
            port = Integer.parseInt(suffix.substring(1));
            if (port < 1 || port > 65535) {
                throw new InvalidUrlException("Port outside 1..65535");
            }
        }

        String path = uri.getRawPath();
        if (path != null && (path.contains("[") || path.contains("]"))) {
            throw new InvalidUrlException("Square brackets forbidden in path");
        }
        return new ParsedUrl(scheme, host.toLowerCase(Locale.ROOT), port,
                path == null ? "" : path, uri.getRawQuery(), uri.getRawFragment());
    }

    private static boolean validDomain(String host) {
        if (host == null || host.length() > 253 || host.indexOf('.') < 0) {
            return false;
        }
        String[] labels = host.split("\\.", -1);
        if (labels.length < 2) {
            return false;
        }
        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63
                    || !label.matches("(?i)[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
                return false;
            }
        }
        return labels[labels.length - 1].matches("(?i)[a-z][a-z0-9-]*");
    }
}
