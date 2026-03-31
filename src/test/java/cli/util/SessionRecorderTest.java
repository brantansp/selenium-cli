package cli.util;

import cli.config.BrowserConfig;
import cli.model.CommandRequest;
import cli.model.SessionFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionRecorder} — recording, exclusion, save, and reset.
 * Uses the singleton instance, so tests reset state in @BeforeEach / @AfterEach.
 */
@DisplayName("SessionRecorder")
class SessionRecorderTest {

    private final SessionRecorder recorder = SessionRecorder.getInstance();

    @BeforeEach
    void setUp() {
        recorder.reset();
        // Also reset BrowserConfig so toMap() returns defaults
        BrowserConfig.getInstance().reset();
    }

    @AfterEach
    void tearDown() {
        recorder.reset();
        BrowserConfig.getInstance().reset();
    }

    // ── Enable / Disable ────────────────────────────────────────

    @Test
    @DisplayName("Recording is enabled by default")
    void enabledByDefault() {
        assertTrue(recorder.isEnabled());
    }

    @Test
    @DisplayName("disable() prevents recording")
    void disablePreventsRecording() {
        recorder.disable();
        recorder.record(new String[]{"open", "https://google.com"});
        assertFalse(recorder.hasRecords());
    }

    @Test
    @DisplayName("enable() resumes recording after disable")
    void enableResumesRecording() {
        recorder.disable();
        recorder.record(new String[]{"open", "https://google.com"});
        recorder.enable();
        recorder.record(new String[]{"click", "#btn"});
        assertEquals(1, recorder.getCommands().size());
        assertEquals("click", recorder.getCommands().get(0).getCommand());
    }

    // ── Record behaviour ────────────────────────────────────────

    @Test
    @DisplayName("record() captures command and args")
    void recordCapturesCommandAndArgs() {
        recorder.record(new String[]{"type", "#email", "user@test.com"});
        assertEquals(1, recorder.getCommands().size());

        CommandRequest req = recorder.getCommands().get(0);
        assertEquals("type", req.getCommand());
        assertEquals(List.of("#email", "user@test.com"), req.getArgs());
    }

    @Test
    @DisplayName("record() stores command name in lowercase")
    void recordLowercases() {
        recorder.record(new String[]{"OPEN", "https://google.com"});
        assertEquals("open", recorder.getCommands().get(0).getCommand());
    }

    @Test
    @DisplayName("record() handles command with no args")
    void recordNoArgs() {
        recorder.record(new String[]{"url"});
        CommandRequest req = recorder.getCommands().get(0);
        assertEquals("url", req.getCommand());
        assertTrue(req.getArgs().isEmpty());
    }

    @Test
    @DisplayName("record() ignores null tokens")
    void recordIgnoresNull() {
        recorder.record(null);
        assertFalse(recorder.hasRecords());
    }

    @Test
    @DisplayName("record() ignores empty tokens array")
    void recordIgnoresEmpty() {
        recorder.record(new String[]{});
        assertFalse(recorder.hasRecords());
    }

    // ── Excluded (meta) commands ────────────────────────────────

    @Test
    @DisplayName("Meta commands are excluded from recording")
    void metaCommandsExcluded() {
        String[] excluded = {"help", "exit", "session", "run", "history", "--help", "-h"};
        for (String cmd : excluded) {
            recorder.record(new String[]{cmd});
        }
        assertFalse(recorder.hasRecords(), "None of the meta commands should be recorded");
    }

    @Test
    @DisplayName("Meta command exclusion is case-insensitive")
    void metaCommandsCaseInsensitive() {
        recorder.record(new String[]{"HELP"});
        recorder.record(new String[]{"Exit"});
        recorder.record(new String[]{"SESSION"});
        assertFalse(recorder.hasRecords());
    }

    @Test
    @DisplayName("Non-meta commands ARE recorded")
    void nonMetaCommandsRecorded() {
        recorder.record(new String[]{"open", "https://google.com"});
        recorder.record(new String[]{"click", "#btn"});
        recorder.record(new String[]{"quit"});
        assertEquals(3, recorder.getCommands().size());
    }

    // ── getCommands() immutability ──────────────────────────────

    @Test
    @DisplayName("getCommands() returns unmodifiable list")
    void getCommandsUnmodifiable() {
        recorder.record(new String[]{"open", "https://google.com"});
        assertThrows(UnsupportedOperationException.class,
                () -> recorder.getCommands().add(new CommandRequest("hack", List.of())));
    }

    // ── Reset ───────────────────────────────────────────────────

    @Test
    @DisplayName("reset() clears commands and re-enables recording")
    void resetClearsAll() {
        recorder.disable();
        recorder.enable();
        recorder.record(new String[]{"open", "https://google.com"});
        assertTrue(recorder.hasRecords());

        recorder.reset();
        assertFalse(recorder.hasRecords());
        assertTrue(recorder.isEnabled());
    }

    // ── Save to file ────────────────────────────────────────────

    @Test
    @DisplayName("save() writes SessionFile JSON with config and numbered steps")
    void saveWritesSessionFileFormat() throws IOException {
        recorder.record(new String[]{"open", "https://google.com"});
        recorder.record(new String[]{"click", "#btn"});

        Path tempFile = Files.createTempFile("session-test-", ".json");
        try {
            recorder.save(tempFile);
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);

            // Parse and validate structure
            JsonObject root = JsonOutput.gson().fromJson(content, JsonObject.class);
            assertNotNull(root.get("config"), "Should have config section");
            assertNotNull(root.get("commands"), "Should have commands section");

            // Validate config section has expected keys
            JsonObject config = root.getAsJsonObject("config");
            assertTrue(config.has("headless"));
            assertTrue(config.has("maximize"));
            assertTrue(config.has("incognito"));
            assertTrue(config.has("pageLoadStrategy"));

            // Validate commands have step numbers
            JsonArray commands = root.getAsJsonArray("commands");
            assertEquals(2, commands.size());

            JsonObject step1 = commands.get(0).getAsJsonObject();
            assertEquals(1, step1.get("step").getAsInt());
            assertEquals("open", step1.get("command").getAsString());
            assertEquals("https://google.com", step1.getAsJsonArray("args").get(0).getAsString());

            JsonObject step2 = commands.get(1).getAsJsonObject();
            assertEquals(2, step2.get("step").getAsInt());
            assertEquals("click", step2.get("command").getAsString());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("save() captures BrowserConfig snapshot in config section")
    void saveIncludesConfigSnapshot() throws IOException {
        BrowserConfig.getInstance().headless(true).windowSize("1920x1080");
        recorder.record(new String[]{"open", "https://example.com"});

        Path tempFile = Files.createTempFile("session-config-", ".json");
        try {
            recorder.save(tempFile);
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);

            JsonObject root = JsonOutput.gson().fromJson(content, JsonObject.class);
            JsonObject config = root.getAsJsonObject("config");
            assertTrue(config.get("headless").getAsBoolean());
            assertEquals("1920x1080", config.get("windowSize").getAsString());
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("save() default filename includes timestamp pattern")
    void saveDefaultFilename() throws IOException {
        recorder.record(new String[]{"url"});
        Path saved = recorder.save();
        try {
            assertTrue(Files.exists(saved));
            assertTrue(saved.getFileName().toString().matches("session-\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.json"));
        } finally {
            Files.deleteIfExists(saved);
        }
    }
}

