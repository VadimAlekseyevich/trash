package edu.lab.testing;

/** Checked error for a URL rejected by the laboratory contract. */
public class InvalidUrlException extends Exception {
    public InvalidUrlException(String message) {
        super(message);
    }

    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
