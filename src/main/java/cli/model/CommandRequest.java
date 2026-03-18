package cli.model;

import java.util.List;

/**
 * Represents a single command in a JSON batch file for {@code run --json}.
 *
 * Example JSON:
 * <pre>
 * { "command": "open", "args": ["https://google.com"] }
 * </pre>
 */
public class CommandRequest {

    private String command;
    private List<String> args;

    public CommandRequest() {}

    public CommandRequest(String command, List<String> args) {
        this.command = command;
        this.args = args;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<String> getArgs() { return args; }
    public void setArgs(List<String> args) { this.args = args; }
}

