package cli;

import cli.commands.*;
import cli.completions.*;
import cli.config.BrowserConfig;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.SessionRecorder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.completer.SystemCompleter;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.TailTipWidgets;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExecutionExceptionHandler;
import picocli.shell.jline3.PicocliCommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Selenium CLI — an interactive REPL that exposes Selenium WebDriver as
 * simple shell commands. All output is structured JSON.
 *
 * <h3>Usage</h3>
 * <pre>
 *   java -jar selenium-cli.jar                  → starts the REPL
 *   java -jar selenium-cli.jar open https://...  → one-shot mode
 * </pre>
 *
 * <h3>Adding new commands</h3>
 * <ol>
 *   <li>Create a class under {@code cli.commands} annotated with {@code @Command}
 *       implementing {@code Runnable}.</li>
 *   <li>Add it to the {@code subcommands} list below.</li>
 * </ol>
 * <!-- TODO: migrate to ServiceLoader for plugin support -->
 */
@Command(
        name = "selenium",
        version = "selenium-cli 1.0.0",
        description = "Interactive Selenium WebDriver CLI",
        mixinStandardHelpOptions = true,
        subcommands = {
                OpenCommand.class,
                ClickCommand.class,
                TypeCommand.class,
                GetTextCommand.class,
                GetAttrCommand.class,
                ScreenshotCommand.class,
                NavigateCommand.class,
                WaitCommand.class,
                ExecuteJsCommand.class,
                ConfigCommand.class,
                RunCommand.class,
                SessionCommand.class,
                QuitCommand.class
        }
)
public class SeleniumCli implements Runnable {

    @CommandLine.Option(names = "--no-record",
            description = "Disable automatic session recording (recording is ON by default)")
    private boolean noRecord;

    // ── ANSI color constants ────────────────────────────────────
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BOLD = "\u001B[1m";
    private static final String RESET = "\u001B[0m";

    // CLI prompt icon (> ✓ ━) rendered as text art
    private static final String L = CYAN;   // border
    private static final String C = BOLD;   // chevron + bar (charcoal)
    private static final String G = GREEN;  // checkmark (green)
    private static final String R = RESET;
    private static final String BANNER = String.join(System.lineSeparator(),
            "",
            L + "         #################################################################        " + R,
            L + "       ####" + R + "                                                             " + L + "####      " + R,
            L + "     ###" + R + "                                                                  " + L + "###     " + R,
            L + "     ###" + R + "                                                        " + G + "+++++" + R + "     " + L + "###     " + R,
            L + "     ###" + R + "                                                      " + G + "++++++" + R + "      " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "####" + R + "                                         " + G + "++++++" + R + "        " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "#######" + R + "                             " + G + "+++" + R + "     " + G + "++++++" + R + "         " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "##########" + R + "                         " + G + "++++++" + R + " " + G + "++++++" + R + "           " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "#############" + R + "                       " + G + "+++++++++++" + R + "            " + L + "###     " + R,
            L + "     ###" + R + "        " + C + "###############" + R + "                      " + G + "+++++++" + R + "              " + L + "###     " + R,
            L + "     ###" + R + "            " + C + "##############" + R + "                    " + G + "+++++" + R + "               " + L + "###     " + R,
            L + "     ###" + R + "              " + C + "###############" + R + "                                     " + L + "###     " + R,
            L + "     ###" + R + "                 " + C + "###############" + R + "                                  " + L + "###     " + R,
            L + "     ###" + R + "                    " + C + "##############" + R + "                                " + L + "###     " + R,
            L + "     ###" + R + "                       " + C + "##############" + R + "                             " + L + "###     " + R,
            L + "     ###" + R + "                    " + C + "################" + R + "                              " + L + "###     " + R,
            L + "     ###" + R + "                  " + C + "###############" + R + "                                 " + L + "###     " + R,
            L + "     ###" + R + "               " + C + "##############" + R + "                                     " + L + "###     " + R,
            L + "     ###" + R + "            " + C + "##############" + R + "                                        " + L + "###     " + R,
            L + "     ###" + R + "        " + C + "################" + R + "                 " + C + "###################" + R + "      " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "#############" + R + "                     " + C + "###################" + R + "      " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "###########" + R + "                                                " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "########" + R + "                                                   " + L + "###     " + R,
            L + "     ###" + R + "       " + C + "#####" + R + "                                                      " + L + "###     " + R,
            L + "     ###" + R + "                                                                  " + L + "###     " + R,
            L + "       ###" + R + "                                                               " + L + "###      " + R,
            L + "        ###################################################################       " + R,
            "",
            BOLD + GREEN + "     Selenium CLI  " + R + YELLOW + "v1.0.0" + R,
            YELLOW + "     Type 'help' or '--help' for usage" + R,
            YELLOW + "     Type 'exit' to leave" + R,
            "");

