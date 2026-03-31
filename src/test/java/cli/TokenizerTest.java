package cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SeleniumCli#tokenize(String)} — the REPL input tokenizer.
 * This is critical because it determines how every user command is parsed.
 * Incorrect tokenization = wrong arguments passed to commands.
 */
@DisplayName("SeleniumCli.tokenize()")
class TokenizerTest {

    /**
     * Helper to invoke the package-private tokenize method.
     */
    private String[] tokenize(String input) {
        return SeleniumCli.tokenize(input);
    }

    // ── Basic tokenization ──────────────────────────────────────

    @Nested
    @DisplayName("Basic tokenization")
    class BasicTokenization {

        @Test
        @DisplayName("Simple command with no args")
        void simpleNoArgs() {
            assertArrayEquals(new String[]{"url"}, tokenize("url"));
        }

        @Test
        @DisplayName("Command with one arg")
        void commandOneArg() {
            assertArrayEquals(
                    new String[]{"open", "https://google.com"},
                    tokenize("open https://google.com"));
        }

        @Test
        @DisplayName("Command with multiple args")
        void commandMultipleArgs() {
            assertArrayEquals(
                    new String[]{"type", "#email", "user@test.com"},
                    tokenize("type #email user@test.com"));
        }

        @Test
        @DisplayName("Extra spaces between tokens are collapsed")
        void extraSpaces() {
            assertArrayEquals(
                    new String[]{"click", "#btn"},
                    tokenize("  click    #btn  "));
        }

        @Test
        @DisplayName("Empty input returns empty array")
        void emptyInput() {
            assertArrayEquals(new String[]{}, tokenize(""));
        }

        @Test
        @DisplayName("Whitespace-only input returns empty array")
        void whitespaceOnly() {
            assertArrayEquals(new String[]{}, tokenize("   "));
        }
    }

    // ── Quoted strings (grouping quotes) ────────────────────────

    @Nested
    @DisplayName("Grouping quotes (start of token)")
    class GroupingQuotes {

        @Test
        @DisplayName("Double quotes group text with spaces")
        void doubleQuotes() {
            assertArrayEquals(
                    new String[]{"type", "#email", "hello world"},
                    tokenize("type #email \"hello world\""));
        }

        @Test
        @DisplayName("Single quotes group text with spaces")
        void singleQuotes() {
            assertArrayEquals(
                    new String[]{"type", "#name", "John Doe"},
                    tokenize("type #name 'John Doe'"));
        }

        @Test
        @DisplayName("Quotes at token start are stripped")
        void quotesStripped() {
            String[] result = tokenize("type #x \"my text\"");
            assertEquals("my text", result[2]); // no quotes in output
        }

        @Test
        @DisplayName("Mixed quoted and unquoted args")
        void mixedArgs() {
            assertArrayEquals(
                    new String[]{"type", "#email", "user@test.com", "--clear"},
                    tokenize("type #email \"user@test.com\" --clear"));
        }
    }

    // ── Embedded quotes (mid-token) ─────────────────────────────

    @Nested
    @DisplayName("Embedded quotes (mid-token)")
    class EmbeddedQuotes {

        @Test
        @DisplayName("XPath with embedded single quotes preserves quotes")
        void xpathEmbedded() {
            String[] result = tokenize("click xpath=(//*[@value='Google Search'])[2]");
            assertEquals(2, result.length);
            assertEquals("click", result[0]);
            assertEquals("xpath=(//*[@value='Google Search'])[2]", result[1]);
        }

        @Test
        @DisplayName("CSS selector with embedded quotes")
        void cssSelectorEmbedded() {
            String[] result = tokenize("click input[type='submit']");
            assertEquals(2, result.length);
            assertEquals("input[type='submit']", result[1]);
        }
    }

    // ── Real-world command examples ─────────────────────────────

    @Nested
    @DisplayName("Real-world command examples")
    class RealWorldExamples {

        @Test
        @DisplayName("config command with multiple flags")
        void configCommand() {
            assertArrayEquals(
                    new String[]{"config", "--headless", "--window-size", "1920x1080"},
                    tokenize("config --headless --window-size 1920x1080"));
        }

        @Test
        @DisplayName("type command with quoted text containing spaces")
        void typeWithSpaces() {
            assertArrayEquals(
                    new String[]{"type", "//textarea", "I am human"},
                    tokenize("type //textarea \"I am human\""));
        }

        @Test
        @DisplayName("execute with JavaScript code")
        void executeJs() {
            assertArrayEquals(
                    new String[]{"execute", "return document.title"},
                    tokenize("execute \"return document.title\""));
        }

        @Test
        @DisplayName("highlight with options")
        void highlightWithOptions() {
            assertArrayEquals(
                    new String[]{"highlight", "#logo", "--color", "blue", "--duration", "5"},
                    tokenize("highlight #logo --color blue --duration 5"));
        }

        @Test
        @DisplayName("getattr command")
        void getattr() {
            assertArrayEquals(
                    new String[]{"getattr", "#logo", "src"},
                    tokenize("getattr #logo src"));
        }

        @Test
        @DisplayName("run command with file path")
        void runCommand() {
            assertArrayEquals(
                    new String[]{"run", "--json", "session-2026-03-31.json"},
                    tokenize("run --json session-2026-03-31.json"));
        }

        @Test
        @DisplayName("select with quoted visible text")
        void selectWithQuotedText() {
            assertArrayEquals(
                    new String[]{"select", "#country", "United States"},
                    tokenize("select #country \"United States\""));
        }
    }
}

