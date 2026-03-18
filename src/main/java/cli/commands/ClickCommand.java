package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Clicks an element identified by a locator.
 *
 * <pre>
 *   selenium&gt; click //button[@id='submit']
 *   selenium&gt; click .btn-primary
 *   selenium&gt; click #login
 * </pre>
 */
@Command(name = "click", description = "Click an element by locator")
public class ClickCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator (xpath, css, #id, .class)",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            driver.findElement(LocatorParser.parse(locator)).click();
            CommandResult.success("click", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("click", List.of(locator), e.getMessage()).print();
        }
    }
}
