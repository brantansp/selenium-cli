package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Submits a form element (or any element within a form).
 *
 * <pre>
 *   selenium&gt; submit #login-form
 *   selenium&gt; submit #username
 * </pre>
 */
@Command(name = "submit", description = "Submit a form")
public class SubmitCommand implements Runnable {

    @Parameters(index = "0", description = "Form or element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            driver.findElement(LocatorParser.parse(locator)).submit();
            CommandResult.success("submit", List.of(locator), null).print();
        } catch (Exception e) {
            CommandResult.error("submit", List.of(locator), e.getMessage()).print();
        }
    }
}

