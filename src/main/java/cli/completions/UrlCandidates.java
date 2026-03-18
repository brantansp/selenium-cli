package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common URLs.
 */
public class UrlCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "https://",
            "http://",
            "https://www.google.com",
            "https://www.github.com",
            "https://www.example.com",
            "http://localhost:3000",
            "http://localhost:8080",
            "http://localhost:4200",
            "file:///"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

