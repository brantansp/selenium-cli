<p align="center">
  <img src="selenium-cli-removebg-preview.svg" alt="Selenium CLI Logo" width="200"/>
</p>

# Selenium CLI

An interactive REPL and one-shot command-line interface that exposes **Selenium WebDriver** as simple shell commands. Every response is structured **JSON**, making it easy to pipe into `jq`, log to files, or integrate with other tooling.

```

         #################################################################        
       ####                                                             ####      
     ###                                                                  ###     
     ###                                                        +++++     ###     
     ###                                                      ++++++      ###     
     ###       ####                                         ++++++        ###     
     ###       #######                             +++     ++++++         ###     
     ###       ##########                         ++++++ ++++++           ###     
     ###       #############                       +++++++++++            ###     
     ###        ###############                      +++++++              ###     
     ###            ##############                    +++++               ###     
     ###              ###############                                     ###     
     ###                 ###############                                  ###     
     ###                    ##############                                ###     
     ###                       ##############                             ###     
     ###                    ################                              ###     
     ###                  ###############                                 ###     
     ###               ##############                                     ###     
     ###            ##############                                        ###     
     ###        ################                 ###################      ###     
     ###       #############                     ###################      ###     
     ###       ###########                                                ###     
     ###       ########                                                   ###     
     ###       #####                                                      ###                                                                       
     ###                                                                  ###
       ###                                                               ###
        ###################################################################
           
     Selenium CLI  v1.0.0                                                                                                                           
     Type 'help' or '--help' for usage
     Type 'exit' to leave
     
selenium> open https://google.com
{
  "status": "success",
  "command": "open",
  "args": ["https://google.com"],
  "result": "https://www.google.com/",
  "sessionId": "abc123...",
  "timestamp": "2026-03-18T10:00:00Z"
}
```

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Building from Source](#building-from-source)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Usage Modes](#usage-modes)
  - [REPL (Interactive) Mode](#repl-interactive-mode)
  - [One-Shot Mode](#one-shot-mode)
  - [Piped / Scripted Mode](#piped--scripted-mode)
- [Startup Options Display](#startup-options-display)
- [Session Recording](#session-recording)
- [Tab-Completion & Autocomplete](#tab-completion--autocomplete)
  - [Command-Level Completion](#command-level-completion)
  - [Value-Level Completion (Smart Suggestions)](#value-level-completion-smart-suggestions)
  - [Completion Reference by Command](#completion-reference-by-command)
- [ANSI Color Support](#ansi-color-support)
- [Commands Reference](#commands-reference)
  - [open](#open)
  - [click](#click)
  - [type](#type)
  - [gettext](#gettext)
  - [getattr](#getattr)
  - [screenshot](#screenshot)
  - [navigate](#navigate)
  - [wait](#wait)
  - [execute](#execute)
  - [config](#config)
  - [session](#session)
  - [run](#run)
  - [quit](#quit)
  - [exit (REPL only)](#exit-repl-only)
- [Locator Syntax](#locator-syntax)
- [JSON Output Format](#json-output-format)
- [Batch Execution (JSON Scripts)](#batch-execution-json-scripts)
- [Configuration Deep Dive](#configuration-deep-dive)
- [Examples](#examples)
  - [Login Flow](#login-flow)
  - [Scrape Text from a Page](#scrape-text-from-a-page)
  - [Headless Screenshot Pipeline](#headless-screenshot-pipeline)
  - [Batch File with Error Handling](#batch-file-with-error-handling)
- [Architecture Overview](#architecture-overview)
  - [Project Structure](#project-structure)
  - [Key Classes](#key-classes)
  - [How It Works](#how-it-works)
- [Contributing](#contributing)
  - [Adding a New Command](#adding-a-new-command)
  - [Adding Completion Candidates](#adding-completion-candidates)
  - [Code Style & Conventions](#code-style--conventions)
  - [Building & Testing Locally](#building--testing-locally)
- [Tech Stack](#tech-stack)
- [Troubleshooting](#troubleshooting)
- [License](#license)

---

## Features

- **Interactive REPL** — explore and automate browsers conversationally
- **Startup options display** — on launch, every option is printed as ENABLED or disabled so you always know the active configuration
- **Session recording (on by default)** — every REPL session is automatically recorded to a replayable JSON file; disable with `--no-record`
- **Tab-completion & autocomplete** — press `Tab` to see command names, options, and smart value suggestions powered by JLine3 + Picocli
- **ANSI color output** — colored banner, prompt, and help text for a modern terminal experience
- **One-shot mode** — run a single command from your shell and exit
- **Batch execution** — run a sequence of commands from a JSON file (`run --json`)
- **Structured JSON output** — every command returns a consistent JSON envelope
- **Smart locators** — auto-detects XPath, CSS, `#id`, `.class` shorthand
- **Zero driver management** — Selenium Manager (bundled) automatically downloads ChromeDriver and Chrome for Testing
- **Rich configuration** — headless, incognito, proxy, custom headers (CDP), window size, and more
- **Single fat JAR** — no external dependencies to install at runtime

---

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| **Java JDK** | 17+ | `java -version` to check |
| **Apache Maven** | 3.8+ | Only needed to build from source |
| **Google Chrome** | Any recent | Selenium Manager auto-downloads Chrome for Testing if needed |

> **Note:** You do **not** need to download ChromeDriver manually. Selenium 4.28+ includes Selenium Manager, which resolves the correct ChromeDriver and Chrome for Testing (CfT) binaries automatically.

---

## Building from Source

```bash
# Clone the repository
git clone <repo-url> selenium-cli
cd selenium-cli

# Build the fat JAR (includes all dependencies)
mvn clean package

# The JAR is produced at:
#   target/selenium-cli-1.0.0.jar
```

The Maven Shade plugin produces a single executable JAR with all dependencies bundled. The main class is `cli.SeleniumCli`.

---

## Installation

### Option 1: Use the wrapper scripts

Wrapper scripts are provided for convenience. Add the project root to your `PATH`:

**Windows (cmd / PowerShell):**
```batch
set PATH=%PATH%;C:\TestAutomation\selenium-cli
selenium open https://google.com
```

**Linux / macOS / Git Bash:**
```bash
export PATH="$PATH:/path/to/selenium-cli"
selenium open https://google.com
```

### Option 2: Run the JAR directly

```bash
java -jar target/selenium-cli-1.0.0.jar [command] [args...]
```

### Option 3: Create an alias

```bash
# Bash / Zsh
alias selenium='java -jar /path/to/selenium-cli/target/selenium-cli-1.0.0.jar'

# PowerShell
Set-Alias selenium 'java -jar C:\TestAutomation\selenium-cli\target\selenium-cli-1.0.0.jar'
```

---

## Quick Start

```bash
# Start the interactive REPL
selenium

# Or pre-configure and enter the REPL in one go
selenium config --headless --window-size 1920x1080

# Or run a single command (one-shot mode — exits after execution)
selenium open https://google.com
selenium screenshot home.png
selenium quit
```

Inside the REPL:

```
selenium> open https://google.com
selenium> type "input[name='q']" "Selenium CLI"
selenium> click "input[value='Google Search']"
selenium> screenshot search-results.png
selenium> quit
selenium> exit
```

---

## Usage Modes

### REPL (Interactive) Mode

Launch without arguments to enter the interactive Read-Eval-Print Loop:

```bash
selenium
```

Or **pre-configure and enter the REPL** in a single command:

```bash
selenium config --headless --window-size 1920x1080
```

This applies the config, prints confirmation, and drops straight into the REPL with those settings active — ready for `open`, `click`, etc.

```bash
# Combine startup flags with config
selenium --no-record config --headless --incognito
```

- Type commands at the `selenium>` prompt
- Type `help` to see all available commands
- Type `exit` to close the session and leave the REPL
- Press `Ctrl+C` — the shutdown hook automatically closes the browser

### One-Shot Mode

Pass a command and its arguments directly:

```bash
selenium open https://example.com
selenium screenshot page.png
selenium quit
```

Each invocation starts a fresh JVM. The process exits with code `0` on success, `1` on error.

### Piped / Scripted Mode

Commands can be piped from stdin. The REPL reads line-by-line until EOF:

```bash
echo open https://google.com | selenium
```

```bash
# Multi-command script
(
echo open https://example.com
echo screenshot example.png
echo quit
) | selenium
```

---

## Startup Options Display

When the REPL starts, a status block is printed immediately after the banner showing the current state of **every option** — so you always know exactly what configuration is active:

```
     Selenium CLI  v1.0.0
     Type 'help' or '--help' for usage
     Type 'exit' to leave

     ── Startup Options ──────────────────────────
     Session Recording    : ENABLED
     Headless             : disabled
     Maximize             : disabled
     Incognito            : disabled
     Window Size          : off
     User Data Dir        : off
     Proxy                : off
     Browser Version      : off
     Extra Headers        : off
     Chrome Arguments     : off
     ─────────────────────────────────────────────
     Use 'config --<option> true/false' to change at runtime
     Use 'config --show' to view current settings
```

- **ENABLED** (green) — the option is active
- **disabled / off** (dim) — the option is not set

If you pre-configure options before starting the REPL, they will show up here:

```bash
# Pre-configure headless + window size, then start the REPL
selenium config --headless --window-size 1920x1080
selenium
```

```
     Session Recording    : ENABLED
     Headless             : ENABLED
     Maximize             : disabled
     Incognito            : disabled
     Window Size          : 1920x1080
     ...
```

> **How it works:** The `config` command persists all settings to a `.selenium-cli.json` file
> in the current working directory. Every subsequent `selenium` invocation (REPL or one-shot)
> loads this file on startup, so config set in one command carries to the next.
> The file is automatically deleted when you run `quit`.

This gives you at-a-glance confidence about the session you're about to use.

### Changing Options at Runtime

All options shown in the startup block can be **toggled on or off** during a live session using the `config` command. Boolean options accept `true` / `false`:

| Option | Enable | Disable |
|---|---|---|
| Session Recording | `config --record` or `config --record true` | `config --record false` |
| Headless | `config --headless` or `config --headless true` | `config --headless false` |
| Maximize | `config --maximize` or `config --maximize true` | `config --maximize false` |
| Incognito | `config --incognito` or `config --incognito true` | `config --incognito false` |
| Window Size | `config --window-size 1920x1080` | *(set before next session)* |
| Proxy | `config --proxy http://host:port` | *(set before next session)* |
| Browser Version | `config --browser-version 124` | *(set before next session)* |

Use `config --show` at any time to see the current state of all options:

```
selenium> config --show
```

> **Tip:** Use `config --help` to see all available options and their descriptions.

---

## Session Recording

By default, every REPL session **automatically records** all executed commands to a JSON file. The file uses the **same format** as the `run --json` input, so you can replay any session.

### How It Works

1. When you start the REPL, a `SessionRecorder` begins capturing every replayable command
2. Meta-commands (`help`, `exit`, `session`, `run`) are excluded — only browser actions are recorded
3. When the session ends (`exit`, `Ctrl+D`, or `Ctrl+C`), the recording is saved to `session-<timestamp>.json` in the current directory

### Example

```
selenium> open https://google.com
selenium> type "input[name='q']" "Selenium CLI"
selenium> screenshot search.png
selenium> quit
selenium> exit
{"session_recorded": "C:\\TestAutomation\\selenium-cli\\session-2026-03-31_14-30-00.json"}
```

The saved file looks like this:

```json
[
  {
    "command": "open",
    "args": ["https://google.com"]
  },
  {
    "command": "type",
    "args": ["input[name\u0027q\u0027]", "Selenium CLI"]
  },
  {
    "command": "screenshot",
    "args": ["search.png"]
  },
  {
    "command": "quit",
    "args": []
  }
]
```

### Replaying a Recorded Session

```bash
selenium run --json session-2026-03-31_14-30-00.json
```

### Disabling Session Recording

Recording is **on by default**. There are two ways to control it:

#### At startup — `--no-record` flag

Start the REPL with recording disabled from the very beginning:

```bash
selenium --no-record
```

The startup options display will reflect this:

```
     Session Recording    : disabled
```

#### At runtime — `config --record`

Toggle recording on or off at any time during a live session:

```
selenium> config --record false      ← disable recording
selenium> config --record true       ← re-enable recording
selenium> config --record            ← shorthand for --record true
```

Use `config --show` to verify the current state:

```
selenium> config --show
{
  "status": "success",
  "command": "config",
  "args": ["--show"],
  "result": {
    "sessionRecording": true,
    "headless": false,
    ...
  }
}
```

> **Tip:** You can combine `--record` with other config flags:
> ```
> selenium> config --record false --headless --window-size 1920x1080
> ```

---

## Tab-Completion & Autocomplete

Selenium CLI provides rich **tab-completion** in REPL mode, powered by [JLine3](https://github.com/jline/jline3) and [picocli-shell-jline3](https://picocli.info/#_jline3_completion). Press `Tab` at any point to see context-aware suggestions.

### Command-Level Completion

Press `Tab` at an empty prompt to see all available commands with descriptions:

```
selenium> <Tab>
click        Click an element by locator
config       Configure browser options
execute      Execute JavaScript in the browser
getattr      Get an attribute value of an element
gettext      Get the visible text of an element
navigate     Navigate back, forward, or refresh
open         Navigate to a URL (starts a session if none is active)
quit         Quit the active browser session
run          Execute a batch of commands from a JSON file
screenshot   Take a screenshot of the current page
session      Show current session information
type         Type text into an element
wait         Pause execution for N seconds
```

Start typing and press `Tab` to narrow suggestions:

```
selenium> sc<Tab>
screenshot   Take a screenshot of the current page
```

### Value-Level Completion (Smart Suggestions)

After typing a command, pressing `Tab` shows **smart suggestions** for each parameter and option — not just generic placeholders, but real usable values:

```
selenium> open <Tab>
https://              http://               https://www.google.com
http://localhost:3000  http://localhost:8080  file:///
...

selenium> open https://google.com --options <Tab>
--headless            --start-maximized     --disable-gpu
--no-sandbox          --incognito           --disable-extensions
--window-size=1920,1080  --ignore-certificate-errors  ...

selenium> navigate <Tab>
back    forward    refresh

selenium> config --window-size <Tab>
1920x1080  1366x768  1440x900  1280x720  375x812  768x1024  ...

selenium> getattr #logo <Tab>
id    class    href    src    value    title    placeholder
aria-label    data-testid    innerHTML    textContent    ...
```

### TailTip Parameter Hints

As you type, **inline parameter hints** appear to the right of the cursor showing what argument comes next:

```
selenium> type ▌
              <locator> <text> [--clear]
```

This is powered by JLine3's `TailTipWidgets`.

### Completion Reference by Command

| Command | Parameter / Option | Suggested Values |
|---|---|---|
| `open` | `<url>` | `https://`, `http://`, `localhost` URLs, `file:///` |
| `open` | `--options` | 45+ Chrome flags: `--headless`, `--start-maximized`, `--disable-gpu`, `--no-sandbox`, etc. |
| `click` | `<locator>` | Locator strategies: `xpath=//`, `css=`, `#elementId`, `.className`, XPath templates |
| `type` | `<locator>` | Same locator candidates |
| `gettext` | `<locator>` | Same locator candidates |
| `getattr` | `<locator>` | Same locator candidates |
| `getattr` | `<attribute>` | 35+ HTML attributes: `id`, `class`, `href`, `src`, `value`, `data-testid`, `innerHTML`, etc. |
| `screenshot` | `<filepath>` | `screenshot.png`, `screenshots/page.png`, `output/screenshot.png`, etc. |
| `navigate` | `<direction>` | `back`, `forward`, `refresh` |
| `wait` | `<seconds>` | `1`, `2`, `3`, `5`, `10`, `15`, `20`, `30`, `60` |
| `execute` | `<script>` | 30+ JS snippets: `return document.title`, `window.scrollTo(...)`, `return navigator.userAgent`, etc. |
| `config` | `--options` | Same Chrome flags as `open --options` |
| `config` | `--window-size` | 16 common sizes: `1920x1080`, `1366x768`, `375x812` (mobile), etc. |
| `config` | `--browser-version` | `stable`, `beta`, `dev`, `canary`, `130`–`134` |
| `config` | `--header` | Common headers: `Authorization:Bearer token`, `Content-Type:application/json`, etc. |

---

## ANSI Color Support

Selenium CLI renders colored output for a modern terminal experience. Color is enabled by default via `TerminalBuilder.color(true)` and Picocli's `Ansi.ON`.

### What's Colored

| Element | Color | Description |
|---|---|---|
| **Banner** | Cyan box, green title, yellow hints | Displayed at REPL startup |
| **Prompt** | Bold cyan `selenium> ` | The input prompt |
| **Help text** | Picocli default color scheme | Command names, options, and descriptions in `help` output |
| **Tab-completion menu** | JLine3 default styling | Completion candidates and descriptions |

### Terminal Compatibility

| Terminal | Color Support |
|---|---|
| **Windows Terminal** | ✅ Full ANSI |
| **PowerShell 7+** | ✅ Full ANSI |
| **cmd.exe (Win 10+)** | ✅ ANSI via JLine3 |
| **Git Bash / MSYS2** | ✅ Full ANSI |
| **macOS Terminal** | ✅ Full ANSI |
| **Linux terminals** | ✅ Full ANSI |
| **IntelliJ IDEA terminal** | ✅ Full ANSI |
| **VS Code terminal** | ✅ Full ANSI |

> **Note:** If colors appear as garbled escape codes (e.g., `[36m`), your terminal does not support ANSI. This is rare on modern systems. JLine3 detects most terminals automatically.

---

## Commands Reference

### `open`

Navigate to a URL. **Automatically starts a Chrome session** if none is active.

```
open <url> [--options <chrome-args>]
```

| Parameter | Required | Description |
|---|---|---|
| `url` | Yes | The URL to navigate to |
| `--options` | No | Comma-separated Chrome arguments (only applied on new session) |

**Examples:**

```bash
selenium> open https://google.com
selenium> open https://example.com --options --start-maximized,--disable-gpu
selenium> open file:///C:/tests/page.html
```

---

### `click`

Click an element identified by a [locator](#locator-syntax).

```
click <locator>
```

**Examples:**

```bash
selenium> click #submit-btn
selenium> click .btn-primary
selenium> click //button[@id='login']
selenium> click "input[type='submit']"
```

---

### `type`

Type text into an input element.

```
type <locator> <text> [--clear]
```

| Parameter | Required | Description |
|---|---|---|
| `locator` | Yes | Element locator |
| `text` | Yes | Text to type (quote if it contains spaces) |
| `--clear` | No | Clear the field before typing |

**Examples:**

```bash
selenium> type #email user@test.com
selenium> type #email "new-user@test.com" --clear
selenium> type "input[name='password']" "my secret"
selenium> type //input[@id='search'] "hello world"
```

---

### `gettext`

Get the visible text content of an element.

```
gettext <locator>
```

**Examples:**

```bash
selenium> gettext h1
selenium> gettext .page-title
selenium> gettext //div[@class='message']
```

**Response:**

```json
{
  "status": "success",
  "command": "gettext",
  "args": [".page-title"],
  "result": "Welcome to My Site",
  "sessionId": "abc123",
  "timestamp": "2026-03-18T10:00:00Z"
}
```

---

### `getattr`

Get the value of an HTML attribute on an element.

```
getattr <locator> <attribute>
```

**Examples:**

```bash
selenium> getattr #logo src
selenium> getattr "//input[@name='q']" value
selenium> getattr .main-link href
selenium> getattr #email placeholder
```

---

### `screenshot`

Capture a screenshot of the current page and save it to a file.

```
screenshot [filepath]
```

| Parameter | Required | Default | Description |
|---|---|---|---|
| `filepath` | No | `screenshot.png` | Destination file path |

**Examples:**

```bash
selenium> screenshot
selenium> screenshot home-page.png
selenium> screenshot ./captures/checkout-step-3.png
```

Parent directories are created automatically if they don't exist.

---

### `navigate`

Browser navigation controls.

```
navigate <direction>
```

| Direction | Description |
|---|---|
| `back` | Go to the previous page (browser back) |
| `forward` | Go to the next page (browser forward) |
| `refresh` | Reload the current page |

**Examples:**

```bash
selenium> navigate back
selenium> navigate forward
selenium> navigate refresh
```

---

### `wait`

Pause execution for a specified number of seconds.

```
wait <seconds>
```

**Examples:**

```bash
selenium> wait 3
selenium> wait 10
```

---

### `execute`

Execute arbitrary JavaScript in the current page context.

```
execute <script>
```

**Examples:**

```bash
selenium> execute "return document.title"
selenium> execute "document.querySelector('h1').style.color='red'"
selenium> execute "return window.location.href"
selenium> execute "window.scrollTo(0, document.body.scrollHeight)"
```

If the script returns a value (via `return`), it is included in the JSON `result` field.

---

### `config`

View or modify browser configuration. Some settings apply immediately to a live session; others take effect on the **next** session.

```
config [options]
```

| Option | Description | Live-apply? |
|---|---|---|
| `--record [true\|false]` | Enable/disable session recording | ✅ Yes |
| `--headless [true\|false]` | Enable/disable headless mode | ❌ Next session |
| `--maximize [true\|false]` | Enable/disable maximize window | ✅ Yes |
| `--incognito [true\|false]` | Enable/disable incognito mode | ❌ Next session |
| `--window-size <WxH>` | Set window size (e.g. `1920x1080`) | ❌ Next session |
| `--user-data-dir <path>` | Chrome user data directory | ❌ Next session |
| `--proxy <url>` | HTTP/S proxy (e.g. `http://host:port`) | ❌ Next session |
| `--browser-version <ver>` | Chrome version for Selenium Manager | ❌ Next session |
| `--header <Name:Value>` | Add custom HTTP header (repeatable) | ✅ Yes (via CDP) |
| `--options <args>` | Raw Chrome arguments, comma-separated | ❌ Next session |
| `--show` | Print current configuration (incl. recording) | — |

**Examples:**

```bash
# View current config (includes sessionRecording status)
selenium> config --show

# Toggle session recording on/off during a live session
selenium> config --record false
selenium> config --record true

# Set up headless with custom size before opening
selenium> config --headless --window-size 1920x1080
selenium> open https://example.com

# Add custom headers to a live session
selenium> config --header "Authorization:Bearer token123"
selenium> config --header "X-Custom:myvalue"

# Maximize the current window
selenium> config --maximize

# Disable options that were previously enabled
selenium> config --headless false
selenium> config --maximize false
selenium> config --incognito false

# Multiple options for next session
selenium> config --incognito --proxy http://127.0.0.1:8080
```

---

### `session`

Display metadata about the active browser session.

```
session
```

**Example output:**

```json
{
  "status": "success",
  "command": "session",
  "args": [],
  "result": {
    "active": true,
    "sessionId": "d5f2a1...",
    "startedAt": "2026-03-18T10:00:00Z",
    "currentUrl": "https://google.com/",
    "title": "Google",
    "browserName": "chrome",
    "browserVersion": "124.0.6367.91",
    "config": {
      "headless": false,
      "maximize": true,
      "incognito": false,
      ...
    }
  }
}
```

---

### `run`

Execute a batch of commands from a JSON file.

```
run --json <file> [--output <file>] [--continue-on-error]
```

| Option | Required | Description |
|---|---|---|
| `--json <file>` | Yes | Path to JSON command file |
| `--output <file>` | No | Write results to file instead of stdout |
| `--continue-on-error` | No | Don't stop on first error |

**Examples:**

```bash
selenium> run --json test.json
selenium> run --json test.json --output results.json
selenium> run --json test.json --continue-on-error
```

See [Batch Execution](#batch-execution-json-scripts) for the JSON file format.

---

### `quit`

Close the active browser session. Does **not** exit the REPL — you can start a new session with `open`.

```
quit
```

---

### `exit` (REPL only)

Close the browser session (if active) and exit the REPL.

```
exit
```

---

## Locator Syntax

The CLI supports flexible element locators with auto-detection:

| Syntax | Strategy | Example |
|---|---|---|
| `#id` | `By.id` | `#submit-btn` |
| `.class` | `By.cssSelector` | `.btn-primary` |
| `//xpath` | `By.xpath` | `//button[@type='submit']` |
| `(//xpath)` | `By.xpath` | `(//div[@class='item'])[1]` |
| `css=selector` | `By.cssSelector` | `css=div.content > p` |
| `xpath=expr` | `By.xpath` | `xpath=//h1` |
| `id=value` | `By.id` | `id=email` |
| `name=value` | `By.name` | `name=username` |
| `tag=value` | `By.tagName` | `tag=h1` |
| *(anything else)* | `By.cssSelector` | `input[type='email']` |

### Locator Tips

- **Prefer `#id`** when the element has a unique ID — it's the fastest and most readable.
- **Use quotes** around complex CSS selectors: `"div.main > ul li:first-child"`
- **XPath** is auto-detected when the locator starts with `//` or `(//`.
- **Explicit prefixes** (`css=`, `xpath=`, `id=`, `name=`, `tag=`) override auto-detection.

---

## JSON Output Format

Every command produces a consistent JSON envelope:

### Success

```json
{
  "status": "success",
  "command": "open",
  "args": ["https://google.com"],
  "result": "https://www.google.com/",
  "sessionId": "d5f2a1b0c...",
  "timestamp": "2026-03-18T10:05:30.123Z",
  "error": null
}
```

### Error

```json
{
  "status": "error",
  "command": "click",
  "args": ["#nonexistent"],
  "result": null,
  "sessionId": "d5f2a1b0c...",
  "timestamp": "2026-03-18T10:05:32.456Z",
  "error": "no such element: Unable to locate element: {\"method\":\"css selector\",\"selector\":\"#nonexistent\"}"
}
```

### Fields

| Field | Type | Description |
|---|---|---|
| `status` | `string` | `"success"` or `"error"` |
| `command` | `string` | The command that was executed |
| `args` | `string[]` | Arguments passed to the command |
| `result` | `any` | Command-specific result (string, object, or `null`) |
| `sessionId` | `string?` | Active WebDriver session ID, or `null` |
| `timestamp` | `string` | ISO-8601 timestamp |
| `error` | `string?` | Error message, or `null` on success |

---

## Batch Execution (JSON Scripts)

The `run` command accepts a JSON array of command objects:

### JSON File Format

```json
[
  { "command": "<command-name>", "args": ["arg1", "arg2", ...] },
  { "command": "<command-name>", "args": [] }
]
```

Each object has:
- `command` — the subcommand name (e.g. `"open"`, `"click"`, `"type"`)
- `args` — an array of string arguments, in the same order as the CLI positional parameters

### Example: `examples/test.json`

```json
[
  { "command": "open",       "args": ["https://www.google.com"] },
  { "command": "screenshot", "args": ["google-home.png"] },
  { "command": "gettext",    "args": ["body"] },
  { "command": "quit",       "args": [] }
]
```

### Running a batch

```bash
# Run and print results to stdout
selenium run --json examples/test.json

# Save results to a file
selenium run --json examples/test.json --output results.json

# Continue past failures
selenium run --json examples/test.json --continue-on-error
```

### Batch Output

The output is a JSON array of individual command results:

```json
[
  {
    "status": "success",
    "command": "open",
    "args": ["https://www.google.com"],
    "result": "https://www.google.com/",
    ...
  },
  {
    "status": "success",
    "command": "screenshot",
    "args": ["google-home.png"],
    "result": "C:\\TestAutomation\\selenium-cli\\google-home.png",
    ...
  }
]
```

By default, execution **stops on the first error**. Use `--continue-on-error` to execute all commands regardless.

---

## Configuration Deep Dive

### Config Lifecycle

1. **Before a session** — Use `config` to set options. These are stored in the singleton `BrowserConfig` **and persisted to `.selenium-cli.json`** in the current working directory.
2. **Across JVM invocations** — On startup, `.selenium-cli.json` is loaded automatically. This means one-shot `config` commands carry over to subsequent invocations (e.g. `selenium config --headless` followed by `selenium open https://...`).
3. **Session starts** — When `open` is called (or a session is started), `BrowserConfig.toChromeOptions()` builds the `ChromeOptions`.
4. **During a session** — Only `--maximize` and `--header` can be applied to a live session. All other options show a warning and take effect on the **next** session.
5. **On quit** — `BrowserConfig.reset()` clears all configuration back to defaults **and deletes `.selenium-cli.json`**.

### Config File (`.selenium-cli.json`)

The config file is created automatically when you run `config` and deleted on `quit`:

```bash
# Creates .selenium-cli.json with headless=true, windowSize=1920x1080
selenium config --headless --window-size 1920x1080

# This invocation loads .selenium-cli.json → opens headless with the saved size
selenium open https://example.com

# Cleans up the session and deletes .selenium-cli.json
selenium quit
```

You can also inspect the file directly:

```bash
type .selenium-cli.json       # Windows
cat .selenium-cli.json        # Linux/macOS
```

### Default Chrome Arguments

These are always applied (see `BrowserConfig.toChromeOptions()`):

```
--no-sandbox
--disable-dev-shm-usage
--disable-notifications
--disable-popup-blocking
--remote-allow-origins=*
```

### Headless Mode

```bash
selenium> config --headless --window-size 1920x1080
selenium> open https://example.com
selenium> screenshot headless-capture.png
selenium> quit
```

Uses `--headless=new` (Chrome's modern headless mode).

### Proxy Configuration

```bash
selenium> config --proxy http://127.0.0.1:8080
selenium> open https://example.com
```

Sets both HTTP and SSL proxy.

### Custom HTTP Headers (CDP)

Headers are injected via Chrome DevTools Protocol and apply to **all requests**:

```bash
selenium> config --header "Authorization:Bearer eyJhbGciOi..."
selenium> config --header "X-Request-Id:test-123"
```

### Specific Chrome Version

Selenium Manager resolves the matching ChromeDriver and Chrome for Testing binary:

```bash
selenium> config --browser-version 124
selenium> open https://example.com
```

---

## Examples

### Login Flow

```bash
selenium> open https://myapp.com/login
selenium> type #username admin
selenium> type #password "s3cret!" --clear
selenium> click #login-btn
selenium> wait 2
selenium> gettext .welcome-message
selenium> screenshot after-login.png
```

### Scrape Text from a Page

```bash
selenium> open https://news.ycombinator.com
selenium> gettext .titleline
selenium> getattr .titleline a href
selenium> execute "return document.querySelectorAll('.titleline').length"
```

### Headless Screenshot Pipeline

```bash
# One-shot from the shell
selenium config --headless --window-size 1920x1080
selenium open https://example.com
selenium screenshot example.png
selenium quit
```

Or as a batch file (`headless-capture.json`):

```json
[
  { "command": "config", "args": ["--headless", "--window-size", "1920x1080"] },
  { "command": "open",   "args": ["https://example.com"] },
  { "command": "screenshot", "args": ["example-headless.png"] },
  { "command": "quit",   "args": [] }
]
```

```bash
selenium run --json headless-capture.json --output report.json
```

### Batch File with Error Handling

```json
[
  { "command": "open",  "args": ["https://httpbin.org/html"] },
  { "command": "gettext", "args": ["h1"] },
  { "command": "click", "args": ["#nonexistent"] },
  { "command": "screenshot", "args": ["after-error.png"] }
]
```

```bash
# Stops at the click error:
selenium run --json test-errors.json

# Continues past the error, takes screenshot anyway:
selenium run --json test-errors.json --continue-on-error
```

### Pipe with jq

```bash
selenium open https://example.com 2>&1 | jq '.result'
```

---

## Architecture Overview

### Project Structure

```
selenium-cli/
├── pom.xml                          # Maven build (Shade plugin → fat JAR)
├── selenium                         # Unix/macOS/Git Bash wrapper script
├── selenium.bat                     # Windows wrapper script
├── examples/
│   └── test.json                    # Sample batch file
└── src/main/java/cli/
    ├── SeleniumCli.java             # Entry point, REPL loop, Picocli root command
    ├── commands/                    # One class per command (Picocli subcommands)
    │   ├── OpenCommand.java         #   open <url>
    │   ├── ClickCommand.java        #   click <locator>
    │   ├── TypeCommand.java         #   type <locator> <text>
    │   ├── GetTextCommand.java      #   gettext <locator>
    │   ├── GetAttrCommand.java      #   getattr <locator> <attribute>
    │   ├── ScreenshotCommand.java   #   screenshot [filepath]
    │   ├── NavigateCommand.java     #   navigate back|forward|refresh
    │   ├── WaitCommand.java         #   wait <seconds>
    │   ├── ExecuteJsCommand.java    #   execute <script>
    │   ├── ConfigCommand.java       #   config [options]
    │   ├── RunCommand.java          #   run --json <file>
    │   ├── SessionCommand.java      #   session
    │   └── QuitCommand.java         #   quit
    ├── completions/                 # Tab-completion candidate providers
    │   ├── AttributeCandidates.java #   HTML attributes (id, class, href, src…)
    │   ├── BrowserVersionCandidates.java  # stable, beta, dev, 130–134
    │   ├── ChromeArgCandidates.java #   45+ Chrome CLI flags
    │   ├── HeaderCandidates.java    #   Common HTTP headers
    │   ├── JsSnippetCandidates.java #   30+ JavaScript snippets
    │   ├── LocatorCandidates.java   #   Locator strategies & templates
    │   ├── NavigationCandidates.java#   back, forward, refresh
    │   ├── ScreenshotPathCandidates.java  # Common screenshot paths
    │   ├── UrlCandidates.java       #   Common URL prefixes
    │   ├── WaitSecondsCandidates.java #  Common wait durations
    │   └── WindowSizeCandidates.java#   16 common viewport sizes
    ├── config/
    │   └── BrowserConfig.java       # Singleton: accumulates Chrome options
    ├── model/
    │   ├── CommandResult.java       # JSON envelope (success/error)
    │   └── CommandRequest.java      # POJO for batch JSON entries
    ├── session/
    │   ├── SessionManager.java      # Singleton: owns the ChromeDriver lifecycle
    │   └── NoActiveSessionException.java
    └── util/
        ├── JsonOutput.java          # Centralized Gson instance
        ├── LocatorParser.java       # Auto-detect locator strategy
        └── SessionRecorder.java     # Records REPL commands for replay
```

### Key Classes

| Class | Role |
|---|---|
| `SeleniumCli` | Picocli `@Command` root. Hosts the REPL loop, JLine3 terminal setup, tokenizer, and error handlers. Configures `PicocliCommands`, `SystemCompleter`, and `TailTipWidgets` for autocomplete. |
| `SessionManager` | Singleton owning the single live `ChromeDriver`. Handles `start()`, `shutdown()`, `getDriverOrThrow()`. |
| `BrowserConfig` | Singleton accumulating Chrome configuration. Converts to `ChromeOptions` via `toChromeOptions()`. Resets on quit. |
| `CommandResult` | Immutable JSON envelope. Factory methods `success()` / `error()`. Calls `JsonOutput.toJson()` on `print()`. |
| `CommandRequest` | POJO deserialized from batch JSON files (`{ "command": "...", "args": [...] }`). |
| `LocatorParser` | Stateless utility: parses a raw locator string into a Selenium `By` object. |
| `JsonOutput` | Provides a shared `Gson` instance with pretty-printing and HTML escaping disabled. |
| `SessionRecorder` | Records all REPL commands during a session and writes them to a replayable JSON file on exit. |

#### Completion Candidate Classes (`cli.completions`)

Each class implements `Iterable<String>` and provides a static list of tab-completion suggestions. Picocli's `completionCandidates` attribute on `@Parameters` / `@Option` connects them to the JLine completer.

| Class | Provides | Used By |
|---|---|---|
| `ChromeArgCandidates` | 45+ Chrome CLI flags (`--headless`, `--disable-gpu`, …) | `open --options`, `config --options` |
| `UrlCandidates` | Common URL prefixes (`https://`, `http://localhost:8080`, …) | `open <url>` |
| `LocatorCandidates` | Locator strategies (`xpath=//`, `css=`, `#elementId`, `.className`, XPath templates) | `click`, `type`, `gettext`, `getattr` |
| `AttributeCandidates` | 35+ HTML attributes (`id`, `href`, `src`, `data-testid`, `innerHTML`, …) | `getattr <attribute>` |
| `NavigationCandidates` | `back`, `forward`, `refresh` | `navigate <direction>` |
| `WaitSecondsCandidates` | Common durations (`1`, `2`, `3`, `5`, `10`, `15`, `30`, `60`) | `wait <seconds>` |
| `JsSnippetCandidates` | 30+ JS snippets (`return document.title`, `window.scrollTo(…)`, …) | `execute <script>` |
| `ScreenshotPathCandidates` | Common file paths (`screenshot.png`, `screenshots/page.png`, …) | `screenshot <filepath>` |
| `WindowSizeCandidates` | 16 viewport sizes (`1920x1080`, `1366x768`, `375x812`, …) | `config --window-size` |
| `BrowserVersionCandidates` | Channel names + versions (`stable`, `beta`, `dev`, `130`–`134`) | `config --browser-version` |
| `HeaderCandidates` | Common HTTP headers (`Authorization:Bearer token`, `Content-Type:…`, …) | `config --header` |
| `CommaSplitCompleter` | JLine `Completer` for comma-separated values inside `--options=a,b,<Tab>` | `open --options`, `config --options` |

### How It Works

The REPL's tab-completion system is assembled from three layers:

```
┌──────────────────────────────────────────────────────┐
│                  AggregateCompleter                   │
│  (merges results from all child completers)          │
├──────────────┬──────────────────┬────────────────────┤
│ SystemCompleter │ ArgumentCompleters │ CommaSplitCompleter │
│ (picocli-jline3) │ (positional params) │ (--options=a,b,<Tab>) │
└──────────────┴──────────────────┴────────────────────┘
```

1. **`SystemCompleter`** — generated by `PicocliCommands.compileCompleters()`. Handles command-name completion and `--option` name/value completion automatically from picocli's `@Command` model.

2. **`ArgumentCompleter`s** — manually registered in `SeleniumCli.buildParameterCompleters()`. Each command that has `@Parameters` with `completionCandidates` gets an explicit `ArgumentCompleter(commandName, param1Candidates, param2Candidates, NullCompleter)`. This is necessary because **picocli-shell-jline3 does not propagate `completionCandidates` from `@Parameters` into JLine**.

3. **`CommaSplitCompleter`** — a custom JLine `Completer` that detects when the user is typing comma-separated values (e.g., `--options=--headless,<Tab>`) and offers per-item suggestions after the last comma. Standard completers treat the entire `--options=value1,value2` as one opaque token.

---

## Contributing

### Adding a New Command

Follow these steps to add a new subcommand (e.g., `select` for dropdowns):

#### 1. Create the command class

Create a new file at `src/main/java/cli/commands/SelectCommand.java`:

```java
package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

@Command(name = "select", description = "Select an option from a dropdown")
public class SelectCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator for the <select>")
    private String locator;

    @Parameters(index = "1", description = "Visible text of the option")
    private String text;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            new Select(element).selectByVisibleText(text);
            CommandResult.success("select", List.of(locator, text), null).print();
        } catch (Exception e) {
            CommandResult.error("select", List.of(locator, text), e.getMessage()).print();
        }
    }
}
```

#### 2. Register it in `SeleniumCli.java`

Add `SelectCommand.class` to the `subcommands` array in the `@Command` annotation:

```java
@Command(
    name = "selenium",
    ...
    subcommands = {
        OpenCommand.class,
        ClickCommand.class,
        // ... existing commands ...
        SelectCommand.class    // ← add here
    }
)
```

#### 3. Build and test

```bash
mvn clean package
selenium select "#country" "United States"
```

### Command Implementation Checklist

- [ ] Class in `cli.commands` package
- [ ] Annotated with `@Command(name = "...", description = "...")`
- [ ] Implements `Runnable`
- [ ] Uses `@Parameters` for positional args, `@Option` for flags
- [ ] Gets the driver via `SessionManager.getInstance().getDriverOrThrow()`
- [ ] Uses `LocatorParser.parse()` for element locators
- [ ] Wraps logic in try/catch
- [ ] Returns `CommandResult.success(...)` or `CommandResult.error(...)`
- [ ] Registered in `SeleniumCli`'s `@Command.subcommands`

### Adding Completion Candidates

To add tab-completion suggestions for a new command's parameters or options, create a candidate provider class in the `cli.completions` package:

#### 1. Create the candidates class

Create `src/main/java/cli/completions/MyValueCandidates.java`:

```java
package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for my new parameter.
 */
public class MyValueCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "value-one",
            "value-two",
            "value-three"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}
```

**Requirements:**
- Must implement `Iterable<String>`
- Must have a **public no-arg constructor** (Picocli instantiates it via reflection)
- Return candidates via the `iterator()` method

#### 2. Wire it into the command annotation

Add `completionCandidates` to the `@Parameters` or `@Option` annotation:

```java
@Parameters(index = "0", description = "My parameter",
        completionCandidates = MyValueCandidates.class)
private String myParam;

@Option(names = "--my-option", description = "My option",
        completionCandidates = MyValueCandidates.class)
private String myOption;
```

#### 3. Register an `ArgumentCompleter` for positional parameters

> **⚠️ Important:** The picocli-shell-jline3 bridge (`PicocliCommands.compileCompleters()`)
> automatically handles `--option` completion, but does **NOT** propagate
> `completionCandidates` from `@Parameters` into JLine's tab-completion system.
> You must manually register an `ArgumentCompleter` for each command that uses
> positional parameters.

In `SeleniumCli.java`, add an entry to the `buildParameterCompleters()` method:

```java
// mycommand <param>
completers.add(new ArgumentCompleter(
        new StringsCompleter("mycommand"),
        new StringsCompleter(iterableToList(new MyValueCandidates())),
        NullCompleter.INSTANCE));
```

For commands with **multiple positional parameters**, add one completer per position:

```java
// mycommand <locator> <attribute>
completers.add(new ArgumentCompleter(
        new StringsCompleter("mycommand"),
        new StringsCompleter(iterableToList(new LocatorCandidates())),
        new StringsCompleter(iterableToList(new MyValueCandidates())),
        NullCompleter.INSTANCE));
```

> **Note:** `--option` completionCandidates work automatically through `PicocliCommands` — you
> only need this step for `@Parameters` (positional arguments).

#### 4. Build and test

```bash
mvn clean package
selenium
selenium> mycommand <Tab>
value-one    value-two    value-three
```

#### Existing candidates you can reuse

| Class | Good For |
|---|---|
| `LocatorCandidates` | Any parameter that accepts an element locator |
| `ChromeArgCandidates` | Any parameter that accepts Chrome flags |
| `AttributeCandidates` | Any parameter that accepts HTML attribute names |
| `UrlCandidates` | Any parameter that accepts a URL |

### Completion Candidates Checklist

- [ ] Class in `cli.completions` package
- [ ] Implements `Iterable<String>`
- [ ] Has a public no-arg constructor
- [ ] Returns meaningful, commonly-used values
- [ ] Wired via `completionCandidates = MyClass.class` on `@Parameters` or `@Option`
- [ ] For `@Parameters`: registered in `SeleniumCli.buildParameterCompleters()` as an `ArgumentCompleter`

---

### Code Style & Conventions

- **Java 17+** — uses text blocks, `var`, pattern matching, enhanced switch
- **All output is JSON** — never print raw strings to stdout; always use `CommandResult`
- **Singleton pattern** — `SessionManager` and `BrowserConfig` are singletons
- **No test framework yet** — contributions to add JUnit 5 tests are welcome
- **Picocli conventions** — use `@Parameters` for positional, `@Option` for named flags
- **Error handling** — catch `Exception` broadly, emit `CommandResult.error()`

### Building & Testing Locally

```bash
# Full build
mvn clean package

# Quick compile (skip shade)
mvn compile

# Run directly from compiled classes
mvn exec:java -Dexec.mainClass="cli.SeleniumCli"

# Run the fat JAR
java -jar target/selenium-cli-1.0.0.jar

# One-shot test
java -jar target/selenium-cli-1.0.0.jar open https://example.com
```

---

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| [Selenium Java](https://www.selenium.dev/) | 4.28.0 | Browser automation via WebDriver |
| [Picocli](https://picocli.info/) | 4.7.6 | CLI framework with subcommands, help generation, annotation processing |
| [picocli-shell-jline3](https://picocli.info/#_jline3_completion) | 4.7.6 | Bridges Picocli command metadata into JLine3 completers |
| [JLine3](https://github.com/jline/jline3) | 3.26.1 | Terminal handling, line editing, tab-completion, and TailTip widgets |
| [Gson](https://github.com/google/gson) | 2.12.1 | JSON serialization |
| **Maven Shade Plugin** | 3.6.0 | Produces single fat JAR |
| **Java** | 17+ | Language level |

---

## Troubleshooting

### "No active session available. Use 'open <url>' to start one."

You need to call `open <url>` before any command that interacts with the browser (`click`, `type`, `gettext`, etc.). The `open` command auto-starts a Chrome session.

### Chrome doesn't launch / ChromeDriver errors

Selenium Manager handles driver resolution automatically. Ensure:
- You have internet access (first run downloads binaries)
- Java 17+ is installed
- No conflicting `CHROMEDRIVER` environment variables

### Output is not valid JSON

Selenium's internal logging can sometimes leak to stdout. The CLI suppresses JUL logging at `SEVERE` level. If you see extra output, redirect stderr:

```bash
selenium open https://example.com 2>/dev/null
```

### "A session is already active"

Only one browser session is allowed at a time. Call `quit` before starting a new one:

```bash
selenium> quit
selenium> open https://other-site.com
```

### Headless mode shows blank screenshots

Always set a window size with headless mode:

```bash
selenium> config --headless --window-size 1920x1080
```

### Config changes not taking effect

Most config options only apply when a **new session** starts. Quit the current session first:

```bash
selenium> quit
selenium> config --headless --incognito
selenium> open https://example.com
```

---

## License

This project is provided as-is for internal test automation use.

