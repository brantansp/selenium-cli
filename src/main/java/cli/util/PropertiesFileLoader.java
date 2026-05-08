package cli.util;

import cli.config.BrowserConfig;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a {@code .properties} file and maps recognised {@code browser.chrome.*} keys
 * to {@link BrowserConfig}. Supports two-pass variable interpolation:
 * <ul>
 *   <li>{@code ${sys:key}}                → {@link System#getProperty(String)}</li>
 *   <li>{@code ${const:pkg.Class.FIELD}}  → static field via reflection</li>
 *   <li>{@code ${name}}                   → chained reference to another property</li>
 * </ul>
 *
 * <p>Recognised key prefixes:</p>
 * <ul>
 *   <li>{@code browser.chrome.version}        → browserVersion</li>
 *   <li>{@code browser.chrome.arguments.*}    → addArgument(value)  (deduped)</li>
 *   <li>{@code browser.proxy.manual}          → proxyUrl</li>
 *   <li>{@code browser.chrome.preferences.*} → addPreference(suffix, coerced-value)</li>
 *   <li>{@code browser.chrome.capabilities.*}→ addCapability(suffix, coerced-value)</li>
 *   <li>All other {@code browser.*} keys     → warnings list</li>
 * </ul>
 *
 * <p>Returns a {@link LoadResult} with {@code applied} and {@code warnings} lists
 * that the caller surfaces in the JSON response.</p>
 */
public class PropertiesFileLoader {

    private static final Pattern INTERPOLATION = Pattern.compile("\\$\\{([^}]+)}");

    private PropertiesFileLoader() {}

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Load a properties file and apply all recognised settings to {@code config}.
     *
     * @param filePath path to the {@code .properties} file
     * @param config   the target {@link BrowserConfig} instance
     * @return a {@link LoadResult} listing applied settings and warnings
     * @throws IOException if the file cannot be read
     */
    public static LoadResult load(String filePath, BrowserConfig config) throws IOException {
        String raw = Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        Properties props = new Properties();
        props.load(new StringReader(raw));

        // Resolve variable interpolation across all values
        Properties resolved = resolve(props);

        List<String> applied  = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Track only within-this-file duplicates so each flag is reported once
        // even when the same value appears under multiple keys in the same file.
        // We intentionally do NOT seed from config.getRawArguments() — args that
        // were added in a previous load (and persisted to .selenium-cli.json) must
        // still appear in the applied list when the file is loaded again.
        Set<String> seenArgs = new LinkedHashSet<>();

        for (String key : resolved.stringPropertyNames()) {
            String value = resolved.getProperty(key).trim();
            if (value.isEmpty()) continue;

            if (key.equals("browser.chrome.version")) {
                config.browserVersion(value);
                applied.add("browserVersion=" + value);

            } else if (key.startsWith("browser.chrome.arguments.")) {
                // A single property value may contain multiple space-separated flags,
                // e.g. "--flag1=x --flag2=y"
                applyArguments(value, config, seenArgs, applied);

            } else if (key.equals("browser.proxy.manual")) {
                config.proxyUrl(value);
                applied.add("proxyUrl=" + value);

            } else if (key.startsWith("browser.chrome.preferences.")) {
                String prefKey = key.substring("browser.chrome.preferences.".length());
                Object coerced = coerce(value);
                config.addPreference(prefKey, coerced);
                applied.add("preference=" + prefKey + "=" + coerced);

            } else if (key.startsWith("browser.chrome.capabilities.")) {
                String capKey = key.substring("browser.chrome.capabilities.".length());
                Object coerced = coerce(value);
                config.addCapability(capKey, coerced);
                applied.add("capability=" + capKey + "=" + coerced);

            } else if (key.startsWith("browser.")) {
                // A browser.* key that we don't recognise — tell the user rather than silently drop
                warnings.add("skipped (unsupported key): " + key);
            }
            // Any other key (fs, download.directory, plugin.directory, userdata.directory, …)
            // is a pure interpolation variable — silently ignored.
        }

        return new LoadResult(applied, warnings);
    }

    // ── Argument splitting ───────────────────────────────────────────────────

    /**
     * Splits a value like {@code "--flag1=x --flag2=y"} on {@code " --"} boundaries,
     * deduplicates against already-registered arguments, and adds new ones to config.
     */
    private static void applyArguments(String value, BrowserConfig config,
                                       Set<String> seen, List<String> applied) {
        // Split on whitespace followed by -- (preserves leading -- on tokens after split)
        String[] parts = value.split("(?=\\s+--)|(?<=\\s)(?=--)");
        for (String raw : parts) {
            String arg = raw.trim();
            if (arg.isEmpty()) continue;
            if (!arg.startsWith("--") && !arg.startsWith("-")) {
                // Bare value – probably not a Chrome flag, skip with warning handled by caller
                continue;
            }
            if (seen.add(arg)) {
                config.addArgument(arg);
                applied.add("argument=" + arg);
            }
        }
    }

    // ── Variable interpolation ───────────────────────────────────────────────

    /**
     * Two-pass resolver: first resolves {@code ${sys:…}} and {@code ${const:…}}
     * tokens; a second pass resolves chained {@code ${name}} references.
     */
    static Properties resolve(Properties raw) {
        Map<String, String> interim = new LinkedHashMap<>();
        for (String key : raw.stringPropertyNames()) {
            interim.put(key, raw.getProperty(key));
        }

        // First pass: sys: and const:
        Map<String, String> firstPass = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : interim.entrySet()) {
            firstPass.put(entry.getKey(), interpolate(entry.getValue(), interim));
        }

        // Second pass: chained references (now resolved in firstPass)
        Properties out = new Properties();
        for (Map.Entry<String, String> entry : firstPass.entrySet()) {
            out.setProperty(entry.getKey(), interpolate(entry.getValue(), firstPass));
        }
        return out;
    }

    /**
     * Replace all {@code ${…}} placeholders in {@code value}.
     */
    static String interpolate(String value, Map<String, String> context) {
        Matcher m = INTERPOLATION.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String token = m.group(1);
            String replacement = resolveToken(token, context);
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String resolveToken(String token, Map<String, String> context) {
        if (token.startsWith("sys:")) {
            String sysProp = token.substring(4);
            String val = System.getProperty(sysProp);
            return val != null ? val : "${" + token + "}";
        }
        if (token.startsWith("const:")) {
            return resolveConst(token.substring(6));
        }
        // Chained reference to another property
        String chained = context.get(token);
        if (chained != null) {
            // Recurse only if the chained value still contains placeholders to avoid deep recursion
            return chained.contains("${") ? interpolate(chained, context) : chained;
        }
        return "${" + token + "}";  // leave unresolved
    }

    /**
     * Resolve {@code const:fully.qualified.ClassName.FIELD} via reflection.
     * Returns the raw token (still wrapped in {@code ${…}}) on any failure so the
     * caller can surface it as a warning rather than throwing.
     */
    static String resolveConst(String fullyQualified) {
        int lastDot = fullyQualified.lastIndexOf('.');
        if (lastDot < 0) return "${const:" + fullyQualified + "}";
        String className = fullyQualified.substring(0, lastDot);
        String fieldName = fullyQualified.substring(lastDot + 1);
        try {
            Class<?> cls = Class.forName(className);
            Field field  = cls.getField(fieldName);
            Object val   = field.get(null);
            return val != null ? val.toString() : "";
        } catch (Exception ignored) {
            return "${const:" + fullyQualified + "}";
        }
    }

    // ── Value coercion ───────────────────────────────────────────────────────

    /**
     * Coerce a raw string property value to the most specific Java type:
     * {@code "true"}/{@code "false"} → {@link Boolean},
     * digit strings  → {@link Integer},
     * everything else → {@link String}.
     */
    static Object coerce(String value) {
        if ("true".equalsIgnoreCase(value))  return Boolean.TRUE;
        if ("false".equalsIgnoreCase(value)) return Boolean.FALSE;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {}
        return value;
    }

    // ── Result container ─────────────────────────────────────────────────────

    /**
     * Immutable result object returned by {@link #load}.
     */
    public static final class LoadResult {
        private final List<String> applied;
        private final List<String> warnings;

        public LoadResult(List<String> applied, List<String> warnings) {
            this.applied  = Collections.unmodifiableList(new ArrayList<>(applied));
            this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        }

        public List<String> getApplied()  { return applied; }
        public List<String> getWarnings() { return warnings; }
    }
}

