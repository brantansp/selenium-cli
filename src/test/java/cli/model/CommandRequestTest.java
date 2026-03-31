package cli.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CommandRequest} — the POJO used in batch/session files.
 */
@DisplayName("CommandRequest")
class CommandRequestTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        @DisplayName("No-arg constructor creates empty request")
        void noArg() {
            CommandRequest req = new CommandRequest();
            assertNull(req.getStep());
            assertNull(req.getCommand());
            assertNull(req.getArgs());
        }

        @Test
        @DisplayName("Two-arg constructor sets command and args, step is null")
        void twoArg() {
            CommandRequest req = new CommandRequest("open", List.of("https://google.com"));
            assertNull(req.getStep());
            assertEquals("open", req.getCommand());
            assertEquals(List.of("https://google.com"), req.getArgs());
        }

        @Test
        @DisplayName("Three-arg constructor sets step, command, and args")
        void threeArg() {
            CommandRequest req = new CommandRequest(5, "click", List.of("#btn"));
            assertEquals(5, req.getStep());
            assertEquals("click", req.getCommand());
            assertEquals(List.of("#btn"), req.getArgs());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setStep / getStep round-trip")
        void stepRoundTrip() {
            CommandRequest req = new CommandRequest();
            req.setStep(10);
            assertEquals(10, req.getStep());
        }

        @Test
        @DisplayName("setCommand / getCommand round-trip")
        void commandRoundTrip() {
            CommandRequest req = new CommandRequest();
            req.setCommand("navigate");
            assertEquals("navigate", req.getCommand());
        }

        @Test
        @DisplayName("setArgs / getArgs round-trip")
        void argsRoundTrip() {
            CommandRequest req = new CommandRequest();
            req.setArgs(List.of("back"));
            assertEquals(List.of("back"), req.getArgs());
        }
    }
}

