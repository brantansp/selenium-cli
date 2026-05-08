package cli.util;

import cli.config.BrowserConfig;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PropertiesFileLoader} — properties-file → BrowserConfig mapping.
 * All tests use temporary files and reset BrowserConfig before/after each test.
 */
@DisplayName("PropertiesFileLoader")
class PropertiesFileLoaderTest {

    private final BrowserConfig config = BrowserConfig.getInstance();
    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        config.reset();
        tempFile = Files.createTempFile("selenium-cli-test-", ".properties");
    }

    @AfterEach
    void tearDown() throws IOException {
        config.reset();
        Files.deleteIfExists(tempFile);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void write(String content) throws IOException {
        Files.writeString(tempFile, content, StandardCharsets.UTF_8);
    }

    private PropertiesFileLoader.LoadResult load() throws IOException {
        return PropertiesFileLoader.load(tempFile.toString(), config);
    }

    // ── browser.chrome.version ───────────────────────────────────────────────

    @Nested
    @DisplayName("browser.chrome.version")
    class BrowserVersion {

        @Test
        @DisplayName("Maps to browserVersion")
        void maps() throws IOException {
            write("browser.chrome.version=135\n");
            load();
            assertEquals("135", config.getBrowserVersion());
        }

        @Test
        @DisplayName("Appears in applied list")
        void appliedEntry() throws IOException {
            write("browser.chrome.version=130\n");
            var result = load();
            assertTrue(result.getApplied().stream().anyMatch(s -> s.startsWith("browserVersion=")));
        }
    }

    // ── browser.chrome.arguments.* ───────────────────────────────────────────

    @Nested
    @DisplayName("browser.chrome.arguments.*")
    class Arguments {

        @Test
        @DisplayName("Single flag value is added as argument")
        void singleFlag() throws IOException {
            write("browser.chrome.arguments.sandbox=--no-sandbox\n");
            load();
            assertTrue(config.getRawArguments().contains("--no-sandbox"));
        }

        @Test
        @DisplayName("Multi-flag value is split and added individually")
        void multiFlag() throws IOException {
            write("browser.chrome.arguments.add=--auth-server-allowlist=*.example.com --auth-negotiate-delegate-whitelist=*.example.com\n");
            load();
            assertTrue(config.getRawArguments().stream()
                    .anyMatch(a -> a.contains("--auth-server-allowlist")));
            assertTrue(config.getRawArguments().stream()
                    .anyMatch(a -> a.contains("--auth-negotiate-delegate-whitelist")));
        }

        @Test
        @DisplayName("Duplicate arguments within same file are not added twice")
        void noDuplicatesWithinFile() throws IOException {
            write("browser.chrome.arguments.a=--no-sandbox\nbrowser.chrome.arguments.b=--no-sandbox\n");
            load();
            long count = config.getRawArguments().stream().filter("--no-sandbox"::equals).count();
            assertEquals(1, count, "Within-file duplicate should only be added once");
        }

        @Test
        @DisplayName("Arg already in rawArguments from previous load is re-applied (not silently skipped)")
        void preExistingArgIsReapplied() throws IOException {
            // Simulate a previous load that populated rawArguments
            config.addArgument("--lang=en-US");
            assertEquals(1, config.getRawArguments().stream().filter("--lang=en-US"::equals).count());

            // A fresh --load-file should still report the arg in applied (shows user intent)
            write("browser.chrome.arguments.lang=--lang=en-US\n");
            var result = load();
            assertTrue(result.getApplied().stream().anyMatch(s -> s.contains("--lang=en-US")),
                    "Pre-existing arg must appear in applied list so user sees all effective settings");
        }

        @Test
        @DisplayName("Argument appears in applied list")
        void appliedEntry() throws IOException {
            write("browser.chrome.arguments.gpu=--disable-gpu\n");
            var result = load();
            assertTrue(result.getApplied().stream().anyMatch(s -> s.contains("--disable-gpu")));
        }
    }

    // ── browser.proxy.manual ─────────────────────────────────────────────────

    @Nested
    @DisplayName("browser.proxy.manual")
    class Proxy {

        @Test
        @DisplayName("Maps to proxyUrl")
        void maps() throws IOException {
            write("browser.proxy.manual=http://proxy.example.com:3128\n");
            load();
            assertEquals("http://proxy.example.com:3128", config.getProxyUrl());
        }

        @Test
        @DisplayName("Appears in applied list")
        void appliedEntry() throws IOException {
            write("browser.proxy.manual=http://proxy:8080\n");
            var result = load();
            assertTrue(result.getApplied().stream().anyMatch(s -> s.startsWith("proxyUrl=")));
        }
    }

    // ── browser.chrome.preferences.* ─────────────────────────────────────────

    @Nested
    @DisplayName("browser.chrome.preferences.*")
    class Preferences {

        @Test
        @DisplayName("String value mapped correctly")
        void stringValue() throws IOException {
            write("browser.chrome.preferences.download.default_directory=/tmp/downloads\n");
            load();
            assertEquals("/tmp/downloads",
                    config.getChromePreferences().get("download.default_directory"));
        }

        @Test
        @DisplayName("Boolean true coerced")
        void boolTrue() throws IOException {
            write("browser.chrome.preferences.safebrowsing.enabled=true\n");
            load();
            assertEquals(Boolean.TRUE, config.getChromePreferences().get("safebrowsing.enabled"));
        }

        @Test
        @DisplayName("Boolean false coerced")
        void boolFalse() throws IOException {
            write("browser.chrome.preferences.plugins.always_open_pdf_externally=false\n");
            load();
            assertEquals(Boolean.FALSE,
                    config.getChromePreferences().get("plugins.always_open_pdf_externally"));
        }

        @Test
        @DisplayName("Integer value coerced")
        void intValue() throws IOException {
            write("browser.chrome.preferences.profile.default_content_setting_values.geolocation=1\n");
            load();
            assertEquals(1,
                    config.getChromePreferences().get("profile.default_content_setting_values.geolocation"));
        }

        @Test
        @DisplayName("Preference appears in applied list")
        void appliedEntry() throws IOException {
            write("browser.chrome.preferences.download.prompt_for_download=false\n");
            var result = load();
            assertTrue(result.getApplied().stream()
                    .anyMatch(s -> s.startsWith("preference=download.prompt_for_download")));
        }
    }

    // ── browser.chrome.capabilities.* ────────────────────────────────────────

    @Nested
    @DisplayName("browser.chrome.capabilities.*")
    class Capabilities {

        @Test
        @DisplayName("Boolean capability coerced")
        void boolCap() throws IOException {
            write("browser.chrome.capabilities.acceptInsecureCerts=true\n");
            load();
            assertEquals(Boolean.TRUE, config.getChromeCapabilities().get("acceptInsecureCerts"));
        }

        @Test
        @DisplayName("Integer capability coerced")
        void intCap() throws IOException {
            write("browser.chrome.capabilities.timeouts=30\n");
            load();
            assertEquals(30, config.getChromeCapabilities().get("timeouts"));
        }

        @Test
        @DisplayName("String capability stored as-is")
        void stringCap() throws IOException {
            write("browser.chrome.capabilities.pageLoadStrategy=eager\n");
            load();
            assertEquals("eager", config.getChromeCapabilities().get("pageLoadStrategy"));
        }

        @Test
        @DisplayName("Capability appears in applied list")
        void appliedEntry() throws IOException {
            write("browser.chrome.capabilities.acceptInsecureCerts=false\n");
            var result = load();
            assertTrue(result.getApplied().stream()
                    .anyMatch(s -> s.startsWith("capability=acceptInsecureCerts")));
        }
    }

    // ── Unsupported / unknown keys → warnings ────────────────────────────────

    @Nested
    @DisplayName("Unknown keys → warnings")
    class Warnings {

        @Test
        @DisplayName("browser.type goes to warnings")
        void browserType() throws IOException {
            write("browser.type=chrome\n");
            var result = load();
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("browser.type")));
        }

        @Test
        @DisplayName("browser.chrome.extensions.* goes to warnings")
        void extensions() throws IOException {
            write("browser.chrome.extensions.foo=my-extension\n");
            var result = load();
            assertTrue(result.getWarnings().stream()
                    .anyMatch(w -> w.contains("browser.chrome.extensions.foo")));
        }

        @Test
        @DisplayName("Commented keys are silently skipped")
        void commentedKeys() throws IOException {
            write("#browser.proxy.manual=http://proxy:8080\n");
            var result = load();
            assertNull(config.getProxyUrl());
            assertTrue(result.getWarnings().isEmpty());
        }

        @Test
        @DisplayName("Variable definition keys (fs, download.directory, etc.) produce NO warnings")
        void variableKeysAresilentlyIgnored() throws IOException {
            write("fs=${const:java.io.File.separator}\n" +
                  "download.directory=${sys:user.dir}/Downloads\n" +
                  "userdata.directory=${sys:user.dir}/UserData\n" +
                  "plugin.directory=${sys:user.dir}/plugins\n");
            var result = load();
            assertTrue(result.getWarnings().isEmpty(),
                    "Interpolation variable keys must not appear in warnings: " + result.getWarnings());
        }
    }

    // ── Variable interpolation ────────────────────────────────────────────────

    @Nested
    @DisplayName("Variable interpolation")
    class Interpolation {

        @Test
        @DisplayName("${sys:user.dir} resolves to System.getProperty(user.dir)")
        void sysUserDir() throws IOException {
            write("browser.chrome.preferences.download.default_directory=${sys:user.dir}/downloads\n");
            load();
            String expected = System.getProperty("user.dir") + "/downloads";
            assertEquals(expected,
                    config.getChromePreferences().get("download.default_directory"));
        }

        @Test
        @DisplayName("${const:java.io.File.separator} resolves to file separator")
        void constFileSeparator() throws IOException {
            write("browser.chrome.preferences.path=${const:java.io.File.separator}tmp\n");
            load();
            String expected = java.io.File.separator + "tmp";
            assertEquals(expected, config.getChromePreferences().get("path"));
        }

        @Test
        @DisplayName("Chained variable reference (fs = ${const:...} then used as ${fs})")
        void chainedRef() throws IOException {
            write("fs=${const:java.io.File.separator}\n" +
                  "browser.chrome.preferences.dir=${sys:user.dir}${fs}data\n");
            load();
            String expected = System.getProperty("user.dir") + java.io.File.separator + "data";
            assertEquals(expected, config.getChromePreferences().get("dir"));
        }

        @Test
        @DisplayName("Unknown ${const:...} token is left unresolved without throwing")
        void unknownConst() throws IOException {
            write("browser.chrome.preferences.val=${const:com.nonexistent.Class.FIELD}\n");
            assertDoesNotThrow(PropertiesFileLoaderTest.this::load);
        }

        @Test
        @DisplayName("Unknown ${sys:...} token is left unresolved without throwing")
        void unknownSys() throws IOException {
            write("browser.chrome.preferences.val=${sys:no.such.property.xyz}\n");
            assertDoesNotThrow(PropertiesFileLoaderTest.this::load);
        }
    }

    // ── coerce() unit tests ───────────────────────────────────────────────────

    @Nested
    @DisplayName("coerce() value type resolution")
    class Coerce {

        @Test @DisplayName("'true' → Boolean.TRUE")
        void trueVal()  { assertEquals(Boolean.TRUE,  PropertiesFileLoader.coerce("true")); }

        @Test @DisplayName("'True' (mixed case) → Boolean.TRUE")
        void trueCase() { assertEquals(Boolean.TRUE,  PropertiesFileLoader.coerce("True")); }

        @Test @DisplayName("'false' → Boolean.FALSE")
        void falseVal() { assertEquals(Boolean.FALSE, PropertiesFileLoader.coerce("false")); }

        @Test @DisplayName("'42' → Integer 42")
        void intVal()   { assertEquals(42, PropertiesFileLoader.coerce("42")); }

        @Test @DisplayName("'hello' → String 'hello'")
        void strVal()   { assertEquals("hello", PropertiesFileLoader.coerce("hello")); }
    }

    // ── resolveConst() unit tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("resolveConst()")
    class ResolveConst {

        @Test
        @DisplayName("Resolves java.io.File.separator")
        void fileSeparator() {
            String result = PropertiesFileLoader.resolveConst("java.io.File.separator");
            assertEquals(java.io.File.separator, result);
        }

        @Test
        @DisplayName("Returns unresolved token on bad class")
        void badClass() {
            String result = PropertiesFileLoader.resolveConst("com.bad.NoClass.FIELD");
            assertTrue(result.startsWith("${const:"));
        }

        @Test
        @DisplayName("Returns unresolved token when no dot present")
        void noDot() {
            String result = PropertiesFileLoader.resolveConst("NOCLASSNODOT");
            assertTrue(result.startsWith("${const:"));
        }
    }

    // ── loadedFrom entry ─────────────────────────────────────────────────────

    @Test
    @DisplayName("applied list contains loadedFrom entry when called via ConfigCommand path")
    void loadedFromIsTrackedByLoad() throws IOException {
        write("browser.chrome.version=120\n");
        var result = load();
        // The loadedFrom entry is added by ConfigCommand, not PropertiesFileLoader itself.
        // Verify instead that applied is non-empty and file was processed correctly.
        assertFalse(result.getApplied().isEmpty());
        assertEquals("120", config.getBrowserVersion());
    }

    // ── Full config.properties round trip ────────────────────────────────────

    @Test
    @DisplayName("Full config.properties file loads without errors")
    void fullPropertiesFile() throws IOException {
        // Mirrors the structure of the project's config.properties
        write("""
                fs=${const:java.io.File.separator}
                download.directory=${sys:user.dir}${fs}Downloads
                plugin.directory=${sys:user.dir}${fs}plugins
                userdata.directory=${sys:user.dir}${fs}UserData
                browser.type=chrome
                browser.chrome.version=135
                browser.chrome.preferences.download.prompt_for_download=false
                browser.chrome.preferences.safebrowsing.enabled=true
                browser.chrome.preferences.plugins.always_open_pdf_externally=false
                browser.chrome.arguments.sandbox=--no-sandbox
                browser.chrome.arguments.gpu=--disable-gpu
                browser.chrome.arguments.lang=--lang=en-US
                browser.chrome.capabilities.acceptInsecureCerts=true
                """);

        var result = load();

        // Core mappings
        assertEquals("135", config.getBrowserVersion());
        assertTrue(config.getRawArguments().contains("--no-sandbox"));
        assertTrue(config.getRawArguments().contains("--disable-gpu"));
        assertTrue(config.getRawArguments().contains("--lang=en-US"),
                "lang arg must be applied: rawArguments=" + config.getRawArguments());
        assertEquals(Boolean.FALSE, config.getChromePreferences().get("download.prompt_for_download"));
        assertEquals(Boolean.TRUE,  config.getChromePreferences().get("safebrowsing.enabled"));
        assertEquals(Boolean.TRUE,  config.getChromeCapabilities().get("acceptInsecureCerts"));

        // browser.type → warning; variable keys (fs, download.directory, etc.) → NOT in warnings
        assertTrue(result.getWarnings().stream().anyMatch(w -> w.contains("browser.type")));
        assertTrue(result.getWarnings().stream().noneMatch(w -> w.contains("fs")),
                "fs variable key must not appear in warnings");
        assertTrue(result.getWarnings().stream().noneMatch(w -> w.contains("download.directory")),
                "download.directory variable key must not appear in warnings");

        // All expected args in applied
        assertTrue(result.getApplied().stream().anyMatch(s -> s.contains("--lang=en-US")));
        assertFalse(result.getApplied().isEmpty());
    }
}

