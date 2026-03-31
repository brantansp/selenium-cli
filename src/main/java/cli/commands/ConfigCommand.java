package cli.commands;

import cli.completions.BrowserVersionCandidates;
import cli.completions.ChromeArgCandidates;
import cli.completions.HeaderCandidates;
import cli.completions.WindowSizeCandidates;
import cli.config.BrowserConfig;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.SessionRecorder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configures browser options and CLI behaviour before or during a session.
 *
 * <pre>
 *   selenium&gt; config --maximize
 *   selenium&gt; config --headless --window-size 1920x1080
 *   selenium&gt; config --header X-Custom:value
 *   selenium&gt; config --browser-version 124
 *   selenium&gt; config --record false        ← disable session recording
 *   selenium&gt; config --record true         ← re-enable session recording
 *   selenium&gt; config --show
 * </pre>
 */
@Command(name = "config", description = "Configure browser options")
public class ConfigCommand implements Runnable {

    @Option(names = "--record", arity = "0..1", fallbackValue = "true",
            description = "Enable/disable session recording (default: true)")
    private Boolean record;

    @Option(names = "--headless", arity = "0..1", fallbackValue = "true",
            description = "Enable/disable headless mode (next session)")
    private Boolean headless;

    @Option(names = "--maximize", arity = "0..1", fallbackValue = "true",
            description = "Enable/disable maximize window")
    private Boolean maximize;

    @Option(names = "--incognito", arity = "0..1", fallbackValue = "true",
            description = "Enable/disable incognito mode (next session)")
    private Boolean incognito;

    @Option(names = "--window-size", description = "Window size, e.g. 1920x1080 (next session)",
            completionCandidates = WindowSizeCandidates.class)
    private String windowSize;

    @Option(names = "--user-data-dir", description = "Chrome user data directory (next session)")
    private String userDataDir;

    @Option(names = "--proxy", description = "Proxy URL, e.g. http://host:port (next session)")
    private String proxy;

    @Option(names = "--browser-version",
            description = "Chrome/CfT version for Selenium Manager to resolve (next session)",
            completionCandidates = BrowserVersionCandidates.class)
    private String browserVersion;

    @Option(names = "--header", description = "Extra HTTP header as Name:Value (repeatable)",
            arity = "1",
            completionCandidates = HeaderCandidates.class)
    private List<String> headers;

    @Option(names = "--options", split = ",",
            description = "Raw Chrome arguments, comma-separated (next session)",
            completionCandidates = ChromeArgCandidates.class)
    private List<String> rawOptions;

    @Option(names = "--show", description = "Print current configuration")
    private boolean show;

    @Override
    public void run() {
        try {
            BrowserConfig config = BrowserConfig.getInstance();
            List<String> applied = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            boolean sessionActive = SessionManager.getInstance().isActive();

            if (show) {
                Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
                snapshot.put("sessionRecording", SessionRecorder.getInstance().isEnabled());
                snapshot.putAll(config.toMap());
                CommandResult.success("config", List.of("--show"), snapshot).print();
                return;
            }

            // ── Session recording toggle ─────────────────────────
            if (record != null) {
                if (record) {
                    SessionRecorder.getInstance().enable();
                    applied.add("sessionRecording=true");
                } else {
                    SessionRecorder.getInstance().disable();
                    applied.add("sessionRecording=false");
                }
            }

            // Apply settings to BrowserConfig
            if (headless != null) {
                config.headless(headless);
                applied.add("headless=" + headless);
                if (sessionActive) warnings.add("headless will apply on next session");
            }
            if (maximize != null) {
                config.maximize(maximize);
                applied.add("maximize=" + maximize);
                // Live-apply if session is active
                if (sessionActive && maximize) {
                    SessionManager.getInstance().getDriverOrThrow().manage().window().maximize();
                    applied.add("maximize applied to live session");
                }
            }
            if (incognito != null) {
                config.incognito(incognito);
                applied.add("incognito=" + incognito);
                if (sessionActive) warnings.add("incognito will apply on next session");
            }
            if (windowSize != null) {
                config.windowSize(windowSize);
                applied.add("windowSize=" + windowSize);
                if (sessionActive) warnings.add("windowSize will apply on next session");
            }
            if (userDataDir != null) {
                config.userDataDir(userDataDir);
                applied.add("userDataDir=" + userDataDir);
                if (sessionActive) warnings.add("userDataDir will apply on next session");
            }
            if (proxy != null) {
                config.proxyUrl(proxy);
                applied.add("proxy=" + proxy);
                if (sessionActive) warnings.add("proxy will apply on next session");
            }
            if (browserVersion != null) {
                config.browserVersion(browserVersion);
                applied.add("browserVersion=" + browserVersion);
                if (sessionActive) warnings.add("browserVersion will apply on next session");
            }
            if (headers != null) {
                for (String h : headers) {
                    String[] parts = h.split(":", 2);
                    if (parts.length == 2) {
                        config.addHeader(parts[0].trim(), parts[1].trim());
                        applied.add("header " + parts[0].trim());
                    } else {
                        warnings.add("Invalid header format (expected Name:Value): " + h);
                    }
                }
                // Live-apply headers via CDP if session active
                if (sessionActive && !config.getExtraHeaders().isEmpty()) {
                    var driver = SessionManager.getInstance().getDriverOrThrow();
                    driver.executeCdpCommand("Network.enable", Map.of());
                    driver.executeCdpCommand("Network.setExtraHTTPHeaders",
                            Map.of("headers", config.getExtraHeaders()));
                    applied.add("headers applied to live session via CDP");
                }
            }
            if (rawOptions != null) {
                rawOptions.forEach(config::addArgument);
                applied.add("rawOptions added: " + rawOptions);
                if (sessionActive) warnings.add("rawOptions will apply on next session");
            }

            if (applied.isEmpty() && warnings.isEmpty()) {
                CommandResult.error("config", Collections.emptyList(),
                        "No options specified. Use --show to see current config or --help for options.").print();
                return;
            }

            Map<String, Object> result = Map.of(
                    "applied", applied,
                    "warnings", warnings
            );
            CommandResult.success("config", Collections.emptyList(), result).print();
        } catch (Exception e) {
            CommandResult.error("config", Collections.emptyList(), e.getMessage()).print();
        }
    }
}
