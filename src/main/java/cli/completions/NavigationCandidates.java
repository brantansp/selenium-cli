package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for the {@code navigate} direction parameter.
 */
public class NavigationCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "back",
            "forward",
            "refresh"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

