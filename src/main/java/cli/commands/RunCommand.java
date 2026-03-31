package cli.commands;

import cli.config.BrowserConfig;
import cli.model.CommandRequest;
import cli.model.CommandResult;
import cli.model.SessionFile;
import cli.util.JsonOutput;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.openqa.selenium.PageLoadStrategy;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Executes a batch of commands from a JSON file.
 * Supports two formats:
 *
 * <p><b>Session file format</b> (produced by session recording):
 * <pre>
 * {
 *   "config": { "headless": true, ... },
 *   "commands": [
 *     { "step": 1, "command": "open", "args": ["https://google.com"] }
 *   ]
 * }
 * </pre>
 *
 * <p><b>Simple array format</b> (manual/legacy):
 * <pre>
 * [
 *   { "command": "open", "args": ["https://google.com"] }
 * ]
 * </pre>
 */
@Command(name = "run", description = "Execute a batch of commands from a JSON file")
public class RunCommand implements Runnable {

    @Option(names = "--json", required = true, description = "Path to JSON command file")
    private File inputFile;

    @Option(names = "--output", description = "Write results to this file instead of stdout")
    private File outputFile;

    @Option(names = "--continue-on-error",
            description = "Continue executing remaining commands after a failure")
    private boolean continueOnError;

    /**
     * The parent CommandLine is injected so we can re-dispatch subcommands.
     */
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    @Override
    public void run() {
        List<String> captured = new ArrayList<>();
        try {
            // Read the JSON file
            String json = Files.readString(inputFile.toPath(), StandardCharsets.UTF_8);

            // Detect format: { "config": ..., "commands": [...] }  vs  [...]
            List<CommandRequest> requests;
            JsonElement root = JsonOutput.gson().fromJson(json, JsonElement.class);

            if (root.isJsonObject()) {
                // Session file format — apply config, then extract commands
                SessionFile sessionFile = JsonOutput.gson().fromJson(json, SessionFile.class);
                if (sessionFile.getConfig() != null) {
                    applyConfig(sessionFile.getConfig());
                }
                requests = sessionFile.getCommands();
            } else {
                // Simple array format (legacy / manual)
                Type listType = new TypeToken<List<CommandRequest>>() {}.getType();
                requests = JsonOutput.gson().fromJson(json, listType);
            }

            if (requests == null || requests.isEmpty()) {
                CommandResult.error("run", List.of(inputFile.getName()),
                        "JSON file is empty or invalid").print();
                return;
            }

            CommandLine parentCli = spec.commandLine().getParent();
            if (parentCli == null) {
                parentCli = spec.commandLine();
            }

            // Redirect stdout to capture each command's JSON output
            PrintStream originalOut = System.out;
            for (int i = 0; i < requests.size(); i++) {
                CommandRequest req = requests.get(i);
                List<String> tokens = new ArrayList<>();
                tokens.add(req.getCommand());
                if (req.getArgs() != null) {
                    tokens.addAll(req.getArgs());
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream capture = new PrintStream(baos, true, StandardCharsets.UTF_8);
                System.setOut(capture);

                try {
                    parentCli.execute(tokens.toArray(new String[0]));
                } finally {
                    System.setOut(originalOut);
                }

                String output = baos.toString(StandardCharsets.UTF_8).trim();
                captured.add(output);

                // Check for error — crude but effective: look for "error" status
                if (!continueOnError && output.contains("\"status\": \"error\"")) {
                    originalOut.println(buildBatchOutput(captured, i + 1, requests.size()));
                    return;
                }
            }

            String batchResult = buildBatchOutput(captured, requests.size(), requests.size());

            if (outputFile != null) {
                Files.writeString(outputFile.toPath(), batchResult, StandardCharsets.UTF_8);
                CommandResult.success("run",
                        List.of("--json", inputFile.getName(), "--output", outputFile.getName()),
                        "Results written to " + outputFile.getAbsolutePath()).print();
            } else {
                System.out.println(batchResult);
            }

        } catch (Exception e) {
            CommandResult.error("run", List.of(inputFile != null ? inputFile.getName() : "null"),
                    e.getMessage()).print();
        }
    }

    /**
     * Apply the config section from a session file to {@link BrowserConfig}
     * so the session is replayed with the exact same browser settings.
     */
    @SuppressWarnings("unchecked")
    private void applyConfig(Map<String, Object> config) {
        BrowserConfig cfg = BrowserConfig.getInstance();

        if (config.containsKey("headless")) {
            cfg.headless(Boolean.TRUE.equals(config.get("headless")));
        }
        if (config.containsKey("maximize")) {
            cfg.maximize(Boolean.TRUE.equals(config.get("maximize")));
        }
        if (config.containsKey("incognito")) {
            cfg.incognito(Boolean.TRUE.equals(config.get("incognito")));
        }
        if (config.containsKey("windowSize") && config.get("windowSize") != null) {
            cfg.windowSize(config.get("windowSize").toString());
        }
        if (config.containsKey("userDataDir") && config.get("userDataDir") != null) {
            cfg.userDataDir(config.get("userDataDir").toString());
        }
        if (config.containsKey("proxyUrl") && config.get("proxyUrl") != null) {
            cfg.proxyUrl(config.get("proxyUrl").toString());
        }
        if (config.containsKey("browserVersion") && config.get("browserVersion") != null) {
            cfg.browserVersion(config.get("browserVersion").toString());
        }
        if (config.containsKey("pageLoadStrategy") && config.get("pageLoadStrategy") != null) {
            cfg.pageLoadStrategy(
                    PageLoadStrategy.fromString(config.get("pageLoadStrategy").toString()));
        }
        if (config.get("extraHeaders") instanceof Map) {
            Map<String, Object> headers = (Map<String, Object>) config.get("extraHeaders");
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                cfg.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        if (config.get("rawArguments") instanceof List) {
            List<Object> args = (List<Object>) config.get("rawArguments");
            for (Object arg : args) {
                cfg.addArgument(arg.toString());
            }
        }
    }

    private String buildBatchOutput(List<String> capturedJsons, int executed, int total) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < capturedJsons.size(); i++) {
            // Indent each captured JSON block
            String indented = capturedJsons.get(i).lines()
                    .map(line -> "  " + line)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");
            sb.append(indented);
            if (i < capturedJsons.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}

