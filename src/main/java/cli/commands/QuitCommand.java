package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;

import java.util.Collections;

/**
 * Quits the active browser session. Does NOT exit the REPL — the user can
 * start a new session afterwards. Use {@code exit} to leave the REPL entirely.
 *
 * <pre>
 *   selenium&gt; quit
 * </pre>
 */
@Command(name = "quit", description = "Quit the active browser session")
public class QuitCommand implements Runnable {

    @Override
    public void run() {
        try {
            SessionManager sm = SessionManager.getInstance();
            if (!sm.isActive()) {
                CommandResult.error("quit", Collections.emptyList(),
                        "No active session to quit.").print();
                return;
            }
            sm.shutdown();
            CommandResult.success("quit", Collections.emptyList(),
                    "Session closed.").print();
        } catch (Exception e) {
            CommandResult.error("quit", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

