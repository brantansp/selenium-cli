# SELENIUM-CLI — LLM CONTEXT FILE
# Last updated: 2026-03-31
#
# This file describes the project structure, patterns, and the exact
# step-by-step checklist to follow when creating or modifying commands.
# Feed this file to any LLM so it can make changes without guessing.

## PROJECT OVERVIEW

Selenium CLI is an interactive REPL + one-shot CLI that wraps Selenium
WebDriver as simple shell commands. Every command returns structured JSON.
Built with Java 17, Picocli (CLI framework), JLine3 (terminal/autocomplete),
Selenium 4.28, and Gson (JSON).

## DIRECTORY STRUCTURE

```
selenium-cli/
├── pom.xml                              # Maven build (Shade plugin → fat JAR)
├── selenium                             # Unix wrapper script
├── selenium.bat                         # Windows wrapper script
├── .selenium-cli.json                   # Persisted config (auto-created, auto-deleted)
├── README.md                            # User-facing documentation
├── CONTEXT.md                           # THIS FILE — LLM project context
├── examples/
│   └── test.json                        # Sample batch JSON file
└── src/main/java/cli/
    ├── SeleniumCli.java                 # Entry point + REPL + subcommand registry + tab-completion setup
    ├── commands/                         # One file per command (Picocli @Command + Runnable)
    │   ├── OpenCommand.java
    │   ├── ClickCommand.java
    │   ├── DblClickCommand.java
    │   ├── RightClickCommand.java
    │   ├── TypeCommand.java
    │   ├── ClearCommand.java
    │   ├── SubmitCommand.java
    │   ├── SelectCommand.java
    │   ├── KeysCommand.java
    │   ├── GetTextCommand.java
    │   ├── GetAttrCommand.java
    │   ├── HoverCommand.java
    │   ├── DragDropCommand.java
    │   ├── ScrollCommand.java
    │   ├── HighlightCommand.java
    │   ├── ScreenshotCommand.java
    │   ├── NavigateCommand.java
    │   ├── WaitCommand.java
    │   ├── ExecuteJsCommand.java
    │   ├── SwitchFrameCommand.java
    │   ├── SwitchWindowCommand.java
    │   ├── TabsCommand.java
    │   ├── UrlCommand.java
    │   ├── TitleCommand.java
    │   ├── ConfigCommand.java
    │   ├── RunCommand.java
    │   ├── SessionCommand.java
    │   └── QuitCommand.java
    ├── completions/                      # Tab-completion candidate providers (Iterable<String>)
    │   ├── AttributeCandidates.java
    │   ├── BrowserVersionCandidates.java
    │   ├── ChromeArgCandidates.java
    │   ├── CommaSplitCompleter.java       # Custom JLine Completer for comma-separated values
    │   ├── HeaderCandidates.java
    │   ├── JsSnippetCandidates.java
    │   ├── KeyCandidates.java
    │   ├── LocatorCandidates.java
    │   ├── NavigationCandidates.java
    │   ├── ScreenshotPathCandidates.java
    │   ├── UrlCandidates.java
    │   ├── WaitSecondsCandidates.java
    │   └── WindowSizeCandidates.java
    ├── config/
    │   └── BrowserConfig.java             # Singleton — Chrome options, persists to .selenium-cli.json
    ├── model/
    │   ├── CommandResult.java             # JSON envelope: success()/error() → print()
    │   └── CommandRequest.java            # POJO for batch JSON: { "command": "...", "args": [...] }
    ├── session/
    │   ├── SessionManager.java            # Singleton — owns the live ChromeDriver
    │   └── NoActiveSessionException.java
    └── util/
        ├── JsonOutput.java                # Shared Gson instance (pretty-print, no HTML escaping)
        ├── LocatorParser.java             # Parses locator string → Selenium By object
        └── SessionRecorder.java           # Singleton — records REPL commands → session-<ts>.json
```

## KEY SINGLETONS

| Class | Access | Purpose |
|---|---|---|
| `SessionManager.getInstance()` | `.getDriverOrThrow()` returns `ChromeDriver` | Owns the single browser session |
| `BrowserConfig.getInstance()` | `.isHeadless()`, `.toChromeOptions()`, `.save()`, `.load()` | Accumulates Chrome config, persists to disk |
| `SessionRecorder.getInstance()` | `.record(tokens)`, `.isEnabled()`, `.save()` | Records commands for replay |

## COMMAND PATTERN (every command follows this)

