package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common HTTP headers (Name:Value format).
 */
public class HeaderCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "Authorization:Bearer token",
            "Content-Type:application/json",
            "Accept:application/json",
            "Accept:text/html",
            "Accept-Language:en-US",
            "Cache-Control:no-cache",
            "X-Requested-With:XMLHttpRequest",
            "X-Custom-Header:value",
            "Referer:https://example.com",
            "Origin:https://example.com",
            "Cookie:name=value"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

