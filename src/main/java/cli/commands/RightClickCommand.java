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
 * Right-clicks (context click) an element.
 *
 * <pre>
 *   selenium&gt; rightclick #canvas
 *   selenium&gt; rightclick .context-menu-target
 * </pre>
 */
@Command(name = "rightclick", description = "Right-click (context click) an element")
public class RightClickCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            new Actions(driver).contextClick(element).perform();
            CommandResult.success("rightclick", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("rightclick", List.of(locator), e.getMessage()).print();
        }
    }
}

