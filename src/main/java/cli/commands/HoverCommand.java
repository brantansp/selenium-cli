package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Moves the mouse over an element (hover).
 *
 * <pre>
 *   selenium&gt; hover .dropdown-toggle
 *   selenium&gt; hover #menu-item
 * </pre>
 */
@Command(name = "hover", description = "Mouse hover over an element")
public class HoverCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            new Actions(driver).moveToElement(element).perform();
            CommandResult.success("hover", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("hover", List.of(locator), e.getMessage()).print();
        }
    }
}

