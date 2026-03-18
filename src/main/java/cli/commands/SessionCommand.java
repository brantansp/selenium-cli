package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;

import java.util.Collections;

/**
 * Prints session metadata (id, browser version, current URL, config).
 *
 * <pre>
 *   selenium&gt; session
 * </pre>
 */
@Command(name = "session", description = "Show current session information")
public class SessionCommand implements Runnable {

    @Override
    public void run() {
        try {
            var info = SessionManager.getInstance().getSessionInfo();
            CommandResult.success("session", Collections.emptyList(), info).print();
        } catch (Exception e) {
            CommandResult.error("session", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

