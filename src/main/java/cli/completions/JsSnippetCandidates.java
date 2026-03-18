package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common JavaScript snippets.
 */
public class JsSnippetCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "return document.title",
            "return document.URL",
            "return document.readyState",
            "return document.cookie",
            "return document.referrer",
            "return document.domain",
            "return document.body.scrollHeight",
            "return document.body.scrollWidth",
            "return document.querySelectorAll('a').length",
            "return document.querySelectorAll('img').length",
            "return window.innerHeight",
            "return window.innerWidth",
            "return window.scrollY",
            "return navigator.userAgent",
            "return localStorage.length",
            "return sessionStorage.length",
            "return performance.timing.loadEventEnd - performance.timing.navigationStart",
            "window.scrollTo(0, 0)",
            "window.scrollTo(0, document.body.scrollHeight)",
            "window.scrollBy(0, 500)",
            "window.scrollBy(0, -500)",
            "document.querySelector('').click()",
            "document.querySelector('').value=''",
            "document.querySelector('').style.display='none'",
            "document.querySelector('').style.border='2px solid red'",
            "document.querySelector('').scrollIntoView()",
            "document.querySelector('').remove()",
            "alert('test')",
            "confirm('test')",
            "console.clear()"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

