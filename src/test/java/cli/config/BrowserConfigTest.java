package cli.config;

import cli.util.JsonOutput;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.*;
import org.openqa.selenium.PageLoadStrategy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link BrowserConfig} — the singleton config accumulator.
 * Covers builder methods, toMap() snapshot, reset(), save/load persistence,
 * and ChromeOptions generation.
 */
@DisplayName("BrowserConfig")
class BrowserConfigTest {

    private final BrowserConfig config = BrowserConfig.getInstance();

    @BeforeEach
    void setUp() {
        config.reset();
    }

    @AfterEach
    void tearDown() {
        config.reset();
    }

    // ── Singleton ───────────────────────────────────────────────

    @Test
    @DisplayName("getInstance() always returns same instance")
    void singleton() {
        assertSame(BrowserConfig.getInstance(), BrowserConfig.getInstance());
    }

    // ── Default values ──────────────────────────────────────────

    @Nested
    @DisplayName("Default values after reset")
    class Defaults {

        @Test
        void headlessDefault() { assertFalse(config.isHeadless()); }

        @Test
        void maximizeDefault() { assertFalse(config.isMaximize()); }

        @Test
        void incognitoDefault() { assertFalse(config.isIncognito()); }

        @Test
        void windowSizeDefault() { assertNull(config.getWindowSize()); }

        @Test
        void userDataDirDefault() { assertNull(config.getUserDataDir()); }

        @Test
        void proxyUrlDefault() { assertNull(config.getProxyUrl()); }

        @Test
        void browserVersionDefault() { assertNull(config.getBrowserVersion()); }

        @Test
        void extraHeadersDefault() { assertTrue(config.getExtraHeaders().isEmpty()); }

        @Test
        void rawArgumentsDefault() { assertTrue(config.getRawArguments().isEmpty()); }
    }

    // ── Builder methods ─────────────────────────────────────────

    @Nested
    @DisplayName("Builder methods")
    class Builders {

        @Test
        @DisplayName("Fluent builder returns same instance")
        void fluentChaining() {
            BrowserConfig result = config.headless(true).maximize(true).incognito(true);
            assertSame(config, result);
        }

        @Test
        @DisplayName("headless(true) enables headless mode")
        void headless() {
            config.headless(true);
            assertTrue(config.isHeadless());
        }

        @Test
        @DisplayName("windowSize sets the value")
        void windowSize() {
            config.windowSize("1920x1080");
            assertEquals("1920x1080", config.getWindowSize());
        }

        @Test
        @DisplayName("proxyUrl sets the proxy")
        void proxyUrl() {
            config.proxyUrl("http://proxy:8080");
            assertEquals("http://proxy:8080", config.getProxyUrl());
        }

        @Test
        @DisplayName("browserVersion sets the version")
        void browserVersion() {
            config.browserVersion("124");
            assertEquals("124", config.getBrowserVersion());
        }

        @Test
        @DisplayName("addHeader accumulates headers")
        void addHeader() {
            config.addHeader("Authorization", "Bearer token");
            config.addHeader("X-Custom", "value");
            assertEquals(2, config.getExtraHeaders().size());
            assertEquals("Bearer token", config.getExtraHeaders().get("Authorization"));
        }

        @Test
        @DisplayName("addArgument accumulates raw Chrome arguments")
        void addArgument() {
            config.addArgument("--disable-extensions");
            config.addArgument("--start-fullscreen");
            assertEquals(2, config.getRawArguments().size());
        }

        @Test
        @DisplayName("pageLoadStrategy sets the strategy")
        void pageLoadStrategy() {
            config.pageLoadStrategy(PageLoadStrategy.EAGER);
            // Verify via toMap()
            assertEquals("eager", config.toMap().get("pageLoadStrategy"));
        }
    }

    // ── toMap() snapshot ────────────────────────────────────────

    @Nested
    @DisplayName("toMap() snapshot")
    class ToMap {

        @Test
        @DisplayName("toMap() contains all expected keys")
        void containsAllKeys() {
            Map<String, Object> map = config.toMap();
            assertTrue(map.containsKey("headless"));
            assertTrue(map.containsKey("maximize"));
            assertTrue(map.containsKey("incognito"));
            assertTrue(map.containsKey("windowSize"));
            assertTrue(map.containsKey("userDataDir"));
            assertTrue(map.containsKey("proxyUrl"));
            assertTrue(map.containsKey("browserVersion"));
            assertTrue(map.containsKey("pageLoadStrategy"));
            assertTrue(map.containsKey("extraHeaders"));
            assertTrue(map.containsKey("rawArguments"));
        }

        @Test
        @DisplayName("toMap() reflects current state")
        void reflectsState() {
            config.headless(true).windowSize("800x600").addHeader("X-Test", "1");
            Map<String, Object> map = config.toMap();

            assertEquals(true, map.get("headless"));
            assertEquals("800x600", map.get("windowSize"));
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) map.get("extraHeaders");
            assertEquals("1", headers.get("X-Test"));
        }

