package cli.commands;

import cli.completions.NavigationCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Browser navigation: back, forward, refresh.
 *
 * <pre>
 *   selenium&gt; navigate back
 *   selenium&gt; navigate forward
 *   selenium&gt; navigate refresh
 * </pre>
 */
@Command(name = "navigate", description = "Navigate back, forward, or refresh")
public class NavigateCommand implements Runnable {

    @Parameters(index = "0", description = "Direction: back | forward | refresh",
            completionCandidates = NavigationCandidates.class)
    private String direction;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();

            switch (direction.toLowerCase()) {
                case "back"    -> driver.navigate().back();
                case "forward" -> driver.navigate().forward();
                case "refresh" -> driver.navigate().refresh();
                default -> {
                    CommandResult.error("navigate", List.of(direction),
                            "Unknown direction '" + direction + "'. Use: back, forward, refresh").print();
                    return;
                }
            }

            String currentUrl = driver.getCurrentUrl();
            CommandResult.success("navigate", List.of(direction), currentUrl).print();
        } catch (Exception e) {
            CommandResult.error("navigate", List.of(direction), e.getMessage()).print();
        }
    }
}
