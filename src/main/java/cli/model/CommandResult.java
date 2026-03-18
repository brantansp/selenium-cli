package cli.model;

import cli.session.SessionManager;
import cli.util.JsonOutput;

import java.time.Instant;
import java.util.List;

/**
 * Universal JSON envelope for every CLI response.
 */
public class CommandResult {

    private final String status;
    private final String command;
    private final List<String> args;
    private final Object result;
    private final String sessionId;
    private final String timestamp;
    private final String error;

    private CommandResult(String status, String command, List<String> args,
                          Object result, String error) {
        this.status = status;
        this.command = command;
        this.args = args;
        this.result = result;
        this.error = error;
        this.timestamp = Instant.now().toString();
        this.sessionId = SessionManager.getInstance().isActive()
                ? SessionManager.getInstance().getSessionId()
                : null;
    }

    public static CommandResult success(String command, List<String> args, Object result) {
        return new CommandResult("success", command, args, result, null);
    }

    public static CommandResult error(String command, List<String> args, String errorMessage) {
        return new CommandResult("error", command, args, null, errorMessage);
    }

    public String getStatus() { return status; }
    public String getCommand() { return command; }
    public List<String> getArgs() { return args; }
    public Object getResult() { return result; }
    public String getSessionId() { return sessionId; }
    public String getTimestamp() { return timestamp; }
    public String getError() { return error; }

    /** Print this result as pretty JSON to stdout. */
    public void print() {
        System.out.println(JsonOutput.toJson(this));
    }
}

