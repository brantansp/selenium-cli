package cli.util;

import cli.config.BrowserConfig;
import cli.model.CommandRequest;
import cli.model.SessionFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Records all commands executed during a REPL session and writes them
 * to a JSON file in the same format as the {@code run --json} input.
 *
 * <p>Singleton — obtain via {@link #getInstance()}.
 * Recording is <b>enabled by default</b>; toggle with
 * {@link #enable()} / {@link #disable()} or via
 * {@code config --record} / {@code config --record false}.
 *
 * <p>The output file can be replayed with:
 * <pre>
 *   selenium run --json session-2026-03-18_10-05-30.json
 * </pre>
 *
 * <p>Meta-commands ({@code help}, {@code exit}, {@code session}, {@code run})
 * are excluded from the recording since they are not replayable actions.
 */
public class SessionRecorder {

    private static final SessionRecorder INSTANCE = new SessionRecorder();

    /** Commands that should NOT be recorded (non-replayable meta commands). */
    private static final Set<String> EXCLUDED_COMMANDS = Set.of(
            "help", "exit", "session", "run", "history", "--help", "-h"
    );

    private boolean enabled = true;
    private final List<CommandRequest> commands = new ArrayList<>();

    private SessionRecorder() {}

    public static SessionRecorder getInstance() { return INSTANCE; }

    // ── enable / disable ────────────────────────────────────────

    public void enable()               { this.enabled = true; }
    public void disable()              { this.enabled = false; }
    public boolean isEnabled()         { return enabled; }

    /**
     * Record a command if recording is enabled and the command is replayable.
     *
     * @param tokens the tokenised command line (e.g. {@code ["open", "https://google.com"]})
     */
    public void record(String[] tokens) {
        if (!enabled) return;
        if (tokens == null || tokens.length == 0) return;

        String command = tokens[0].toLowerCase();
        if (EXCLUDED_COMMANDS.contains(command)) return;

        List<String> args = tokens.length > 1
                ? Arrays.asList(Arrays.copyOfRange(tokens, 1, tokens.length))
                : List.of();

        commands.add(new CommandRequest(command, args));
    }

    /**
     * @return an unmodifiable view of the recorded commands so far.
     */
    public List<CommandRequest> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    /**
     * @return {@code true} if at least one replayable command has been recorded.
     */
    public boolean hasRecords() {
        return !commands.isEmpty();
    }

    /**
     * Build a {@link SessionFile} with numbered steps and a config snapshot.
     */
    private SessionFile buildSessionFile() {
        List<CommandRequest> numbered = new ArrayList<>();
        for (int i = 0; i < commands.size(); i++) {
            CommandRequest src = commands.get(i);
            numbered.add(new CommandRequest(i + 1, src.getCommand(), src.getArgs()));
        }
        return new SessionFile(BrowserConfig.getInstance().toMap(), numbered);
    }

    /**
     * Write the recorded commands to a JSON file and return the path.
     * <p>
     * The file includes step numbers and a snapshot of the browser config
     * so the session can be replayed with the exact same settings.
     *
     * @return the absolute path of the written file
     * @throws IOException if writing fails
     */
    public Path save() throws IOException {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path path = Path.of("session-" + timestamp + ".json");
        String json = JsonOutput.toJson(buildSessionFile());
        Files.writeString(path, json, StandardCharsets.UTF_8);
        return path.toAbsolutePath();
    }

    /**
     * Write the recorded commands to a specific file.
     *
     * @param outputPath target file path
     * @return the absolute path of the written file
     * @throws IOException if writing fails
     */
    public Path save(Path outputPath) throws IOException {
        String json = JsonOutput.toJson(buildSessionFile());
        Files.writeString(outputPath, json, StandardCharsets.UTF_8);
        return outputPath.toAbsolutePath();
    }

    /** Clear all recorded commands and reset enabled state to default (on). */
    public void reset() {
        commands.clear();
        enabled = true;
    }
}
