package cli.model;

import java.util.List;
import java.util.Map;

/**
 * Top-level wrapper for session recording JSON files.
 *
 * <pre>
 * {
 *   "config": { "headless": true, "windowSize": "1920x1080", ... },
 *   "commands": [
 *     { "step": 1, "command": "open", "args": ["https://google.com"] },
 *     ...
 *   ]
 * }
 * </pre>
 *
 * The {@code config} section captures the {@link cli.config.BrowserConfig}
 * snapshot at the time of saving so the session can be replayed with
 * the exact same browser settings.
 */
public class SessionFile {

    private Map<String, Object> config;
    private List<CommandRequest> commands;

    public SessionFile() {}

    public SessionFile(Map<String, Object> config, List<CommandRequest> commands) {
        this.config = config;
        this.commands = commands;
    }

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }

    public List<CommandRequest> getCommands() { return commands; }
    public void setCommands(List<CommandRequest> commands) { this.commands = commands; }
}

