package cli.model;

import java.util.List;

/**
 * Represents a single command in a JSON batch/session file.
 *
 * Example JSON:
 * <pre>
 * { "step": 1, "command": "open", "args": ["https://google.com"] }
 * </pre>
 */
public class CommandRequest {

    private Integer step;
    private String command;
    private List<String> args;

    public CommandRequest() {}

    public CommandRequest(String command, List<String> args) {
        this.command = command;
        this.args = args;
    }

    public CommandRequest(int step, String command, List<String> args) {
        this.step = step;
        this.command = command;
        this.args = args;
    }

    public Integer getStep() { return step; }
    public void setStep(Integer step) { this.step = step; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public List<String> getArgs() { return args; }
    public void setArgs(List<String> args) { this.args = args; }
}

