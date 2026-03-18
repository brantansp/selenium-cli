package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Returns the visible text of an element.
 *
 * <pre>
 *   selenium&gt; gettext .page-title
 * </pre>
 */
@Command(name = "gettext", description = "Get the visible text of an element")
public class GetTextCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            String text = driver.findElement(LocatorParser.parse(locator)).getText();
            CommandResult.success("gettext", List.of(locator), text).print();
        } catch (Exception e) {
            CommandResult.error("gettext", List.of(locator), e.getMessage()).print();
        }
    }
}
