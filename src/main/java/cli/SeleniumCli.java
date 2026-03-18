package cli;

import cli.commands.*;
import cli.completions.ChromeArgCandidates;
import cli.completions.CommaSplitCompleter;
import cli.model.CommandResult;
import cli.session.SessionManager;
import org.jline.reader.impl.completer.AggregateCompleter;
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

    // ── ANSI color constants ────────────────────────────────────
    private static final String CYAN    = "\u001B[36m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String BOLD    = "\u001B[1m";
    private static final String RESET   = "\u001B[0m";

    private static final String BANNER =
            CYAN + "┌─────────────────────────────────────┐\n" +
            "│" + BOLD + GREEN + "   Selenium CLI  v1.0.0              " + RESET + CYAN + "│\n" +
            "│" + YELLOW + "   Type 'help' or '--help' for usage " + CYAN + "│\n" +
            "│" + YELLOW + "   Type 'exit' to leave              " + CYAN + "│\n" +
            "└─────────────────────────────────────┘" + RESET + "\n";

    public static void main(String[] args) {
        // Suppress Selenium's internal JUL warnings so only clean JSON hits stdout/stderr
        Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);

        CommandLine cli = buildCommandLine();

        if (args.length > 0) {
            // One-shot mode: execute the command and exit
            int exitCode = cli.execute(args);
            System.exit(exitCode);
        } else {
            // REPL mode
            new SeleniumCli().repl();
        }
    }

    /** Called by Picocli when 'selenium' is invoked with no subcommand in one-shot mode. */
    @Override
    public void run() {
        // If invoked via Picocli with no subcommand, start the REPL
        repl();
    }

    // ── REPL ────────────────────────────────────────────────────

    private void repl() {

        // Ensure browser is closed when the JVM shuts down (Ctrl+C, etc.)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (SessionManager.getInstance().isActive()) {
                SessionManager.getInstance().shutdown();
            }
        }));

        // Force Picocli to use ANSI colors (monochrome=false)
        CommandLine cli = buildCommandLine();
        cli.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.ON));

        try {
            // ── JLine3 terminal ──────────────────────────────────
            Terminal terminal = TerminalBuilder.builder()
                    .system(true)
                    .color(true)       // force color support on the terminal
                    .build();

            terminal.writer().println(BANNER);
            terminal.writer().flush();

            // ── Picocli → JLine completers ───────────────────────
            PicocliCommands picocliCommands = new PicocliCommands(cli);
            SystemCompleter systemCompleter = picocliCommands.compileCompleters();
            systemCompleter.compile();

            // ── Comma-split completer for --options=val1,val2,... ─
            List<String> chromeArgs = new ArrayList<>();
            new ChromeArgCandidates().forEach(chromeArgs::add);
            CommaSplitCompleter commaSplitCompleter = new CommaSplitCompleter(
                    Map.of("--options", chromeArgs)
            );

            // Combine both completers so standard + comma-split both work
            AggregateCompleter combinedCompleter = new AggregateCompleter(
                    systemCompleter, commaSplitCompleter
            );

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
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) continue;

                // Exit the REPL entirely
                if (line.equalsIgnoreCase("exit")) {
                    if (SessionManager.getInstance().isActive()) {
                        SessionManager.getInstance().shutdown();
                    }
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
     * Simple tokenizer that respects double-quoted strings.
     * <p>
     * {@code type #email "hello world"} → ["type", "#email", "hello world"]
     */
    static String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
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
