package cli.commands;

import cli.completions.KeyCandidates;
import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;
import java.util.Map;

/**
 * Sends special keyboard keys to an element or to the active element.
 *
 * <pre>
 *   selenium&gt; keys ENTER
 *   selenium&gt; keys TAB
 *   selenium&gt; keys --to #search ENTER
 *   selenium&gt; keys CONTROL+a
 *   selenium&gt; keys CONTROL+c
 * </pre>
 */
@Command(name = "keys", description = "Send special keyboard keys (ENTER, TAB, ESCAPE, etc.)")
public class KeysCommand implements Runnable {

    private static final Map<String, CharSequence> KEY_MAP = Map.ofEntries(
            Map.entry("ENTER",      Keys.ENTER),
            Map.entry("RETURN",     Keys.RETURN),
            Map.entry("TAB",        Keys.TAB),
            Map.entry("ESCAPE",     Keys.ESCAPE),
            Map.entry("ESC",        Keys.ESCAPE),
            Map.entry("BACKSPACE",  Keys.BACK_SPACE),
            Map.entry("DELETE",     Keys.DELETE),
            Map.entry("SPACE",      Keys.SPACE),
            Map.entry("UP",         Keys.ARROW_UP),
            Map.entry("DOWN",       Keys.ARROW_DOWN),
            Map.entry("LEFT",       Keys.ARROW_LEFT),
            Map.entry("RIGHT",      Keys.ARROW_RIGHT),
            Map.entry("HOME",       Keys.HOME),
            Map.entry("END",        Keys.END),
            Map.entry("PAGEUP",     Keys.PAGE_UP),
            Map.entry("PAGEDOWN",   Keys.PAGE_DOWN),
            Map.entry("F1",         Keys.F1),
            Map.entry("F2",         Keys.F2),
            Map.entry("F3",         Keys.F3),
            Map.entry("F4",         Keys.F4),
            Map.entry("F5",         Keys.F5),
            Map.entry("F6",         Keys.F6),
            Map.entry("F7",         Keys.F7),
            Map.entry("F8",         Keys.F8),
            Map.entry("F9",         Keys.F9),
            Map.entry("F10",        Keys.F10),
            Map.entry("F11",        Keys.F11),
            Map.entry("F12",        Keys.F12),
            Map.entry("CONTROL",    Keys.CONTROL),
            Map.entry("CTRL",       Keys.CONTROL),
            Map.entry("ALT",        Keys.ALT),
            Map.entry("SHIFT",      Keys.SHIFT)
    );

    @Parameters(index = "0", description = "Key name (ENTER, TAB, ESCAPE, etc.) or combo (CONTROL+a)",
            completionCandidates = KeyCandidates.class)
    private String keyName;

    @Option(names = "--to", description = "Target element locator (defaults to active element)",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            Actions actions = new Actions(driver);

            // Support combos like CONTROL+a, SHIFT+TAB
            String[] parts = keyName.split("\\+");
            CharSequence[] resolvedKeys = new CharSequence[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i].trim().toUpperCase();
                CharSequence mapped = KEY_MAP.get(part);
                resolvedKeys[i] = (mapped != null) ? mapped : parts[i].trim();
            }

            if (locator != null) {
                WebElement el = driver.findElement(LocatorParser.parse(locator));
                if (resolvedKeys.length == 1) {
                    el.sendKeys(resolvedKeys[0]);
                } else {
                    // For combos: hold modifier(s) then press the last key
                    for (int i = 0; i < resolvedKeys.length - 1; i++) {
                        actions = actions.keyDown(el, resolvedKeys[i]);
                    }
                    actions.sendKeys(resolvedKeys[resolvedKeys.length - 1]);
                    for (int i = resolvedKeys.length - 2; i >= 0; i--) {
                        actions = actions.keyUp(el, resolvedKeys[i]);
                    }
                    actions.perform();
                }
            } else {
                // Send to the active element
                if (resolvedKeys.length == 1) {
                    actions.sendKeys(resolvedKeys[0]).perform();
                } else {
                    for (int i = 0; i < resolvedKeys.length - 1; i++) {
                        actions = actions.keyDown(resolvedKeys[i]);
                    }
                    actions.sendKeys(resolvedKeys[resolvedKeys.length - 1]);
                    for (int i = resolvedKeys.length - 2; i >= 0; i--) {
                        actions = actions.keyUp(resolvedKeys[i]);
                    }
                    actions.perform();
                }
            }

            CommandResult.success("keys", List.of(keyName), null).print();
        } catch (Exception e) {
            CommandResult.error("keys", List.of(keyName), e.getMessage()).print();
        }
    }
}