```java
package cli.commands;

import cli.completions.LocatorCandidates;   // if command takes a locator
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;               // if command takes a locator
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;       // positional args
import picocli.CommandLine.Option;           // named flags

import java.util.List;

@Command(name = "COMMAND_NAME", description = "Short description")
public class XxxCommand implements Runnable {

    @Parameters(index = "0", description = "...",
            completionCandidates = LocatorCandidates.class)  // optional
    private String locator;

    @Option(names = "--flag", description = "...")
    private boolean flag;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            // ... Selenium logic ...
            CommandResult.success("COMMAND_NAME", List.of(locator), resultObj).print();
        } catch (Exception e) {
            CommandResult.error("COMMAND_NAME", List.of(locator), e.getMessage()).print();
        }
    }
}
```

## COMPLETION CANDIDATE PATTERN

```java
package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class XxxCandidates implements Iterable<String> {
    private static final List<String> CANDIDATES = Arrays.asList(
            "value1", "value2", "value3"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}
```

Requirements:
- Must implement `Iterable<String>`
- Must have a public no-arg constructor
- Picocli instantiates it via reflection

## REUSABLE COMPLETION CANDIDATES (use these before creating new ones)

| Class | Values | Typically used by |
|---|---|---|
| `LocatorCandidates` | xpath=, css=, #id, .class templates | Any command with `<locator>` param |
| `AttributeCandidates` | 35+ HTML attrs (id, href, src, data-testid...) | `getattr` |
| `UrlCandidates` | https://, http://localhost, file:/// | `open` |
| `ChromeArgCandidates` | 45+ Chrome flags (--headless, --disable-gpu...) | `open --options`, `config --options` |
| `KeyCandidates` | ENTER, TAB, ESCAPE, CONTROL+a, etc. | `keys` |
| `NavigationCandidates` | back, forward, refresh | `navigate` |
| `WaitSecondsCandidates` | 1, 2, 3, 5, 10, 15, 30, 60 | `wait` |
| `JsSnippetCandidates` | 30+ JS snippets | `execute` |
| `ScreenshotPathCandidates` | screenshot.png, screenshots/page.png | `screenshot` |
| `WindowSizeCandidates` | 1920x1080, 1366x768, 375x812... | `config --window-size` |
| `BrowserVersionCandidates` | stable, beta, dev, 130-134 | `config --browser-version` |
| `HeaderCandidates` | Authorization:Bearer, Content-Type... | `config --header` |

---

## STEP-BY-STEP: ADDING A NEW COMMAND

Follow these steps IN ORDER. Every step is mandatory.

### STEP 1 — Create the command class

File: `src/main/java/cli/commands/<Name>Command.java`

- Package: `cli.commands`
- Annotate: `@Command(name = "cmdname", description = "...")`
- Implement: `Runnable`
- Use `@Parameters` for positional args, `@Option` for flags
- Get driver: `SessionManager.getInstance().getDriverOrThrow()`
- Parse locators: `LocatorParser.parse(locator)`
- Return JSON: `CommandResult.success(...)` or `CommandResult.error(...)`
- Wrap everything in try/catch

### STEP 2 — (If needed) Create a completion candidates class

File: `src/main/java/cli/completions/<Name>Candidates.java`

- Only if the command has a positional parameter with UNIQUE suggestions
  that don't already exist in the reusable candidates listed above.
- Implements `Iterable<String>` with a public no-arg constructor.
- Wire it via `completionCandidates = XxxCandidates.class` on the `@Parameters`
  or `@Option` annotation in the command class.

### STEP 3 — Register the command in SeleniumCli.java

File: `src/main/java/cli/SeleniumCli.java`

Location: The `@Command` annotation's `subcommands` array (around line 56-85).

```java
subcommands = {
    OpenCommand.class,
    ClickCommand.class,
    // ... existing commands ...
    NewCommand.class,          // ← ADD HERE (keep logical grouping)
    ConfigCommand.class,       // config/session/run/quit stay at the end
    RunCommand.class,
    SessionCommand.class,
    QuitCommand.class
}
```

### STEP 4 — Register tab-completion for positional parameters

File: `src/main/java/cli/SeleniumCli.java`

Location: The `buildParameterCompleters()` method (around line 452-571).

IMPORTANT: This step is ONLY needed for `@Parameters` (positional args).
`@Option` completion candidates are handled automatically by Picocli.

Add an `ArgumentCompleter` for the command:

