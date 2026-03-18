package cli.commands;

import cli.completions.ChromeArgCandidates;
import cli.completions.UrlCandidates;
import cli.config.BrowserConfig;
import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Opens a URL in the browser. Implicitly starts a Chrome session if none is active.
 *
 * <pre>
 *   selenium&gt; open https://google.com
 *   selenium&gt; open https://example.com --options --start-maximized,--disable-gpu
 * </pre>
 */
@Command(name = "open", description = "Navigate to a URL (starts a session if none is active)")
public class OpenCommand implements Runnable {

    @Parameters(index = "0", description = "The URL to open",
            completionCandidates = UrlCandidates.class)
    private String url;

    @Option(names = "--options", split = ",",
            description = "Extra Chrome arguments, comma-separated (applied only on new session)",
            completionCandidates = ChromeArgCandidates.class)
    private List<String> extraOptions;

    @Override
    public void run() {
        try {
            SessionManager sm = SessionManager.getInstance();

            // Auto-start session if not active
            if (!sm.isActive()) {
                BrowserConfig config = BrowserConfig.getInstance();
                if (extraOptions != null) {
                    extraOptions.forEach(config::addArgument);
                }
                sm.start(config);
            }

            sm.getDriverOrThrow().get(url);

            String currentUrl = sm.getDriverOrThrow().getCurrentUrl();
            CommandResult.success("open", List.of(url), currentUrl).print();
        } catch (Exception e) {
            CommandResult.error("open", List.of(url), e.getMessage()).print();
        }
    }
}
