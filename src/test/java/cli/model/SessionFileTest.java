package cli.model;

import cli.util.JsonOutput;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionFile} — the wrapper for session recording JSON files.
 * Validates both POJO behaviour and JSON round-trip serialisation,
 * ensuring the session file format is stable for replay with {@code run --json}.
 */
@DisplayName("SessionFile")
class SessionFileTest {

    @Nested
    @DisplayName("POJO behaviour")
    class PojoBehaviour {

        @Test
        @DisplayName("No-arg constructor creates empty SessionFile")
        void noArg() {
            SessionFile sf = new SessionFile();
            assertNull(sf.getConfig());
            assertNull(sf.getCommands());
        }

        @Test
        @DisplayName("Two-arg constructor sets config and commands")
        void twoArg() {
            Map<String, Object> config = Map.of("headless", true);
            List<CommandRequest> commands = List.of(new CommandRequest(1, "open", List.of("url")));
            SessionFile sf = new SessionFile(config, commands);

            assertEquals(config, sf.getConfig());
            assertEquals(1, sf.getCommands().size());
        }

        @Test
        @DisplayName("Setters update fields")
        void setters() {
            SessionFile sf = new SessionFile();
            sf.setConfig(Map.of("maximize", false));
            sf.setCommands(List.of(new CommandRequest("quit", List.of())));

            assertNotNull(sf.getConfig());
            assertEquals(1, sf.getCommands().size());
        }
    }

    @Nested
    @DisplayName("JSON serialisation")
    class JsonSerialization {

        @Test
        @DisplayName("SessionFile serialises with config and commands")
        void serializesCorrectly() {
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("headless", false);
            config.put("maximize", true);
            config.put("windowSize", "1920x1080");

            List<CommandRequest> commands = List.of(
                    new CommandRequest(1, "open", List.of("https://google.com")),
                    new CommandRequest(2, "click", List.of("#submit"))
            );

            SessionFile sf = new SessionFile(config, commands);
            String json = JsonOutput.toJson(sf);

            // Validate structure
            JsonObject root = JsonOutput.gson().fromJson(json, JsonObject.class);
            assertTrue(root.has("config"));
            assertTrue(root.has("commands"));

            // Config
            JsonObject cfgObj = root.getAsJsonObject("config");
            assertFalse(cfgObj.get("headless").getAsBoolean());
            assertTrue(cfgObj.get("maximize").getAsBoolean());
            assertEquals("1920x1080", cfgObj.get("windowSize").getAsString());

            // Commands with step numbers
            JsonArray cmds = root.getAsJsonArray("commands");
            assertEquals(2, cmds.size());
            assertEquals(1, cmds.get(0).getAsJsonObject().get("step").getAsInt());
            assertEquals("open", cmds.get(0).getAsJsonObject().get("command").getAsString());
            assertEquals(2, cmds.get(1).getAsJsonObject().get("step").getAsInt());
        }

        @Test
        @DisplayName("SessionFile deserialises from JSON")
        void deserializesCorrectly() {
            String json = """
                    {
                      "config": { "headless": true, "windowSize": "1366x768" },
                      "commands": [
                        { "step": 1, "command": "open", "args": ["https://example.com"] },
                        { "step": 2, "command": "screenshot", "args": ["page.png"] }
                      ]
                    }
                    """;

            SessionFile sf = JsonOutput.gson().fromJson(json, SessionFile.class);
            assertNotNull(sf.getConfig());
            assertTrue((Boolean) sf.getConfig().get("headless"));
            assertEquals(2, sf.getCommands().size());
            assertEquals(1, sf.getCommands().get(0).getStep());
            assertEquals("open", sf.getCommands().get(0).getCommand());
            assertEquals(List.of("https://example.com"), sf.getCommands().get(0).getArgs());
        }

        @Test
        @DisplayName("CommandRequest without step omits step in JSON (Gson default)")
        void stepOmittedWhenNull() {
            CommandRequest req = new CommandRequest("open", List.of("url"));
            String json = JsonOutput.toJson(req);
            assertFalse(json.contains("\"step\""), "Null step should be omitted");
        }

        @Test
        @DisplayName("CommandRequest with step includes step in JSON")
        void stepIncludedWhenSet() {
            CommandRequest req = new CommandRequest(3, "click", List.of("#btn"));
            String json = JsonOutput.toJson(req);
            assertTrue(json.contains("\"step\": 3"), "Step should be present");
        }

        @Test
        @DisplayName("Simple array format (legacy) deserialises as List<CommandRequest>")
        void legacyArrayFormat() {
            String json = """
                    [
                      { "command": "open", "args": ["https://google.com"] },
                      { "command": "quit", "args": [] }
                    ]
                    """;
            java.lang.reflect.Type listType =
                    new com.google.gson.reflect.TypeToken<List<CommandRequest>>() {}.getType();
            List<CommandRequest> requests = JsonOutput.gson().fromJson(json, listType);

            assertEquals(2, requests.size());
            assertEquals("open", requests.get(0).getCommand());
            assertNull(requests.get(0).getStep());
            assertEquals("quit", requests.get(1).getCommand());
        }
    }
}

