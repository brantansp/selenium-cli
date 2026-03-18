package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for Chrome browser versions.
 */
public class BrowserVersionCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "stable",
            "beta",
            "dev",
            "canary",
            "130",
            "131",
            "132",
            "133",
            "134"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

