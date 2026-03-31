package cli.completions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for all completion candidate providers.
 * Validates that each one:
 *   1. Can be instantiated with a no-arg constructor (required by Picocli)
 *   2. Returns a non-empty iterator
 *   3. Contains expected key entries
 *
 * These tests catch accidental deletion or corruption of completion data,
 * which would degrade the REPL user experience.
 */
@DisplayName("Completion Candidates")
class CompletionCandidatesTest {

    /**
     * Collect all items from an Iterable into a List.
     */
    private List<String> toList(Iterable<String> iterable) {
        List<String> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    // ── LocatorCandidates ───────────────────────────────────────

    @Test
    @DisplayName("LocatorCandidates: instantiable and non-empty")
    void locatorCandidates() {
        var candidates = new LocatorCandidates();
        List<String> list = toList(candidates);
        assertFalse(list.isEmpty());
    }

    @Test
    @DisplayName("LocatorCandidates: contains key locator prefixes")
    void locatorCandidatesContent() {
        List<String> list = toList(new LocatorCandidates());
        assertTrue(list.stream().anyMatch(s -> s.startsWith("xpath=")));
        assertTrue(list.stream().anyMatch(s -> s.startsWith("css=")));
        assertTrue(list.stream().anyMatch(s -> s.startsWith("id=")));
        assertTrue(list.stream().anyMatch(s -> s.startsWith("#")));
        assertTrue(list.stream().anyMatch(s -> s.startsWith(".")));
    }

    // ── KeyCandidates ───────────────────────────────────────────

    @Test
    @DisplayName("KeyCandidates: instantiable and non-empty")
    void keyCandidates() {
        List<String> list = toList(new KeyCandidates());
        assertFalse(list.isEmpty());
    }

    @Test
    @DisplayName("KeyCandidates: contains common keys")
    void keyCandidatesContent() {
        List<String> list = toList(new KeyCandidates());
        assertTrue(list.contains("ENTER"));
        assertTrue(list.contains("TAB"));
        assertTrue(list.contains("ESCAPE"));
        assertTrue(list.contains("BACKSPACE"));
        assertTrue(list.contains("CONTROL+a"));
    }

    // ── NavigationCandidates ────────────────────────────────────

    @Test
    @DisplayName("NavigationCandidates: contains back, forward, refresh")
    void navigationCandidates() {
        List<String> list = toList(new NavigationCandidates());
        assertTrue(list.contains("back"));
        assertTrue(list.contains("forward"));
        assertTrue(list.contains("refresh"));
    }

    // ── UrlCandidates ───────────────────────────────────────────

    @Test
    @DisplayName("UrlCandidates: contains https:// template")
    void urlCandidates() {
        List<String> list = toList(new UrlCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.startsWith("https://")));
    }

    // ── AttributeCandidates ─────────────────────────────────────

    @Test
    @DisplayName("AttributeCandidates: contains common HTML attributes")
    void attributeCandidates() {
        List<String> list = toList(new AttributeCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.contains("id"));
        assertTrue(list.contains("href"));
        assertTrue(list.contains("src"));
        assertTrue(list.contains("value"));
    }

    // ── WaitSecondsCandidates ───────────────────────────────────

    @Test
    @DisplayName("WaitSecondsCandidates: contains common durations")
    void waitSecondsCandidates() {
        List<String> list = toList(new WaitSecondsCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.contains("1"));
        assertTrue(list.contains("5"));
        assertTrue(list.contains("10"));
    }

    // ── WindowSizeCandidates ────────────────────────────────────

    @Test
    @DisplayName("WindowSizeCandidates: contains standard viewport sizes")
    void windowSizeCandidates() {
        List<String> list = toList(new WindowSizeCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.contains("1920x1080"));
    }

    // ── BrowserVersionCandidates ────────────────────────────────

    @Test
    @DisplayName("BrowserVersionCandidates: contains 'stable'")
    void browserVersionCandidates() {
        List<String> list = toList(new BrowserVersionCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.contains("stable"));
    }

    // ── ChromeArgCandidates ─────────────────────────────────────

    @Test
    @DisplayName("ChromeArgCandidates: contains common Chrome flags")
    void chromeArgCandidates() {
        List<String> list = toList(new ChromeArgCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.contains("headless")));
        assertTrue(list.stream().anyMatch(s -> s.contains("disable-gpu")));
    }

    // ── HeaderCandidates ────────────────────────────────────────

    @Test
    @DisplayName("HeaderCandidates: contains Authorization template")
    void headerCandidates() {
        List<String> list = toList(new HeaderCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.contains("Authorization")));
    }

    // ── JsSnippetCandidates ─────────────────────────────────────

    @Test
    @DisplayName("JsSnippetCandidates: contains document.title snippet")
    void jsSnippetCandidates() {
        List<String> list = toList(new JsSnippetCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(s -> s.contains("document.title")));
    }

    // ── ScreenshotPathCandidates ────────────────────────────────

    @Test
    @DisplayName("ScreenshotPathCandidates: contains screenshot.png")
    void screenshotPathCandidates() {
        List<String> list = toList(new ScreenshotPathCandidates());
        assertFalse(list.isEmpty());
        assertTrue(list.contains("screenshot.png"));
    }
}

