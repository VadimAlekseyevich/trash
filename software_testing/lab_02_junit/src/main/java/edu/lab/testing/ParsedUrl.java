package edu.lab.testing;

import java.util.Objects;

/** Immutable parse result: constructor protects the class invariants. */
public record ParsedUrl(String scheme, String host, int port, String path,
                        String query, String fragment) {
    public ParsedUrl {
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(host, "host");
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Unsupported scheme");
        }
        if (host.isEmpty()) {
            throw new IllegalArgumentException("Empty host");
        }
        if (port != -1 && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("Port out of range");
        }
    }
}
