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
 * Switches the driver context to an iframe/frame, or back to the main document.
 *
 * <pre>
 *   selenium&gt; switchframe #my-iframe
 *   selenium&gt; switchframe --index 0
 *   selenium&gt; switchframe --parent
 *   selenium&gt; switchframe --main
 * </pre>
 */
@Command(name = "switchframe", description = "Switch to an iframe or back to main content")
public class SwitchFrameCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1", description = "Frame element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Option(names = "--index", description = "Switch to frame by zero-based index")
    private Integer index;

    @Option(names = "--parent", description = "Switch to the parent frame")
    private boolean parent;

    @Option(names = "--main", description = "Switch to the main (top-level) document")
    private boolean main;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();

            if (main) {
                driver.switchTo().defaultContent();
                CommandResult.success("switchframe", List.of("--main"), "Switched to main content").print();
            } else if (parent) {
                driver.switchTo().parentFrame();
                CommandResult.success("switchframe", List.of("--parent"), "Switched to parent frame").print();
            } else if (index != null) {
                driver.switchTo().frame(index);
                CommandResult.success("switchframe", List.of("--index", String.valueOf(index)),
                        "Switched to frame index " + index).print();
            } else if (locator != null) {
                WebElement frame = driver.findElement(LocatorParser.parse(locator));
                driver.switchTo().frame(frame);
                CommandResult.success("switchframe", List.of(locator),
                        "Switched to frame: " + locator).print();
            } else {
                CommandResult.error("switchframe", List.of(),
                        "Specify a locator, --index, --parent, or --main").print();
            }
        } catch (Exception e) {
            CommandResult.error("switchframe", List.of(locator != null ? locator : ""),
                    e.getMessage()).print();
        }
    }
}

