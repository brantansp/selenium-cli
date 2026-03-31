package cli.config;

import cli.util.JsonOutput;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Accumulates Chrome configuration before (and sometimes after) a session starts.
 * Singleton — reset via {@link #reset()}.
 *
 * <p>Configuration is persisted to {@code .selenium-cli.json} in the current
 * working directory so that one-shot {@code config} commands survive across
 * JVM invocations.</p>
 */
public class BrowserConfig {

    private static final BrowserConfig INSTANCE = new BrowserConfig();
    private static final String CONFIG_FILE = ".selenium-cli.json";

    private boolean headless;
    private boolean maximize;
    private boolean incognito;
    private String windowSize;           // e.g. "1920x1080"
    private String userDataDir;
    private String proxyUrl;
    private String browserVersion;       // e.g. "124" → Selenium Manager fetches matching CfT
    private PageLoadStrategy pageLoadStrategy = PageLoadStrategy.NORMAL;
    private final Map<String, String> extraHeaders = new LinkedHashMap<>();
    private final List<String> rawArguments = new ArrayList<>();

    private BrowserConfig() {}

    public static BrowserConfig getInstance() { return INSTANCE; }

    // ── builders ────────────────────────────────────────────────

    public BrowserConfig headless(boolean v)          { this.headless = v; return this; }
    public BrowserConfig maximize(boolean v)          { this.maximize = v; return this; }
    public BrowserConfig incognito(boolean v)         { this.incognito = v; return this; }
    public BrowserConfig windowSize(String v)         { this.windowSize = v; return this; }
    public BrowserConfig userDataDir(String v)        { this.userDataDir = v; return this; }
    public BrowserConfig proxyUrl(String v)           { this.proxyUrl = v; return this; }
    public BrowserConfig browserVersion(String v)     { this.browserVersion = v; return this; }
    public BrowserConfig pageLoadStrategy(PageLoadStrategy v) { this.pageLoadStrategy = v; return this; }

    public BrowserConfig addHeader(String name, String value) {
        extraHeaders.put(name, value);
        return this;
    }

    public BrowserConfig addArgument(String arg) {
        rawArguments.add(arg);
        return this;
    }

    // ── getters ─────────────────────────────────────────────────

    public boolean isHeadless()        { return headless; }
    public boolean isMaximize()        { return maximize; }
    public boolean isIncognito()       { return incognito; }
    public String getWindowSize()      { return windowSize; }
    public String getUserDataDir()     { return userDataDir; }
    public String getProxyUrl()        { return proxyUrl; }
    public String getBrowserVersion()  { return browserVersion; }
    public Map<String, String> getExtraHeaders() { return extraHeaders; }
    public List<String> getRawArguments() { return rawArguments; }

    // ── convert to ChromeOptions ────────────────────────────────

    public ChromeOptions toChromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Sane defaults
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");
        options.setPageLoadStrategy(pageLoadStrategy);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
        }
        if (incognito) {
            options.addArguments("--incognito");
        }
        if (windowSize != null && !windowSize.isBlank()) {
            options.addArguments("--window-size=" + windowSize.replace("x", ","));
        }
        if (userDataDir != null && !userDataDir.isBlank()) {
            options.addArguments("--user-data-dir=" + userDataDir);
        }
        if (proxyUrl != null && !proxyUrl.isBlank()) {
            org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
            proxy.setHttpProxy(proxyUrl);
            proxy.setSslProxy(proxyUrl);
            options.setProxy(proxy);
        }
        if (browserVersion != null && !browserVersion.isBlank()) {
            options.setBrowserVersion(browserVersion);
        }
        // Pass-through raw arguments
        for (String arg : rawArguments) {
            options.addArguments(arg);
        }

        return options;
    }

    /** Returns a snapshot map of the current config (for JSON output). */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("headless", headless);
        m.put("maximize", maximize);
        m.put("incognito", incognito);
        m.put("windowSize", windowSize);
        m.put("userDataDir", userDataDir);
        m.put("proxyUrl", proxyUrl);
        m.put("browserVersion", browserVersion);
        m.put("pageLoadStrategy", pageLoadStrategy.toString());
        m.put("extraHeaders", extraHeaders);
        m.put("rawArguments", rawArguments);
        return m;
    }

    /** Reset to defaults — called on quit. Also removes the persisted config file. */
    public void reset() {
        headless = false;
        maximize = false;
        incognito = false;
        windowSize = null;
        userDataDir = null;
        proxyUrl = null;
        browserVersion = null;
        pageLoadStrategy = PageLoadStrategy.NORMAL;
        extraHeaders.clear();
        rawArguments.clear();
        deleteConfigFile();
    }

    // ── persistence ─────────────────────────────────────────────

    /**
     * Persist the current configuration to {@code .selenium-cli.json}.
     * Called automatically by {@link cli.commands.ConfigCommand} after changes.
     */
    public void save() {
        try {
            String json = JsonOutput.toJson(toMap());
            Files.writeString(Path.of(CONFIG_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Warning: failed to save config — " + e.getMessage());
        }
    }

    /**
     * Load configuration from {@code .selenium-cli.json} if it exists.
     * Called once at startup in {@link cli.SeleniumCli#main(String[])}.
     */
    public void load() {
        Path path = Path.of(CONFIG_FILE);
        if (!Files.exists(path)) return;
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            JsonObject obj = JsonOutput.gson().fromJson(json, JsonObject.class);
            if (obj == null) return;

            if (obj.has("headless"))        headless        = obj.get("headless").getAsBoolean();
            if (obj.has("maximize"))        maximize        = obj.get("maximize").getAsBoolean();
            if (obj.has("incognito"))       incognito       = obj.get("incognito").getAsBoolean();
            if (obj.has("windowSize")   && !obj.get("windowSize").isJsonNull())
                windowSize = obj.get("windowSize").getAsString();
            if (obj.has("userDataDir")  && !obj.get("userDataDir").isJsonNull())
                userDataDir = obj.get("userDataDir").getAsString();
            if (obj.has("proxyUrl")     && !obj.get("proxyUrl").isJsonNull())
                proxyUrl = obj.get("proxyUrl").getAsString();
            if (obj.has("browserVersion") && !obj.get("browserVersion").isJsonNull())
                browserVersion = obj.get("browserVersion").getAsString();
            if (obj.has("pageLoadStrategy"))
                pageLoadStrategy = PageLoadStrategy.fromString(obj.get("pageLoadStrategy").getAsString());
            if (obj.has("extraHeaders") && obj.get("extraHeaders").isJsonObject()) {
                JsonObject hdrs = obj.getAsJsonObject("extraHeaders");
                for (Map.Entry<String, JsonElement> e : hdrs.entrySet()) {
                    extraHeaders.put(e.getKey(), e.getValue().getAsString());
                }
            }
            if (obj.has("rawArguments") && obj.get("rawArguments").isJsonArray()) {
                JsonArray arr = obj.getAsJsonArray("rawArguments");
                for (JsonElement e : arr) {
                    rawArguments.add(e.getAsString());
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: failed to load config from " + CONFIG_FILE + " — " + e.getMessage());
        }
    }

    /** Delete the persisted config file. */
    public void deleteConfigFile() {
        try {
            Files.deleteIfExists(Path.of(CONFIG_FILE));
        } catch (IOException ignored) {}
    }

    /** @return the path of the config file. */
    public static String getConfigFileName() { return CONFIG_FILE; }
}
