package cli.util;

import org.openqa.selenium.By;

/**
 * Auto-detects a Selenium {@link By} locator strategy from a raw string.
 *
 * <ul>
 *   <li>{@code xpath=...}  → By.xpath</li>
 *   <li>{@code css=...}    → By.cssSelector</li>
 *   <li>{@code id=...}     → By.id</li>
 *   <li>{@code name=...}   → By.name</li>
 *   <li>{@code tag=...}    → By.tagName</li>
 *   <li>{@code //} or {@code (//} → By.xpath (auto)</li>
 *   <li>{@code #foo}       → By.id("foo") (shorthand)</li>
 *   <li>{@code .foo}       → By.cssSelector(".foo")</li>
 *   <li>anything else      → By.cssSelector (default)</li>
 * </ul>
 */
public final class LocatorParser {

    private LocatorParser() {}

    public static By parse(String locator) {
        if (locator == null || locator.isBlank()) {
            throw new IllegalArgumentException("Locator must not be empty");
        }

        // Explicit prefix strategies
        if (locator.startsWith("xpath="))  return By.xpath(locator.substring(6));
        if (locator.startsWith("css="))    return By.cssSelector(locator.substring(4));
        if (locator.startsWith("id="))     return By.id(locator.substring(3));
        if (locator.startsWith("name="))   return By.name(locator.substring(5));
        if (locator.startsWith("tag="))    return By.tagName(locator.substring(4));

        // Auto-detection
        if (locator.startsWith("//") || locator.startsWith("(//")) {
            return By.xpath(locator);
        }
        if (locator.startsWith("#")) {
            return By.id(locator.substring(1));
        }
        // Starts with . and followed by a letter/dash/underscore → CSS class shorthand
        if (locator.startsWith(".")) {
            return By.cssSelector(locator);
        }

        // Fallback: treat as CSS selector
        return By.cssSelector(locator);
    }
}

