package cli.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link NoActiveSessionException}.
 */
@DisplayName("NoActiveSessionException")
class NoActiveSessionExceptionTest {

    @Test
    @DisplayName("Exception has the expected message")
    void expectedMessage() {
        NoActiveSessionException ex = new NoActiveSessionException();
        assertEquals("No active session available. Use 'open <url>' to start one.", ex.getMessage());
    }

    @Test
    @DisplayName("Is a RuntimeException")
    void isRuntimeException() {
        assertTrue(new NoActiveSessionException() instanceof RuntimeException);
    }
}

