package cli.commands;

import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Selects an option from a {@code <select>} dropdown.
 *
 * <pre>
 *   selenium&gt; select #country "United States"
 *   selenium&gt; select #country --value us
 *   selenium&gt; select #country --index 3
 * </pre>
 */
@Command(name = "select", description = "Select an option from a dropdown")
public class SelectCommand implements Runnable {

    @Parameters(index = "0", description = "Locator for the <select> element",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Parameters(index = "1", arity = "0..1",
            description = "Visible text of the option to select")
    private String text;

    @Option(names = "--value", description = "Select by option value attribute")
    private String value;

    @Option(names = "--index", description = "Select by zero-based index")
    private Integer index;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            WebElement element = driver.findElement(LocatorParser.parse(locator));
            Select dropdown = new Select(element);

            String description;
            if (value != null) {
                dropdown.selectByValue(value);
                description = "Selected by value: " + value;
            } else if (index != null) {
                dropdown.selectByIndex(index);
                description = "Selected by index: " + index;
            } else if (text != null) {
                dropdown.selectByVisibleText(text);
                description = "Selected by text: " + text;
            } else {
                CommandResult.error("select", List.of(locator),
                        "Specify text, --value, or --index").print();
                return;
            }

            CommandResult.success("select", List.of(locator), description).print();
        } catch (Exception e) {
            CommandResult.error("select", List.of(locator), e.getMessage()).print();
        }
    }
}

