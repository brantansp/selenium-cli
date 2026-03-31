package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;

import java.util.Collections;

/**
 * Prints the current page URL.
 *
 * <pre>
 *   selenium&gt; url
 * </pre>
 */
@Command(name = "url", description = "Print the current page URL")
public class UrlCommand implements Runnable {

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            String currentUrl = driver.getCurrentUrl();
            CommandResult.success("url", Collections.emptyList(), currentUrl).print();
        } catch (Exception e) {
            CommandResult.error("url", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

