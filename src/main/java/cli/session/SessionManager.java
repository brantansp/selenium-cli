package cli.session;

import cli.config.BrowserConfig;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Singleton that owns the single live {@link ChromeDriver} session.
 * <p>
 * Only one session at a time — {@link #start(BrowserConfig)} fails if already active.
 */
public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private ChromeDriver driver;
    private String sessionId;
    private Instant startedAt;

    private SessionManager() {}

    public static SessionManager getInstance() { return INSTANCE; }

    // ── lifecycle ───────────────────────────────────────────────

    /**
     * Launch Chrome with the given config. Throws if a session is already active.
     */
    public synchronized void start(BrowserConfig config) {
        if (driver != null) {
            throw new IllegalStateException("A session is already active (id=" + sessionId + "). Quit first.");
        }

        ChromeOptions options = config.toChromeOptions();
        driver = new ChromeDriver(options);
        sessionId = driver.getSessionId().toString();
        startedAt = Instant.now();

        // Apply live-only settings
        if (config.isMaximize()) {
            driver.manage().window().maximize();
        }

        // Inject extra HTTP headers via CDP if configured
        if (!config.getExtraHeaders().isEmpty()) {
            Map<String, Object> headers = new LinkedHashMap<>(config.getExtraHeaders());
            driver.executeCdpCommand("Network.enable", Map.of());
            driver.executeCdpCommand("Network.setExtraHTTPHeaders", Map.of("headers", headers));
        }
    }

    /**
     * Returns the live driver, or throws {@link NoActiveSessionException}.
     */
    public ChromeDriver getDriverOrThrow() {
        if (driver == null) {
            throw new NoActiveSessionException();
        }
        return driver;
    }

    public boolean isActive() {
        return driver != null;
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * Quit the browser and clear state.
     */
    public synchronized void shutdown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) { /* best-effort */ }
            driver = null;
            sessionId = null;
            startedAt = null;
            BrowserConfig.getInstance().reset();
        }
    }

    /**
     * Snapshot of session metadata for the {@code session} command.
     */
    public Map<String, Object> getSessionInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        if (driver != null) {
            info.put("active", true);
            info.put("sessionId", sessionId);
            info.put("startedAt", startedAt.toString());
            try {
                info.put("currentUrl", driver.getCurrentUrl());
                info.put("title", driver.getTitle());
                var caps = driver.getCapabilities();
                info.put("browserName", caps.getBrowserName());
                info.put("browserVersion", caps.getBrowserVersion());
            } catch (Exception e) {
                info.put("note", "Could not retrieve live details: " + e.getMessage());
            }
            info.put("config", BrowserConfig.getInstance().toMap());
        } else {
            info.put("active", false);
        }
        return info;
    }
}

