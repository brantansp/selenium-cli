package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Clears the content of an input or textarea element.
 *
 * <pre>
 *   selenium&gt; clear #email
 *   selenium&gt; clear "input[name='search']"
 * </pre>
 */
@Command(name = "clear", description = "Clear the content of an input field")
public class ClearCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            driver.findElement(LocatorParser.parse(locator)).clear();
            CommandResult.success("clear", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("clear", List.of(locator), e.getMessage()).print();
        }
    }
}

