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
├── src/main/java/cli/
│   ├── SeleniumCli.java                 # Entry point + REPL + subcommand registry + tab-completion setup
│   ├── commands/                         # One file per command (Picocli @Command + Runnable)
│   │   ├── OpenCommand.java
│   │   ├── ClickCommand.java
│   │   ├── DblClickCommand.java
│   │   ├── RightClickCommand.java
│   │   ├── TypeCommand.java
│   │   ├── ClearCommand.java
│   │   ├── SubmitCommand.java
│   │   ├── SelectCommand.java
│   │   ├── KeysCommand.java
│   │   ├── GetTextCommand.java
│   │   ├── GetAttrCommand.java
│   │   ├── HoverCommand.java
│   │   ├── DragDropCommand.java
│   │   ├── ScrollCommand.java
│   │   ├── HighlightCommand.java
│   │   ├── ScreenshotCommand.java
│   │   ├── NavigateCommand.java
│   │   ├── WaitCommand.java
│   │   ├── ExecuteJsCommand.java
│   │   ├── SwitchFrameCommand.java
│   │   ├── SwitchWindowCommand.java
│   │   ├── TabsCommand.java
│   │   ├── UrlCommand.java
│   │   ├── TitleCommand.java
│   │   ├── ConfigCommand.java
│   │   ├── RunCommand.java
│   │   ├── HistoryCommand.java
│   │   ├── SessionCommand.java
│   │   └── QuitCommand.java
│   ├── completions/                      # Tab-completion candidate providers (Iterable<String>)
│   │   ├── AttributeCandidates.java
│   │   ├── BrowserVersionCandidates.java
│   │   ├── ChromeArgCandidates.java
│   │   ├── CommaSplitCompleter.java       # Custom JLine Completer for comma-separated values
│   │   ├── HeaderCandidates.java
│   │   ├── JsSnippetCandidates.java
│   │   ├── KeyCandidates.java
│   │   ├── LocatorCandidates.java
│   │   ├── NavigationCandidates.java
│   │   ├── ScreenshotPathCandidates.java
│   │   ├── UrlCandidates.java
│   │   ├── WaitSecondsCandidates.java
│   │   └── WindowSizeCandidates.java
│   ├── config/
│   │   └── BrowserConfig.java             # Singleton — Chrome options, persists to .selenium-cli.json
│   ├── model/
│   │   ├── CommandResult.java             # JSON envelope: success()/error() → print()
│   │   ├── CommandRequest.java            # POJO for batch JSON: { "command": "...", "args": [...] }
│   │   └── SessionFile.java              # Wrapper for session JSON: { config: {...}, commands: [...] }
│   ├── session/
│   │   ├── SessionManager.java            # Singleton — owns the live ChromeDriver
│   │   └── NoActiveSessionException.java
│   └── util/
│       ├── JsonOutput.java                # Shared Gson instance (pretty-print, no HTML escaping)
│       ├── LocatorParser.java             # Parses locator string → Selenium By object
│       └── SessionRecorder.java           # Singleton — records REPL commands → session-<ts>.json
└── src/test/java/cli/                     # ══ UNIT TESTS (JUnit 5) ══
    ├── TokenizerTest.java                 # SeleniumCli.tokenize() — REPL input parsing
    ├── commands/
    │   ├── CommandRegistryTest.java       # Validates all commands registered, named, described
    │   └── RunCommandTest.java            # Session file format detection, config round-trip
    ├── completions/
    │   └── CompletionCandidatesTest.java  # All 12 candidate providers: instantiation + content
    ├── config/
    │   └── BrowserConfigTest.java         # Defaults, builders, toMap, reset, save/load, ChromeOptions
    ├── model/
    │   ├── CommandRequestTest.java        # Constructors, setters, step numbering
    │   ├── CommandResultTest.java         # success/error factories, JSON serialisation, print()
    │   └── SessionFileTest.java           # POJO, JSON round-trip, step numbers, legacy format
    ├── session/
    │   ├── SessionManagerTest.java        # Singleton, inactive state, safe shutdown
    │   └── NoActiveSessionExceptionTest.java
    └── util/
        ├── JsonOutputTest.java            # Pretty-print, no HTML escaping, round-trip
        ├── LocatorParserTest.java         # All locator strategies + edge cases
        └── SessionRecorderTest.java       # Record, exclude, enable/disable, save with steps+config
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

