package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;

import java.util.Collections;

/**
 * Prints the current page title.
 *
 * <pre>
 *   selenium&gt; title
 * </pre>
 */
@Command(name = "title", description = "Print the current page title")
public class TitleCommand implements Runnable {

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            String title = driver.getTitle();
            CommandResult.success("title", Collections.emptyList(), title).print();
        } catch (Exception e) {
            CommandResult.error("title", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

