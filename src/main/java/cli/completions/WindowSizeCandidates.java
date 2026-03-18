package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common window/viewport sizes.
 */
public class WindowSizeCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "1920x1080",
            "1366x768",
            "1440x900",
            "1536x864",
            "1280x720",
            "1280x1024",
            "1600x900",
            "1024x768",
            "800x600",
            "375x812",
            "390x844",
            "414x896",
            "360x740",
            "768x1024",
            "820x1180",
            "1280x800"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

