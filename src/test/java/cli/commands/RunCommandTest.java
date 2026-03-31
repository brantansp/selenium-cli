package cli.commands;

import cli.config.BrowserConfig;
import cli.model.SessionFile;
import cli.util.JsonOutput;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RunCommand} — specifically the session file format detection
 * and config application logic. Does NOT execute actual browser commands.
 */
@DisplayName("RunCommand")
class RunCommandTest {

    @BeforeEach
    void setUp() {
        BrowserConfig.getInstance().reset();
    }

    @AfterEach
    void tearDown() {
        BrowserConfig.getInstance().reset();
    }

    @Nested
    @DisplayName("Session file format detection")
    class FormatDetection {

        @Test
        @DisplayName("JSON object is detected as session file format")
        void jsonObjectIsSessionFile() {
            String json = """
                    {
                      "config": { "headless": true },
                      "commands": [
                        { "step": 1, "command": "open", "args": ["https://example.com"] }
                      ]
                    }
                    """;
            JsonElement root = JsonOutput.gson().fromJson(json, JsonElement.class);
            assertTrue(root.isJsonObject(), "Session file format should be detected as JSON object");
        }

        @Test
        @DisplayName("JSON array is detected as legacy format")
        void jsonArrayIsLegacy() {
            String json = """
                    [
                      { "command": "open", "args": ["https://example.com"] }
                    ]
                    """;
            JsonElement root = JsonOutput.gson().fromJson(json, JsonElement.class);
            assertTrue(root.isJsonArray(), "Legacy format should be detected as JSON array");
        }

        @Test
        @DisplayName("Session file deserialises with config and commands")
        void sessionFileDeserialization() {
            String json = """
                    {
                      "config": {
                        "headless": true,
                        "maximize": false,
                        "windowSize": "1920x1080",
                        "pageLoadStrategy": "normal"
                      },
                      "commands": [
                        { "step": 1, "command": "open", "args": ["https://google.com"] },
                        { "step": 2, "command": "screenshot", "args": ["page.png"] }
                      ]
                    }
                    """;
            SessionFile sf = JsonOutput.gson().fromJson(json, SessionFile.class);
            assertNotNull(sf.getConfig());
            assertNotNull(sf.getCommands());
            assertEquals(2, sf.getCommands().size());
            assertEquals(true, sf.getConfig().get("headless"));
        }
    }

    @Nested
    @DisplayName("Config round-trip (toMap → JSON → apply)")
    class ConfigRoundTrip {

        @Test
        @DisplayName("BrowserConfig.toMap() → serialise → deserialise preserves all fields")
        void configRoundTrip() {
            BrowserConfig config = BrowserConfig.getInstance();
            config.headless(true).maximize(true).incognito(true)
                    .windowSize("1366x768")
                    .proxyUrl("http://proxy:8080")
                    .browserVersion("131")
                    .addHeader("Authorization", "Bearer token")
                    .addArgument("--disable-extensions");

            // Serialise
            Map<String, Object> original = config.toMap();
            String json = JsonOutput.toJson(original);

            // Deserialise
            JsonObject parsed = JsonOutput.gson().fromJson(json, JsonObject.class);

            // Verify key fields survived round-trip
            assertTrue(parsed.get("headless").getAsBoolean());
            assertTrue(parsed.get("maximize").getAsBoolean());
            assertTrue(parsed.get("incognito").getAsBoolean());
            assertEquals("1366x768", parsed.get("windowSize").getAsString());
            assertEquals("http://proxy:8080", parsed.get("proxyUrl").getAsString());
            assertEquals("131", parsed.get("browserVersion").getAsString());
            assertEquals("normal", parsed.get("pageLoadStrategy").getAsString());
            assertTrue(parsed.getAsJsonObject("extraHeaders").has("Authorization"));
            assertEquals(1, parsed.getAsJsonArray("rawArguments").size());
        }

        @Test
        @DisplayName("Session file produced by SessionRecorder contains config + numbered steps")
        void sessionRecorderOutput() {
            // Simulate what SessionRecorder.buildSessionFile() produces
            BrowserConfig.getInstance().headless(true).windowSize("800x600");

            Map<String, Object> configMap = BrowserConfig.getInstance().toMap();
            var commands = List.of(
                    new cli.model.CommandRequest(1, "open", List.of("https://example.com")),
                    new cli.model.CommandRequest(2, "quit", List.of())
            );
            SessionFile sf = new SessionFile(configMap, commands);
            String json = JsonOutput.toJson(sf);

            // Verify the full structure
            JsonObject root = JsonOutput.gson().fromJson(json, JsonObject.class);
            assertTrue(root.has("config"));
            assertTrue(root.has("commands"));
            assertEquals(2, root.getAsJsonArray("commands").size());
            assertEquals(1, root.getAsJsonArray("commands").get(0)
                    .getAsJsonObject().get("step").getAsInt());
            assertTrue(root.getAsJsonObject("config").get("headless").getAsBoolean());
        }
    }
}

