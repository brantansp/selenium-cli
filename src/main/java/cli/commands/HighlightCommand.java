package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Highlights an element by injecting a temporary CSS border.
 * Useful for visual debugging during interactive sessions.
 *
 * <pre>
 *   selenium&gt; highlight #login-btn
 *   selenium&gt; highlight .error-msg --color red
 *   selenium&gt; highlight #logo --color blue --duration 5
 * </pre>
 */
@Command(name = "highlight", description = "Highlight an element with a colored border")
public class HighlightCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Option(names = "--color", description = "Border color (default: red)",
            defaultValue = "red")
    private String color;

    @Option(names = "--duration", description = "Highlight duration in seconds (default: 3)",
            defaultValue = "3")
    private int duration;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            JavascriptExecutor js = driver;

            // Save original style, apply highlight
            String script =
                "var el = arguments[0];" +
                "var orig = el.style.cssText;" +
                "el.style.outline = '3px solid " + color + "';" +
                "el.style.outlineOffset = '-3px';" +
                "el.setAttribute('data-sel-orig-style', orig);" +
                "setTimeout(function(){" +
                "  el.style.cssText = el.getAttribute('data-sel-orig-style') || '';" +
                "  el.removeAttribute('data-sel-orig-style');" +
                "}, " + (duration * 1000) + ");";

            js.executeScript(script, element);
            CommandResult.success("highlight", List.of(locator),
                    "Highlighted for " + duration + "s in " + color).print();
        } catch (Exception e) {
            CommandResult.error("highlight", List.of(locator), e.getMessage()).print();
        }
    }
}

