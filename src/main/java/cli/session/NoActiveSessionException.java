package cli.session;

/**
 * Thrown when a command requires an active browser session but none exists.
 */
public class NoActiveSessionException extends RuntimeException {

    public NoActiveSessionException() {
        super("No active session available. Use 'open <url>' to start one.");
    }
}

