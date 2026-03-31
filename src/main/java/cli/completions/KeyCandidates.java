package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for the {@code keys} command.
 */
public class KeyCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "ENTER", "TAB", "ESCAPE", "BACKSPACE", "DELETE", "SPACE",
            "UP", "DOWN", "LEFT", "RIGHT",
            "HOME", "END", "PAGEUP", "PAGEDOWN",
            "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
            "CONTROL+a", "CONTROL+c", "CONTROL+v", "CONTROL+x", "CONTROL+z",
            "SHIFT+TAB", "ALT+F4"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

