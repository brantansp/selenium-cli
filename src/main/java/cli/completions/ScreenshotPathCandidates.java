package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for screenshot file paths.
 */
public class ScreenshotPathCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "screenshot.png",
            "screenshot.jpg",
            "screenshots/page.png",
            "screenshots/error.png",
            "screenshots/step1.png",
            "screenshots/step2.png",
            "screenshots/step3.png",
            "output/screenshot.png",
            "target/screenshot.png"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