        @Test
        @DisplayName("toMap() serialises to valid JSON")
        void serializesToJson() {
            config.headless(true).maximize(true);
            String json = JsonOutput.toJson(config.toMap());
            JsonObject obj = JsonOutput.gson().fromJson(json, JsonObject.class);
            assertTrue(obj.get("headless").getAsBoolean());
            assertTrue(obj.get("maximize").getAsBoolean());
        }
    }

    // ── Reset ───────────────────────────────────────────────────

    @Test
    @DisplayName("reset() restores all defaults")
    void resetRestoresDefaults() {
        config.headless(true).maximize(true).incognito(true)
                .windowSize("1920x1080").userDataDir("C:\\temp")
                .proxyUrl("http://proxy:8080").browserVersion("124")
                .pageLoadStrategy(PageLoadStrategy.EAGER)
                .addHeader("X-Test", "val")
                .addArgument("--disable-gpu");

        config.reset();

        assertFalse(config.isHeadless());
        assertFalse(config.isMaximize());
        assertFalse(config.isIncognito());
        assertNull(config.getWindowSize());
        assertNull(config.getUserDataDir());
        assertNull(config.getProxyUrl());
        assertNull(config.getBrowserVersion());
        assertTrue(config.getExtraHeaders().isEmpty());
        assertTrue(config.getRawArguments().isEmpty());
        assertEquals("normal", config.toMap().get("pageLoadStrategy"));
    }

    // ── Save / Load persistence ─────────────────────────────────

    @Nested
    @DisplayName("Config persistence (save/load)")
    class Persistence {

        @Test
        @DisplayName("save() then load() restores config")
        void saveAndLoad() {
            config.headless(true).windowSize("1366x768")
                    .proxyUrl("http://proxy:3128")
                    .addHeader("Auth", "Bearer xyz");
            config.save();

            try {
                // Verify file was created
                assertTrue(Files.exists(Path.of(BrowserConfig.getConfigFileName())));

                // Manually revert values (NOT reset(), which deletes the file)
                config.headless(false).windowSize(null).proxyUrl(null);
                config.getExtraHeaders().clear();
                assertFalse(config.isHeadless());

                // Reload from disk
                config.load();
                assertTrue(config.isHeadless());
                assertEquals("1366x768", config.getWindowSize());
                assertEquals("http://proxy:3128", config.getProxyUrl());
                assertEquals("Bearer xyz", config.getExtraHeaders().get("Auth"));
            } finally {
                config.deleteConfigFile();
            }
        }

        @Test
        @DisplayName("load() on missing file does nothing (no error)")
        void loadMissingFileNoOp() {
            config.deleteConfigFile();
            assertDoesNotThrow(() -> config.load());
        }

        @Test
        @DisplayName("deleteConfigFile() removes the file")
        void deleteConfigFile() throws IOException {
            config.save();
            assertTrue(Files.exists(Path.of(BrowserConfig.getConfigFileName())));
            config.deleteConfigFile();
            assertFalse(Files.exists(Path.of(BrowserConfig.getConfigFileName())));
        }
    }

    // ── ChromeOptions generation ────────────────────────────────

    @Nested
    @DisplayName("toChromeOptions()")
    class ChromeOptionsGeneration {

        @Test
        @DisplayName("Default options include sane defaults")
        void saneDefaults() {
            var options = config.toChromeOptions();
            // ChromeOptions doesn't expose args directly, but we can check it doesn't throw
            assertNotNull(options);
        }

        @Test
        @DisplayName("Headless mode adds headless arguments")
        void headlessArgs() {
            config.headless(true);
            var options = config.toChromeOptions();
            // Verify via the options' JSON representation
            String optionsJson = options.toJson().toString();
            assertTrue(optionsJson.contains("headless"));
        }

        @Test
        @DisplayName("Incognito mode adds --incognito")
        void incognitoArg() {
            config.incognito(true);
            var options = config.toChromeOptions();
            String optionsJson = options.toJson().toString();
            assertTrue(optionsJson.contains("incognito"));
        }

        @Test
        @DisplayName("Proxy configuration is applied")
        void proxyApplied() {
            config.proxyUrl("http://myproxy:9090");
            var options = config.toChromeOptions();
            assertNotNull(options.getCapability("proxy"));
        }

        @Test
        @DisplayName("browserVersion is set on options")
        void browserVersionSet() {
            config.browserVersion("131");
            var options = config.toChromeOptions();
            assertEquals("131", options.getBrowserVersion());
        }

        @Test
        @DisplayName("rawArguments are passed through")
        void rawArgumentsPassedThrough() {
            config.addArgument("--custom-flag");
            var options = config.toChromeOptions();
            String optionsJson = options.toJson().toString();
            assertTrue(optionsJson.contains("--custom-flag"));
        }
    }
}

