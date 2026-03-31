package cli.model;

import cli.util.JsonOutput;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CommandResult} — the universal JSON envelope.
 * Every single command in the CLI returns output through this class,
 * so its correctness is critical.
 */
@DisplayName("CommandResult")
class CommandResultTest {

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("success() sets status to 'success'")
        void successStatus() {
            CommandResult result = CommandResult.success("open", List.of("https://google.com"), "OK");
            assertEquals("success", result.getStatus());
        }

        @Test
        @DisplayName("success() stores command name")
        void successCommand() {
            CommandResult result = CommandResult.success("click", List.of("#btn"), "clicked");
            assertEquals("click", result.getCommand());
        }

        @Test
        @DisplayName("success() stores args")
        void successArgs() {
            CommandResult result = CommandResult.success("type", List.of("#email", "user@test.com"), null);
            assertEquals(List.of("#email", "user@test.com"), result.getArgs());
        }

        @Test
        @DisplayName("success() stores result object")
        void successResult() {
            CommandResult result = CommandResult.success("gettext", List.of("h1"), "Hello World");
            assertEquals("Hello World", result.getResult());
        }

        @Test
        @DisplayName("success() has null error")
        void successNullError() {
            CommandResult result = CommandResult.success("url", Collections.emptyList(), "https://example.com");
            assertNull(result.getError());
        }

        @Test
        @DisplayName("error() sets status to 'error'")
        void errorStatus() {
            CommandResult result = CommandResult.error("click", List.of("#missing"), "Element not found");
            assertEquals("error", result.getStatus());
        }

        @Test
        @DisplayName("error() stores error message")
        void errorMessage() {
            CommandResult result = CommandResult.error("open", List.of("bad-url"), "Invalid URL");
            assertEquals("Invalid URL", result.getError());
        }

        @Test
        @DisplayName("error() has null result")
        void errorNullResult() {
            CommandResult result = CommandResult.error("click", List.of("#x"), "not found");
            assertNull(result.getResult());
        }
    }

    @Nested
    @DisplayName("Metadata fields")
    class Metadata {

        @Test
        @DisplayName("timestamp is set automatically")
        void timestampSet() {
            CommandResult result = CommandResult.success("url", Collections.emptyList(), "x");
            assertNotNull(result.getTimestamp());
            assertFalse(result.getTimestamp().isEmpty());
        }

        @Test
        @DisplayName("sessionId is null when no session is active")
        void sessionIdNullWhenInactive() {
            // No browser session started in tests
            CommandResult result = CommandResult.success("url", Collections.emptyList(), "x");
            assertNull(result.getSessionId());
        }
    }

    @Nested
    @DisplayName("JSON serialisation")
    class JsonSerialization {

        @Test
        @DisplayName("Success result serialises to valid JSON with expected fields")
        void successToJson() {
            CommandResult result = CommandResult.success("open",
                    List.of("https://google.com"), "https://www.google.com/");
            String json = JsonOutput.toJson(result);

            JsonObject obj = JsonOutput.gson().fromJson(json, JsonObject.class);
            assertEquals("success", obj.get("status").getAsString());
            assertEquals("open", obj.get("command").getAsString());
            assertEquals("https://www.google.com/", obj.get("result").getAsString());
            assertTrue(obj.has("timestamp"));
            assertTrue(obj.has("args"));
        }

        @Test
        @DisplayName("Error result serialises with error field")
        void errorToJson() {
            CommandResult result = CommandResult.error("click", List.of("#x"), "not found");
            String json = JsonOutput.toJson(result);

            JsonObject obj = JsonOutput.gson().fromJson(json, JsonObject.class);
            assertEquals("error", obj.get("status").getAsString());
            assertEquals("not found", obj.get("error").getAsString());
        }

        @Test
        @DisplayName("print() writes JSON to stdout")
        void printWritesToStdout() {
            CommandResult result = CommandResult.success("title", Collections.emptyList(), "My Page");

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream original = System.out;
            System.setOut(new java.io.PrintStream(baos));
            try {
                result.print();
            } finally {
                System.setOut(original);
            }

            String output = baos.toString();
            assertTrue(output.contains("\"status\": \"success\""));
            assertTrue(output.contains("\"command\": \"title\""));
            assertTrue(output.contains("My Page"));
        }
    }
}

