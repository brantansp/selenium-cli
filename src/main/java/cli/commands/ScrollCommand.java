package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrolls the page vertically, horizontally, or to a specific element.
 *
 * <pre>
 *   selenium&gt; scroll --down 500
 *   selenium&gt; scroll --up 300
 *   selenium&gt; scroll --right 200
 *   selenium&gt; scroll --left 200
 *   selenium&gt; scroll --to #footer
 *   selenium&gt; scroll --bottom
 *   selenium&gt; scroll --top
 * </pre>
 */
@Command(name = "scroll", description = "Scroll the page or scroll to an element")
public class ScrollCommand implements Runnable {

    @Option(names = "--down", description = "Scroll down by N pixels")
    private Integer down;

    @Option(names = "--up", description = "Scroll up by N pixels")
    private Integer up;

    @Option(names = "--right", description = "Scroll right by N pixels")
    private Integer right;

    @Option(names = "--left", description = "Scroll left by N pixels")
    private Integer left;

    @Option(names = "--to", description = "Scroll to an element (locator)",
            completionCandidates = LocatorCandidates.class)
    private String toLocator;

    @Option(names = "--bottom", description = "Scroll to the bottom of the page")
    private boolean bottom;

    @Option(names = "--top", description = "Scroll to the top of the page")
    private boolean top;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            JavascriptExecutor js = driver;
            List<String> args = new ArrayList<>();

            if (toLocator != null) {
                WebElement el = driver.findElement(LocatorParser.parse(toLocator));
                js.executeScript("arguments[0].scrollIntoView({behavior:'smooth',block:'center'});", el);
                args.add("--to");
                args.add(toLocator);
            } else if (bottom) {
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
                args.add("--bottom");
            } else if (top) {
                js.executeScript("window.scrollTo(0, 0);");
                args.add("--top");
            } else {
                int x = 0, y = 0;
                if (down != null)  { y += down;  args.add("--down");  args.add(String.valueOf(down)); }
                if (up != null)    { y -= up;     args.add("--up");    args.add(String.valueOf(up)); }
                if (right != null) { x += right;  args.add("--right"); args.add(String.valueOf(right)); }
                if (left != null)  { x -= left;   args.add("--left");  args.add(String.valueOf(left)); }

                if (x == 0 && y == 0) {
                    CommandResult.error("scroll", List.of(),
                            "Specify a direction: --down, --up, --left, --right, --to, --top, or --bottom").print();
                    return;
                }
                js.executeScript("window.scrollBy(arguments[0], arguments[1]);", x, y);
            }

            CommandResult.success("scroll", args, null).print();
        } catch (Exception e) {
            CommandResult.error("scroll", List.of(), e.getMessage()).print();
        }
    }
}

