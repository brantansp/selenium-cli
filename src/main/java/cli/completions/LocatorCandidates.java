package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for element locator parameters.
 * Shows available locator strategies and shorthand prefixes.
 */
public class LocatorCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "xpath=//",
            "css=",
            "id=",
            "name=",
            "tag=",
            "//",
            "(//",
            "#elementId",
            ".className",
            "[attribute='value']",
            "input[type='text']",
            "button[type='submit']",
            "a[href]",
            "//input[@name='']",
            "//button[@id='']",
            "//div[@class='']",
            "//*[contains(text(),'')]",
            "//*[@data-testid='']"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