```java
// cmdname <locator>
completers.add(new ArgumentCompleter(
        new StringsCompleter("cmdname"),
        new StringsCompleter(iterableToList(new LocatorCandidates())),
        NullCompleter.INSTANCE));
```

For multiple positional params, add one completer per position:

```java
// cmdname <source-locator> <target-locator>
completers.add(new ArgumentCompleter(
        new StringsCompleter("cmdname"),
        new StringsCompleter(iterableToList(new LocatorCandidates())),
        new StringsCompleter(iterableToList(new LocatorCandidates())),
        NullCompleter.INSTANCE));
```

Commands with NO positional parameters (only --options) do NOT need this step.

### STEP 5 — Add to README.md

File: `README.md`

Two locations must be updated:

#### 5a. Table of Contents (around line 70-99)

Add an entry in the correct position:

```markdown
  - [cmdname](#cmdname)
```

#### 5b. Commands Reference section

Add a documentation block in the same position within the Commands Reference.
Follow the existing pattern:

```markdown
---

### `cmdname`

One-line description.

\```
cmdname <param> [--option <value>]
\```

| Parameter | Required | Description |
|---|---|---|
| `param` | Yes | ... |
| `--option` | No | ... |

**Examples:**

\```bash
selenium> cmdname #element
selenium> cmdname .class --option value
\```
```

### STEP 6 — Build and verify

```bash
mvn package -q
```

---

## COMMAND GROUPING ORDER (in subcommands array and README)

The commands are grouped logically:

1. **Navigation**: open, navigate
2. **Mouse actions**: click, dblclick, rightclick, hover, dragdrop
3. **Keyboard/Input**: type, clear, submit, select, keys
4. **Reading data**: gettext, getattr, url, title
5. **Visual**: scroll, highlight, screenshot
6. **JavaScript**: execute
7. **Frames/Windows**: switchframe, switchwindow, tabs
8. **Utility**: wait
9. **Meta/Config**: config, session, run, quit, exit

When adding a new command, place it in the appropriate group.

---

## IMPORTANT DETAILS

### Locator syntax (auto-detected by LocatorParser.parse())
- `#id` → By.id
- `.class` → By.cssSelector
- `//xpath` or `(//xpath)` → By.xpath
- `css=...` / `xpath=...` / `id=...` / `name=...` / `tag=...` → explicit
- anything else → By.cssSelector (fallback)

### JSON output envelope (CommandResult)
Every command MUST return structured JSON via:
- `CommandResult.success("cmdname", List.of(args...), resultObject).print()`
- `CommandResult.error("cmdname", List.of(args...), errorMessage).print()`

The first arg to `success`/`error` MUST match the `@Command(name = "...")`.

### Session recording
`SessionRecorder` records commands automatically. Meta-commands excluded:
`help`, `exit`, `session`, `run`, `--help`, `-h`.
If your new command is a meta/utility command that should NOT be recorded,
add it to `EXCLUDED_COMMANDS` in `SessionRecorder.java`.

### Config persistence
`BrowserConfig` persists to `.selenium-cli.json`. If your new command adds
a configurable option, add it to:
1. `BrowserConfig` (field + getter + setter + toMap() + reset() + load())
2. `ConfigCommand` (@Option + apply logic + save)
3. `SeleniumCli.buildStartupOptionsBlock()` (display at startup)

### Build command
```bash
mvn package -q          # quick build (reuses target/)
mvn clean package -q    # full clean build
```
The fat JAR is at: `target/selenium-cli-1.0.0.jar`

---

## QUICK REFERENCE: FILES TO MODIFY PER TASK

### Adding a new command:
1. `src/main/java/cli/commands/NewCommand.java` — CREATE
2. `src/main/java/cli/completions/NewCandidates.java` — CREATE (if needed)
3. `src/main/java/cli/SeleniumCli.java` — subcommands array + buildParameterCompleters()
4. `README.md` — TOC + Commands Reference section

### Adding a new config option:
1. `src/main/java/cli/config/BrowserConfig.java` — field, getter, setter, toMap(), reset(), load()
2. `src/main/java/cli/commands/ConfigCommand.java` — @Option + apply logic
3. `src/main/java/cli/SeleniumCli.java` — buildStartupOptionsBlock()
4. `README.md` — config table + Startup Options section

### Adding a new reusable completion:
1. `src/main/java/cli/completions/NewCandidates.java` — CREATE
2. Wire via `completionCandidates = NewCandidates.class` on the @Parameters/@Option
3. `src/main/java/cli/SeleniumCli.java` — buildParameterCompleters() (if positional)

