package cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LocatorParser} — the core locator auto-detection logic.
 * This is one of the most critical utilities: if locator parsing breaks,
 * every browser-interaction command fails.
 */
@DisplayName("LocatorParser")
class LocatorParserTest {

    // ── Explicit prefix strategies ──────────────────────────────

    @Nested
    @DisplayName("Explicit prefix strategies")
    class ExplicitPrefixes {

        @Test
        @DisplayName("xpath= prefix → By.xpath")
        void xpathPrefix() {
            By result = LocatorParser.parse("xpath=//div[@id='main']");
            assertEquals(By.xpath("//div[@id='main']"), result);
        }

        @Test
        @DisplayName("css= prefix → By.cssSelector")
        void cssPrefix() {
            By result = LocatorParser.parse("css=div.content > p");
            assertEquals(By.cssSelector("div.content > p"), result);
        }

        @Test
        @DisplayName("id= prefix → By.id")
        void idPrefix() {
            By result = LocatorParser.parse("id=email");
            assertEquals(By.id("email"), result);
        }

        @Test
        @DisplayName("name= prefix → By.name")
        void namePrefix() {
            By result = LocatorParser.parse("name=username");
            assertEquals(By.name("username"), result);
        }

        @Test
        @DisplayName("tag= prefix → By.tagName")
        void tagPrefix() {
            By result = LocatorParser.parse("tag=h1");
            assertEquals(By.tagName("h1"), result);
        }
    }

    // ── Auto-detection ──────────────────────────────────────────

    @Nested
    @DisplayName("Auto-detection")
    class AutoDetection {

        @Test
        @DisplayName("// prefix → By.xpath (absolute)")
        void xpathAbsolute() {
            By result = LocatorParser.parse("//button[@type='submit']");
            assertEquals(By.xpath("//button[@type='submit']"), result);
        }

        @Test
        @DisplayName("(// prefix → By.xpath (grouped)")
        void xpathGrouped() {
            By result = LocatorParser.parse("(//div[@class='item'])[1]");
            assertEquals(By.xpath("(//div[@class='item'])[1]"), result);
        }

        @Test
        @DisplayName("#id shorthand → By.id (strips #)")
        void idShorthand() {
            By result = LocatorParser.parse("#submit-btn");
            assertEquals(By.id("submit-btn"), result);
        }

        @Test
        @DisplayName(".class shorthand → By.cssSelector (keeps .)")
        void classShorthand() {
            By result = LocatorParser.parse(".btn-primary");
            assertEquals(By.cssSelector(".btn-primary"), result);
        }

        @Test
        @DisplayName("Complex CSS falls through to cssSelector")
        void cssFallback() {
            By result = LocatorParser.parse("input[type='email']");
            assertEquals(By.cssSelector("input[type='email']"), result);
        }

        @Test
        @DisplayName("Simple tag name falls through to cssSelector")
        void tagFallback() {
            By result = LocatorParser.parse("body");
            assertEquals(By.cssSelector("body"), result);
        }

        @Test
        @DisplayName("Attribute selector falls through to cssSelector")
        void attrFallback() {
            By result = LocatorParser.parse("[data-testid='login']");
            assertEquals(By.cssSelector("[data-testid='login']"), result);
        }
    }

    // ── Edge cases / error handling ─────────────────────────────

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        @DisplayName("Null, empty, or blank input throws IllegalArgumentException")
        void nullOrBlankThrows(String input) {
            assertThrows(IllegalArgumentException.class, () -> LocatorParser.parse(input));
        }

        @Test
        @DisplayName("#id with hyphen and numbers")
        void idWithSpecialChars() {
            assertEquals(By.id("my-element-42"), LocatorParser.parse("#my-element-42"));
        }

        @Test
        @DisplayName("xpath= with empty expression")
        void xpathEmptyExpression() {
            // Should still return a By.xpath, even if the expression is empty
            By result = LocatorParser.parse("xpath=");
            assertEquals(By.xpath(""), result);
        }

        @Test
        @DisplayName("css= with complex nested selector")
        void cssComplex() {
            By result = LocatorParser.parse("css=div.container > ul li:nth-child(2) a");
            assertEquals(By.cssSelector("div.container > ul li:nth-child(2) a"), result);
        }
    }
}