### STEP 7 — Write / update unit tests (MANDATORY)

**Every change MUST include corresponding unit tests.** Tests are the safety
net that prevents incremental changes from breaking core functionality.
Run `mvn test` after every change and confirm all tests pass before committing.

#### 7a. Update `CommandRegistryTest.java`

File: `src/test/java/cli/commands/CommandRegistryTest.java`

Add the new command name to the `EXPECTED_COMMANDS` set:

```java
private static final Set<String> EXPECTED_COMMANDS = Set.of(
        "open", "click", /* ... existing ... */
        "cmdname",         // ← ADD the new command name
        "config", "run", "history", "session", "quit"
);
```

This test will FAIL if a command is registered in `SeleniumCli.java` but
missing from the set (or vice versa), catching registration mistakes.

#### 7b. Update `CompletionCandidatesTest.java` (if new candidates class was created)

File: `src/test/java/cli/completions/CompletionCandidatesTest.java`

Add tests verifying the new candidates class:

```java
@Test
@DisplayName("NewCandidates: instantiable and non-empty")
void newCandidates() {
    List<String> list = toList(new NewCandidates());
    assertFalse(list.isEmpty());
}

@Test
@DisplayName("NewCandidates: contains key values")
void newCandidatesContent() {
    List<String> list = toList(new NewCandidates());
    assertTrue(list.contains("expected_value"));
}
```

#### 7c. Add unit tests for any NEW utility classes or logic

If the command introduces new utility methods, model classes, or config
options, write tests in the appropriate test file:

| Changed area | Test file to update |
|---|---|
| New locator strategy | `LocatorParserTest.java` |
| New model/POJO class | Create `src/test/java/cli/model/NewModelTest.java` |
| New config option | `BrowserConfigTest.java` (default, builder, toMap, reset, save/load) |
| Changed tokenizer logic | `TokenizerTest.java` |
| Changed session recording | `SessionRecorderTest.java` |
| Changed JSON format | `SessionFileTest.java` / `CommandResultTest.java` |

#### 7d. Run the full test suite

```bash
mvn test
```

ALL tests must pass. Do NOT skip failing tests. Fix them or update expectations.

---

## UNIT TESTING

### Overview

The project uses **JUnit 5** (Jupiter) for unit tests. Tests are located
under `src/test/java/cli/` mirroring the main source structure. They are
designed to be **fast** (~2 seconds), require **no browser**, and verify
all core logic that could break with incremental changes.

### Test dependencies (in pom.xml)

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.11.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.14.2</version>
    <scope>test</scope>
</dependency>
```

### Running tests

```bash
mvn test                              # Run all tests
mvn test -Dtest=LocatorParserTest     # Run a specific test class
mvn test -Dtest="cli.model.*"         # Run tests matching a pattern
mvn test -Dtest=TokenizerTest#simpleNoArgs  # Run a single test method
mvn package                           # Compile + test + build JAR (recommended)
```

### Current test inventory (150 tests)

| Test class | Count | Scope |
|---|---|---|
| `LocatorParserTest` | 19 | All locator strategies: `#id`, `.class`, `//xpath`, explicit prefixes, edge cases |
| `JsonOutputTest` | 6 | Pretty-print, no HTML escaping, singleton, null handling, round-trip |
| `SessionRecorderTest` | 16 | Enable/disable, record, meta-command exclusion, save with steps + config, reset |
| `CommandResultTest` | 13 | `success()`/`error()` factories, metadata, JSON serialisation, `print()` |
| `CommandRequestTest` | 6 | All constructors, setters/getters, step numbering |
| `SessionFileTest` | 8 | POJO, JSON serialisation with config + steps, deserialisation, legacy format |
| `BrowserConfigTest` | 32 | Defaults, builders, `toMap()`, `reset()`, save/load persistence, `toChromeOptions()` |
| `SessionManagerTest` | 6 | Singleton, inactive state, `getDriverOrThrow()` exception, safe shutdown |
| `NoActiveSessionExceptionTest` | 2 | Message text, inheritance |
| `TokenizerTest` | 19 | Basic tokenization, grouping quotes, embedded quotes, real-world commands |
| `CompletionCandidatesTest` | 14 | All 12 candidate providers: instantiation, non-empty, contain key values |
| `CommandRegistryTest` | 5 | All commands registered, no extras, Runnable interface, descriptions |
| `RunCommandTest` | 5 | Session file format detection, config round-trip, numbered steps |

