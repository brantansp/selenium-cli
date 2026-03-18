package cli.commands;

import cli.completions.AttributeCandidates;
import cli.completions.LocatorCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import cli.util.LocatorParser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Returns the value of an attribute on an element.
 *
 * <pre>
 *   selenium&gt; getattr #logo src
 *   selenium&gt; getattr //input[@name='q'] value
 * </pre>
 */
@Command(name = "getattr", description = "Get an attribute value of an element")
public class GetAttrCommand implements Runnable {

    @Parameters(index = "0", description = "Element locator",
            completionCandidates = LocatorCandidates.class)
    private String locator;

    @Parameters(index = "1", description = "Attribute name",
            completionCandidates = AttributeCandidates.class)
    private String attribute;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            String value = driver.findElement(LocatorParser.parse(locator)).getDomAttribute(attribute);
            CommandResult.success("getattr", List.of(locator, attribute), value).print();
        } catch (Exception e) {
            CommandResult.error("getattr", List.of(locator, attribute), e.getMessage()).print();
        }
    }
}
