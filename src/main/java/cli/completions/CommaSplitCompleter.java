package cli.completions;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.Map;

/**
 * A JLine {@link Completer} that provides tab-completion <em>after commas</em>
 * inside comma-separated option values.
 * <p>
 * The standard picocli–JLine bridge treats {@code --options=--headless,}
 * as a single opaque token, so pressing {@code Tab} after the comma yields
 * nothing. This completer detects that pattern and offers candidates for the
 * part after the last comma.
 * <p>
 * <b>Usage in the REPL:</b>
 * <pre>
 *   selenium&gt; open https://example.com --options=--headless,&lt;Tab&gt;
 *   --disable-gpu   --no-sandbox   --start-maximized   ...
 * </pre>
 *
 * Register this alongside the {@code SystemCompleter} via an
 * {@link org.jline.reader.impl.completer.AggregateCompleter}.
 */
public class CommaSplitCompleter implements Completer {

    /**
     * Maps an option name (e.g. {@code "--options"}) to its candidate values.
     */
    private final Map<String, List<String>> optionCandidates;

    /**
     * @param optionCandidates option-name → list of candidate values
     */
    public CommaSplitCompleter(Map<String, List<String>> optionCandidates) {
        this.optionCandidates = optionCandidates;
    }

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String word = line.word();
        if (word == null || word.isEmpty()) return;

        for (var entry : optionCandidates.entrySet()) {
            String optName = entry.getKey();
            String prefix = optName + "=";

            if (!word.startsWith(prefix)) continue;

            String valuePart = word.substring(prefix.length());

            // Only activate when there is at least one comma
            // (the first value is handled by picocli's own completer)
            if (!valuePart.contains(",")) continue;

            int lastComma = valuePart.lastIndexOf(',');
            String alreadyTyped = word.substring(0, prefix.length() + lastComma + 1);
            String partial = valuePart.substring(lastComma + 1).toLowerCase();

            // Collect values already chosen (to avoid duplicates in suggestions)
            String[] alreadyChosen = valuePart.substring(0, lastComma).split(",");
            var chosenSet = new java.util.HashSet<>(java.util.Arrays.asList(alreadyChosen));

            for (String cand : entry.getValue()) {
                if (chosenSet.contains(cand)) continue;                 // skip duplicates
                if (!cand.toLowerCase().startsWith(partial)) continue;  // filter by typed text

                candidates.add(new Candidate(
                        alreadyTyped + cand,    // value (replaces entire word)
                        cand,                   // display text in menu
                        null, null, null, null,
                        false                   // don't append space (user may add more commas)
                ));
            }
            return; // only one option pattern can match per word
        }
    }
}
