package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common HTML element attributes.
 */
public class AttributeCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "id",
            "class",
            "name",
            "value",
            "href",
            "src",
            "alt",
            "title",
            "type",
            "placeholder",
            "action",
            "method",
            "target",
            "rel",
            "role",
            "aria-label",
            "aria-hidden",
            "data-testid",
            "data-id",
            "data-value",
            "disabled",
            "checked",
            "selected",
            "readonly",
            "required",
            "style",
            "width",
            "height",
            "maxlength",
            "pattern",
            "tabindex",
            "innerHTML",
            "innerText",
            "textContent"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

