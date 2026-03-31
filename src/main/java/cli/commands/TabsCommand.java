package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;

import java.util.*;

/**
 * Lists all open browser tabs/windows with their handles, titles, and URLs.
 *
 * <pre>
 *   selenium&gt; tabs
 * </pre>
 */
@Command(name = "tabs", description = "List all open browser tabs/windows")
public class TabsCommand implements Runnable {

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            String currentHandle = driver.getWindowHandle();
            Set<String> handles = driver.getWindowHandles();

            List<Map<String, Object>> tabList = new ArrayList<>();
            for (String h : handles) {
                driver.switchTo().window(h);
                Map<String, Object> tab = new LinkedHashMap<>();
                tab.put("handle", h);
                tab.put("title", driver.getTitle());
                tab.put("url", driver.getCurrentUrl());
                tab.put("active", h.equals(currentHandle));
                tabList.add(tab);
            }

            // Switch back to the original window
            driver.switchTo().window(currentHandle);

            CommandResult.success("tabs", Collections.emptyList(), tabList).print();
        } catch (Exception e) {
            CommandResult.error("tabs", Collections.emptyList(), e.getMessage()).print();
        }
    }
}

