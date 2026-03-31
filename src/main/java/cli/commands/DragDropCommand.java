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
 * Drags one element and drops it onto another.
 *
 * <pre>
 *   selenium&gt; dragdrop #source #target
 *   selenium&gt; dragdrop .draggable .droppable
 * </pre>
 */
@Command(name = "dragdrop", description = "Drag an element and drop it onto another")
public class DragDropCommand implements Runnable {

    @Parameters(index = "0", description = "Source element locator",
            completionCandidates = LocatorCandidates.class)
    private String sourceLocator;

    @Parameters(index = "1", description = "Target element locator",
            completionCandidates = LocatorCandidates.class)
    private String targetLocator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement source = driver.findElement(LocatorParser.parse(sourceLocator));
            WebElement target = driver.findElement(LocatorParser.parse(targetLocator));
            new Actions(driver).dragAndDrop(source, target).perform();
            CommandResult.success("dragdrop", List.of(sourceLocator, targetLocator), null).print();
        } catch (Exception e) {
            CommandResult.error("dragdrop", List.of(sourceLocator, targetLocator), e.getMessage()).print();
        }
    }
}

