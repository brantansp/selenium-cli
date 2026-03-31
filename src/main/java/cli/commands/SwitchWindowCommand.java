package cli.commands;

import cli.model.CommandResult;
import cli.session.SessionManager;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Switch between browser windows/tabs, open a new tab, or close the current one.
 *
 * <pre>
 *   selenium&gt; switchwindow --next
 *   selenium&gt; switchwindow --previous
 *   selenium&gt; switchwindow --new
 *   selenium&gt; switchwindow --close
 *   selenium&gt; switchwindow &lt;handle&gt;
 * </pre>
 */
@Command(name = "switchwindow", description = "Switch between browser windows/tabs")
public class SwitchWindowCommand implements Runnable {

    @Parameters(index = "0", arity = "0..1",
            description = "Window handle to switch to")
    private String handle;

    @Option(names = "--next", description = "Switch to the next window/tab")
    private boolean next;

    @Option(names = "--previous", description = "Switch to the previous window/tab")
    private boolean previous;

    @Option(names = "--new", description = "Open a new blank tab and switch to it")
    private boolean newTab;

    @Option(names = "--close", description = "Close the current tab and switch to the remaining one")
    private boolean close;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();

            if (newTab) {
                driver.switchTo().newWindow(org.openqa.selenium.WindowType.TAB);
                CommandResult.success("switchwindow", List.of("--new"),
                        "Opened new tab: " + driver.getWindowHandle()).print();
                return;
            }

            if (close) {
                String current = driver.getWindowHandle();
                Set<String> handles = driver.getWindowHandles();
                driver.close();
                // Switch to the first remaining handle
                for (String h : handles) {
                    if (!h.equals(current)) {
                        driver.switchTo().window(h);
                        break;
                    }
                }
                CommandResult.success("switchwindow", List.of("--close"),
                        "Closed tab, switched to: " + driver.getWindowHandle()).print();
                return;
            }

            List<String> handleList = new ArrayList<>(driver.getWindowHandles());
            String currentHandle = driver.getWindowHandle();
            int currentIndex = handleList.indexOf(currentHandle);

            if (next) {
                int nextIndex = (currentIndex + 1) % handleList.size();
                driver.switchTo().window(handleList.get(nextIndex));
                CommandResult.success("switchwindow", List.of("--next"),
                        "Switched to: " + handleList.get(nextIndex)).print();
            } else if (previous) {
                int prevIndex = (currentIndex - 1 + handleList.size()) % handleList.size();
                driver.switchTo().window(handleList.get(prevIndex));
                CommandResult.success("switchwindow", List.of("--previous"),
                        "Switched to: " + handleList.get(prevIndex)).print();
            } else if (handle != null) {
                driver.switchTo().window(handle);
                CommandResult.success("switchwindow", List.of(handle),
                        "Switched to: " + handle).print();
            } else {
                CommandResult.error("switchwindow", List.of(),
                        "Specify --next, --previous, --new, --close, or a window handle").print();
            }
        } catch (Exception e) {
            CommandResult.error("switchwindow", List.of(), e.getMessage()).print();
        }
    }
}

