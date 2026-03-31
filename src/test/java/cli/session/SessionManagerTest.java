package cli.session;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionManager} — singleton lifecycle and state checks.
 * NOTE: These tests do NOT start a real browser. They only verify the
 * manager's state logic without a driver present.
 */
@DisplayName("SessionManager")
class SessionManagerTest {

    private final SessionManager sm = SessionManager.getInstance();

    @Test
    @DisplayName("getInstance() always returns same instance")
    void singleton() {
        assertSame(SessionManager.getInstance(), SessionManager.getInstance());
    }

    @Test
    @DisplayName("No session is active by default (no browser launched)")
    void notActiveByDefault() {
        // In test context, no driver has been started
        assertFalse(sm.isActive());
    }

    @Test
    @DisplayName("getDriverOrThrow() throws when no session is active")
    void getDriverOrThrowWhenInactive() {
        if (!sm.isActive()) {
            assertThrows(NoActiveSessionException.class, sm::getDriverOrThrow);
        }
    }

    @Test
    @DisplayName("getSessionId() returns null when no session is active")
    void sessionIdNullWhenInactive() {
        if (!sm.isActive()) {
            assertNull(sm.getSessionId());
        }
    }

    @Test
    @DisplayName("getSessionInfo() returns inactive map when no session")
    void sessionInfoInactive() {
        if (!sm.isActive()) {
            var info = sm.getSessionInfo();
            assertEquals(false, info.get("active"));
        }
    }

    @Test
    @DisplayName("shutdown() is safe to call when no session is active")
    void shutdownWhenInactive() {
        assertDoesNotThrow(sm::shutdown);
    }
}