### Test patterns

**Singletons** (BrowserConfig, SessionRecorder) — Always `reset()` in
`@BeforeEach` and `@AfterEach` to avoid cross-test contamination.

**No browser required** — Tests validate logic (parsing, serialisation,
config, recording) without launching Chrome. Any test that would need a
browser should guard with `if (!sm.isActive())`.

**File I/O tests** — Use `Files.createTempFile()` and clean up in `finally`
blocks. For `BrowserConfig.save()/load()`, always call `deleteConfigFile()`
in teardown.

**`@Nested` + `@DisplayName`** — Group related tests into inner classes for
readable output: `LocatorParser > Explicit prefixes > xpath= prefix → By.xpath`.

### MANDATORY RULE: Tests are not optional

**Every pull request / code change MUST:**
1. Keep all existing tests passing (`mvn test` = green)
2. Add new tests for any new or changed functionality
3. Update `CommandRegistryTest.EXPECTED_COMMANDS` when adding/removing commands
4. Update `CompletionCandidatesTest` when adding new candidate providers
5. Update `BrowserConfigTest` when adding new config options

**If a test fails after your change, you MUST either:**
- Fix the bug your change introduced, OR
- Update the test expectations if the old behaviour was intentionally changed

**Never delete or `@Disabled` a test to make the build pass.**

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

## QUICK REFERENCE: FILES TO MODIFY PER TASK

### Adding a new command:
1. `src/main/java/cli/commands/NewCommand.java` — CREATE
2. `src/main/java/cli/completions/NewCandidates.java` — CREATE (if needed)
3. `src/main/java/cli/SeleniumCli.java` — subcommands array + buildParameterCompleters()
4. `README.md` — TOC + Commands Reference section
5. `src/test/java/cli/commands/CommandRegistryTest.java` — add to EXPECTED_COMMANDS
6. `src/test/java/cli/completions/CompletionCandidatesTest.java` — add tests (if new candidates)
7. Run `mvn test` — ALL tests must pass

### Adding a new config option:
1. `src/main/java/cli/config/BrowserConfig.java` — field, getter, setter, toMap(), reset(), load()
2. `src/main/java/cli/commands/ConfigCommand.java` — @Option + apply logic
3. `src/main/java/cli/SeleniumCli.java` — buildStartupOptionsBlock()
4. `README.md` — config table + Startup Options section
5. `src/test/java/cli/config/BrowserConfigTest.java` — default, builder, toMap, reset, save/load tests
6. Run `mvn test` — ALL tests must pass

### Adding a new reusable completion:
1. `src/main/java/cli/completions/NewCandidates.java` — CREATE
2. Wire via `completionCandidates = NewCandidates.class` on the @Parameters/@Option
3. `src/main/java/cli/SeleniumCli.java` — buildParameterCompleters() (if positional)
4. `src/test/java/cli/completions/CompletionCandidatesTest.java` — add instantiation + content tests
5. Run `mvn test` — ALL tests must pass

### Modifying any utility class:
1. Make the change in `src/main/java/cli/util/Xxx.java`
2. Update the corresponding test in `src/test/java/cli/util/XxxTest.java`
3. Run `mvn test` — ALL tests must pass
