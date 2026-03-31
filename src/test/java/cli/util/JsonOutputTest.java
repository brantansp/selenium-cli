package cli.util;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link JsonOutput} — the centralised Gson instance.
 * Ensures JSON serialisation is pretty-printed, consistent, and
 * round-trips correctly.
 */
@DisplayName("JsonOutput")
class JsonOutputTest {

    @Test
    @DisplayName("toJson produces pretty-printed output")
    void prettyPrint() {
        Map<String, Object> map = Map.of("key", "value");
        String json = JsonOutput.toJson(map);
        assertTrue(json.contains("\n"), "Should be pretty-printed with newlines");
        assertTrue(json.contains("  "), "Should have indentation");
    }

    @Test
    @DisplayName("toJson does not escape HTML entities")
    void noHtmlEscaping() {
        Map<String, String> map = Map.of("html", "<b>bold</b> & 'quotes'");
        String json = JsonOutput.toJson(map);
        assertTrue(json.contains("<b>bold</b>"), "HTML tags should not be escaped");
        assertTrue(json.contains("&"), "Ampersand should not be escaped to \\u0026");
    }

    @Test
    @DisplayName("gson() returns same instance every time")
    void singletonGson() {
        assertSame(JsonOutput.gson(), JsonOutput.gson());
    }

    @Test
    @DisplayName("toJson handles null values in maps")
    void nullValues() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("present", "yes");
        map.put("absent", null);
        String json = JsonOutput.toJson(map);
        // Gson default (no serializeNulls) omits null keys
        assertTrue(json.contains("present"));
    }

    @Test
    @DisplayName("toJson serialises lists correctly")
    void listSerialization() {
        List<String> list = List.of("a", "b", "c");
        String json = JsonOutput.toJson(list);
        assertTrue(json.contains("\"a\""));
        assertTrue(json.contains("\"b\""));
        assertTrue(json.contains("\"c\""));
    }

    @Nested
    @DisplayName("Round-trip serialisation")
    class RoundTrip {

        @Test
        @DisplayName("Map round-trips through toJson → fromJson")
        void mapRoundTrip() {
            Map<String, Object> original = new LinkedHashMap<>();
            original.put("name", "test");
            original.put("count", 42);

            String json = JsonOutput.toJson(original);
            JsonObject parsed = JsonOutput.gson().fromJson(json, JsonObject.class);

            assertEquals("test", parsed.get("name").getAsString());
            assertEquals(42, parsed.get("count").getAsInt());
        }
    }
}

