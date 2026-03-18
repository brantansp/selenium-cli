package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.WebElement;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Types text into an element.
 *
 * <pre>
 *   selenium&gt; type #email user@test.com
 *   selenium&gt; type #email user@test.com --clear
 * </pre>
 */
@Command(name = "type", description = "Type text into an element")
public class TypeCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Parameters(index = "1", description = "Text to type")
    private String text;

    @Option(names = "--clear", description = "Clear the field before typing")
    private boolean clear;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            if (clear) {
                element.clear();
            }
            element.sendKeys(text);
            CommandResult.success("type", List.of(locator, text), null).print();
        } catch (Exception e) {
            CommandResult.error("type", List.of(locator, text), e.getMessage()).print();
        }
    }
}