    public static void main(String[] args) {
        // Suppress Selenium's internal JUL warnings so only clean JSON hits stdout/stderr
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);

        // Check for --no-record before Picocli routing
        boolean noRecordFlag = false;
        List<String> filteredArgs = new ArrayList<>();
        for (String arg : args) {
            if ("--no-record".equals(arg)) {
                noRecordFlag = true;
            } else {
                filteredArgs.add(arg);
            }
        }

        if (!filteredArgs.isEmpty()) {
            // One-shot mode: execute the command and exit (no recording in one-shot)
            CommandLine cli = buildCommandLine();
            int exitCode = cli.execute(filteredArgs.toArray(new String[0]));
            System.exit(exitCode);
        } else {
            // REPL mode
            SeleniumCli app = new SeleniumCli();
            app.noRecord = noRecordFlag;
            app.repl();
        }
    }

    /**
     * Called by Picocli when 'selenium' is invoked with no subcommand in one-shot mode.
     */
    @Override
    public void run() {
        // If invoked via Picocli with no subcommand, start the REPL
        repl();
    }

    // ── REPL ────────────────────────────────────────────────────

    private void repl() {

        // ── Session recorder: singleton, ON by default, togglable via config --record ───
        SessionRecorder recorder = SessionRecorder.getInstance();
        if (noRecord) recorder.disable();

        // Ensure browser is closed when the JVM shuts down (Ctrl+C, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            saveRecording(recorder);
            if (SessionManager.getInstance().isActive()) {
                SessionManager.getInstance().shutdown();
            }
        }));

        // Force Picocli to use ANSI colors (monochrome=false)
        CommandLine cli = buildCommandLine();
        cli.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));

        try {
            // Print banner via System.out BEFORE JLine takes over the terminal.
            // Writing through terminal.writer() is tracked by JLine's Display
            // engine, and readLine() overwrites it when it draws the prompt.
            System.out.println(BANNER);
            System.out.println(buildStartupOptionsBlock());
            System.out.flush();

            // ── JLine3 terminal ──────────────────────────────────
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .color(true)       // force color support on the terminal
                    .build();


            // ── Picocli → JLine completers ───────────────────────
            PicocliCommands picocliCommands = new PicocliCommands(cli);
            SystemCompleter systemCompleter = picocliCommands.compileCompleters();
            systemCompleter.compile();

            // ── Positional-parameter completers ──────────────────
            // PicocliCommands.compileCompleters() handles command names
            // and --option completions, but does NOT propagate
            // completionCandidates from @Parameters into JLine.
            // We fix this by adding explicit ArgumentCompleters for
            // each command that has positional parameters.
            List<Completer> paramCompleters = buildParameterCompleters();

            // ── Comma-split completer for --options=val1,val2,... ─
            List<String> chromeArgs = new ArrayList<>();
            new ChromeArgCandidates().forEach(chromeArgs::add);
            CommaSplitCompleter commaSplitCompleter = new CommaSplitCompleter(
                    Map.of("--options", chromeArgs)
            );

            // Combine all completers: picocli system + positional params + comma-split
            List<Completer> allCompleters = new ArrayList<>();
            allCompleters.add(systemCompleter);
            allCompleters.addAll(paramCompleters);
            allCompleters.add(commaSplitCompleter);
            AggregateCompleter combinedCompleter = new AggregateCompleter(allCompleters);

            // ── Build the LineReader with autocomplete ───────────
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(combinedCompleter)
                    .parser(new DefaultParser())
                    .variable(LineReader.LIST_MAX, 50)   // max items in completion menu
                    .build();

            // ── TailTip widgets: show parameter hints as you type
            TailTipWidgets tailTips = new TailTipWidgets(
                    reader,
                    cmdLine -> picocliCommands.commandDescription(cmdLine.getArgs()),
                    5,
                    TailTipWidgets.TipType.COMPLETER
            );
            tailTips.enable();

            // ── REPL loop ────────────────────────────────────────
            String prompt = BOLD + CYAN + "selenium> " + RESET;
            String line;
            while (true) {
                try {
                    line = reader.readLine(prompt);
                } catch (UserInterruptException e) {
                    // Ctrl+C — ignore and re-prompt
                    continue;
                } catch (EndOfFileException e) {
                    // Ctrl+D / EOF — exit gracefully
                    saveRecording(recorder);
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                // Exit the REPL entirely
                if (line.equalsIgnoreCase("exit")) {
                    if (SessionManager.getInstance().isActive()) {
                        SessionManager.getInstance().shutdown();
                    }
                    saveRecording(recorder);
                    CommandResult.success("exit", Collections.emptyList(),
                            "Goodbye.").print();
                    break;
                }

                // 'help' shortcut
                if (line.equalsIgnoreCase("help")) {
                    cli.usage(System.out);
                    continue;
                }

                // Tokenize and execute
                String[] tokens = tokenize(line);
                if (tokens.length == 0) continue;

                // Record the command for session replay (respects enabled/disabled state)
                recorder.record(tokens);

                cli.execute(tokens);
            }

            terminal.close();
        } catch (Exception e) {
            CommandResult.error("repl", Collections.emptyList(),
                    "REPL error: " + e.getMessage()).print();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    /**
     * Build a coloured block that shows the status of every startup option.
     * Printed once, right after the banner.
     */
    private String buildStartupOptionsBlock() {
        BrowserConfig cfg = BrowserConfig.getInstance();

        String DIM   = "\u001B[2m";   // dim / faint
        String ON    = GREEN;          // green for enabled
        String OFF   = DIM;            // dim for disabled
        String LBL   = YELLOW;        // label colour

        StringBuilder sb = new StringBuilder();
        sb.append(BOLD).append(CYAN).append("     ── Startup Options ──────────────────────────").append(RESET).append("\n");

        // CLI-level option
        appendOption(sb, "Session Recording", SessionRecorder.getInstance().isEnabled(), LBL, ON, OFF);

        // Browser config options
        appendOption(sb, "Headless",          cfg.isHeadless(),   LBL, ON, OFF);
        appendOption(sb, "Maximize",          cfg.isMaximize(),   LBL, ON, OFF);
        appendOption(sb, "Incognito",         cfg.isIncognito(),  LBL, ON, OFF);

        // Value-based options (show value when set, "off" when not)
        appendValueOption(sb, "Window Size",      cfg.getWindowSize(),     LBL, ON, OFF);
        appendValueOption(sb, "User Data Dir",    cfg.getUserDataDir(),    LBL, ON, OFF);
        appendValueOption(sb, "Proxy",            cfg.getProxyUrl(),       LBL, ON, OFF);
        appendValueOption(sb, "Browser Version",  cfg.getBrowserVersion(), LBL, ON, OFF);

        // List options
        appendListOption(sb, "Extra Headers",     cfg.getExtraHeaders().isEmpty() ? null : cfg.getExtraHeaders().keySet().toString(), LBL, ON, OFF);
        appendListOption(sb, "Chrome Arguments",  cfg.getRawArguments().isEmpty()  ? null : String.join(", ", cfg.getRawArguments()), LBL, ON, OFF);

        sb.append(BOLD).append(CYAN).append("     ─────────────────────────────────────────────").append(RESET).append("\n");
        sb.append(DIM).append("     Use 'config --<option> true/false' to change at runtime").append(RESET).append("\n");
        sb.append(DIM).append("     Use 'config --show' to view current settings").append(RESET).append("\n");
        return sb.toString();
    }

    private static void appendOption(StringBuilder sb, String label, boolean enabled,
                                     String lblColor, String onColor, String offColor) {
        String status = enabled
                ? onColor  + "ENABLED"  + RESET
                : offColor + "disabled" + RESET;
        sb.append("     ").append(lblColor).append(String.format("%-20s", label)).append(RESET)
          .append(" : ").append(status).append("\n");
    }

    private static void appendValueOption(StringBuilder sb, String label, String value,
                                          String lblColor, String onColor, String offColor) {
        String status = (value != null && !value.isBlank())
                ? onColor  + value     + RESET
                : offColor + "off"     + RESET;
        sb.append("     ").append(lblColor).append(String.format("%-20s", label)).append(RESET)
          .append(" : ").append(status).append("\n");
    }

    private static void appendListOption(StringBuilder sb, String label, String value,
                                         String lblColor, String onColor, String offColor) {
        String status = (value != null)
                ? onColor  + value + RESET
                : offColor + "off" + RESET;
        sb.append("     ").append(lblColor).append(String.format("%-20s", label)).append(RESET)
          .append(" : ").append(status).append("\n");
    }

    /**
     * Write the session recording to a JSON file (if any commands were recorded).
     * The file uses the same format as {@code run --json} input, so it can be
     * replayed directly.
     */
    private static void saveRecording(SessionRecorder recorder) {
        if (!recorder.hasRecords()) return;
        try {
            Path saved = recorder.save();
            System.out.println("{\"session_recorded\": \"" + saved.toString().replace("\\", "\\\\") + "\"}");
        } catch (Exception e) {
            System.err.println("Warning: failed to save session recording — " + e.getMessage());
        }
    }

    /**
     * Build a configured {@link CommandLine} with JSON error handling.
     */
    private static CommandLine buildCommandLine() {
        CommandLine cli = new CommandLine(new SeleniumCli());

        // Override Picocli's default exception handler to emit JSON errors
        cli.setExecutionExceptionHandler(jsonExceptionHandler());
        cli.setParameterExceptionHandler(jsonParameterExceptionHandler());

        return cli;
    }

    /**
     * Handles exceptions thrown during command execution → JSON error output.
     */
    private static IExecutionExceptionHandler jsonExceptionHandler() {
        return (ex, commandLine, parseResult) -> {
            String cmdName = commandLine.getCommandName();
            CommandResult.error(cmdName, Collections.emptyList(), ex.getMessage()).print();
            return 1;
        };
    }

    /**
     * Handles bad user input (missing params, unknown options) → JSON error output.
     */
    private static CommandLine.IParameterExceptionHandler jsonParameterExceptionHandler() {
        return (ex, args) -> {
            CommandResult.error("selenium", Collections.emptyList(), ex.getMessage()).print();
            return 1;
        };
    }

    /**
     * Build JLine {@link ArgumentCompleter}s that provide tab-completion for
     * each command's positional {@code @Parameters} with {@code completionCandidates}.
     * <p>
     * {@code PicocliCommands.compileCompleters()} only generates completers for
     * command names and {@code --option} names/values. Positional parameter
     * candidates are <b>not</b> propagated, so we register them here explicitly.
     */
    private static List<Completer> buildParameterCompleters() {
        List<Completer> completers = new ArrayList<>();

        // open <url>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("open"),
                new StringsCompleter(iterableToList(new UrlCandidates())),
                NullCompleter.INSTANCE));

        // click <locator>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("click"),
                new StringsCompleter(iterableToList(new LocatorCandidates())),
                NullCompleter.INSTANCE));

        // type <locator> <text>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("type"),
                new StringsCompleter(iterableToList(new LocatorCandidates())),
                NullCompleter.INSTANCE));

        // gettext <locator>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("gettext"),
                new StringsCompleter(iterableToList(new LocatorCandidates())),
                NullCompleter.INSTANCE));

        // getattr <locator> <attribute>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("getattr"),
                new StringsCompleter(iterableToList(new LocatorCandidates())),
                new StringsCompleter(iterableToList(new AttributeCandidates())),
                NullCompleter.INSTANCE));

        // navigate <direction>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("navigate"),
                new StringsCompleter(iterableToList(new NavigationCandidates())),
                NullCompleter.INSTANCE));

        // wait <seconds>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("wait"),
                new StringsCompleter(iterableToList(new WaitSecondsCandidates())),
                NullCompleter.INSTANCE));

        // execute <script>
        completers.add(new ArgumentCompleter(
                new StringsCompleter("execute"),
                new StringsCompleter(iterableToList(new JsSnippetCandidates())),
                NullCompleter.INSTANCE));

        // screenshot [path]
        completers.add(new ArgumentCompleter(
                new StringsCompleter("screenshot"),
                new StringsCompleter(iterableToList(new ScreenshotPathCandidates())),
                NullCompleter.INSTANCE));

        return completers;
    }

    /**
     * Convert any {@code Iterable<String>} to a {@code List<String>}.
     */
    private static List<String> iterableToList(Iterable<String> iterable) {
        List<String> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    /**
     * Tokenizer that respects both single- and double-quoted strings.
     * <p>
     * Quotes that begin a new token are treated as <b>grouping</b> quotes and
     * are stripped from the result (shell-style):
     * <pre>  type #email "hello world" → ["type", "#email", "hello world"]</pre>
     * <p>
     * Quotes that appear <b>inside</b> an existing token (e.g. embedded in an
     * XPath expression) are preserved literally while still preventing the
     * enclosed spaces from splitting the token:
     * <pre>  click xpath=(//*[@value='Google Search'])[2]
     *       → ["click", "xpath=(//*[@value='Google Search'])[2]"]</pre>
     */
    static String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        boolean stripQuotes = false; // true when quoting started at a token boundary

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (!inQuotes && (c == '"' || c == '\'')) {
                // Opening quote
                inQuotes = true;
                quoteChar = c;
                if (current.isEmpty()) {
                    // Quote starts a new token → grouping quote, strip it
                    stripQuotes = true;
                } else {
                    // Quote is mid-token (e.g. inside XPath) → keep it literal
                    stripQuotes = false;
                    current.append(c);
                }
            } else if (inQuotes && c == quoteChar) {
                // Closing quote
                inQuotes = false;
                if (!stripQuotes) {
                    current.append(c); // preserve embedded closing quote
                }
                quoteChar = 0;
                stripQuotes = false;
            } else if (c == ' ' && !inQuotes) {
                if (!current.isEmpty()) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) {
            tokens.add(current.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
