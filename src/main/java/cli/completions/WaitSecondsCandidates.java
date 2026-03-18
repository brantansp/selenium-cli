package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common wait durations (seconds).
 */
public class WaitSecondsCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "1",
            "2",
            "3",
            "5",
            "10",
            "15",
            "20",
            "30",
            "60"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

