package cli.commands;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that validate the Picocli command structure — that all commands
 * are properly registered, have the correct names, and are wired into
 * the main CLI entry point.
 *
 * This catches issues like:
 *   - A command class being removed from the subcommands array
 *   - A command annotation being misconfigured
 *   - Missing command registrations after adding new commands
 */
@DisplayName("Picocli Command Registry")
class CommandRegistryTest {

    private final CommandLine cli = new CommandLine(new cli.SeleniumCli());

    /**
     * All command names that should be registered in the CLI.
     */
    private static final Set<String> EXPECTED_COMMANDS = Set.of(
            "open", "click", "dblclick", "rightclick",
            "type", "clear", "submit", "select", "keys",
            "gettext", "getattr",
            "hover", "dragdrop", "scroll", "highlight",
            "screenshot", "navigate", "wait", "execute",
            "switchframe", "switchwindow", "tabs",
            "url", "title",
            "config", "run", "history", "session", "quit"
    );

    @Test
    @DisplayName("All expected commands are registered as subcommands")
    void allCommandsRegistered() {
        Set<String> actual = cli.getSubcommands().keySet();
        for (String expected : EXPECTED_COMMANDS) {
            assertTrue(actual.contains(expected),
                    "Missing subcommand: " + expected);
        }
    }

    @Test
    @DisplayName("No unexpected commands are registered")
    void noExtraCommands() {
        Set<String> actual = cli.getSubcommands().keySet();
        for (String cmd : actual) {
            assertTrue(EXPECTED_COMMANDS.contains(cmd),
                    "Unexpected subcommand found: " + cmd + " — add it to EXPECTED_COMMANDS if intentional");
        }
    }

    @Test
    @DisplayName("Main command is named 'selenium'")
    void mainCommandName() {
        assertEquals("selenium", cli.getCommandName());
    }

    @Test
    @DisplayName("Every subcommand implements Runnable")
    void subcommandsAreRunnable() {
        for (var entry : cli.getSubcommands().entrySet()) {
            Object cmd = entry.getValue().getCommand();
            assertTrue(cmd instanceof Runnable,
                    entry.getKey() + " should implement Runnable");
        }
    }

    @Test
    @DisplayName("Every subcommand has a description")
    void subcommandsHaveDescriptions() {
        for (var entry : cli.getSubcommands().entrySet()) {
            Command annotation = entry.getValue().getCommand().getClass().getAnnotation(Command.class);
            assertNotNull(annotation, entry.getKey() + " should have @Command annotation");
            assertTrue(annotation.description().length > 0,
                    entry.getKey() + " should have a non-empty description");
            assertFalse(annotation.description()[0].isBlank(),
                    entry.getKey() + " description should not be blank");
        }
    }
}

