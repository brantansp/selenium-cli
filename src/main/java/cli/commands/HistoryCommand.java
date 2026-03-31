package cli.commands;

import cli.model.CommandRequest;
import cli.model.CommandResult;
import cli.util.SessionRecorder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.*;

/**
 * Displays all successful commands recorded in the current session.
 *
 * <pre>
 *   selenium&gt; history
 *   selenium&gt; history --clear
 * </pre>
 */
@Command(name = "history", description = "Show recorded commands from the current session")
public class HistoryCommand implements Runnable {

    @Option(names = "--clear", description = "Clear the recorded history")
    private boolean clear;

    @Override
    public void run() {
        try {
            SessionRecorder recorder = SessionRecorder.getInstance();

            if (clear) {
                recorder.reset();
                CommandResult.success("history", List.of("--clear"),
                        "Session history cleared.").print();
                return;
            }

            if (!recorder.hasRecords()) {
                CommandResult.success("history", Collections.emptyList(),
                        "No commands recorded yet.").print();
                return;
            }

            List<CommandRequest> commands = recorder.getCommands();

            // Build a numbered list for display
            List<Map<String, Object>> entries = new ArrayList<>();
            for (int i = 0; i < commands.size(); i++) {
                CommandRequest req = commands.get(i);
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("step", i + 1);
                entry.put("command", req.getCommand());
                entry.put("args", req.getArgs());
                entries.add(entry);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("recording", recorder.isEnabled() ? "enabled" : "disabled");
            result.put("count", commands.size());
            result.put("commands", entries);

            CommandResult.success("history", Collections.emptyList(), result).print();
        } catch (Exception e) {
            CommandResult.error("history", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

