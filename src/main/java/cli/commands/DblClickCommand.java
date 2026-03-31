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
 * Double-clicks an element.
 *
 * <pre>
 *   selenium&gt; dblclick #row-1
 *   selenium&gt; dblclick .editable-cell
 * </pre>
 */
@Command(name = "dblclick", description = "Double-click an element")
public class DblClickCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            new Actions(driver).doubleClick(element).perform();
            CommandResult.success("dblclick", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("dblclick", List.of(locator), e.getMessage()).print();
        }
    }
}

