package cli.commands;

import cli.completions.JsSnippetCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import org.openqa.selenium.JavascriptExecutor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Executes arbitrary JavaScript in the current page context.
 *
 * <pre>
 *   selenium&gt; execute "return document.title"
 *   selenium&gt; execute "document.querySelector('h1').style.color='red'"
 * </pre>
 */
@Command(name = "execute", description = "Execute JavaScript in the browser")
public class ExecuteJsCommand implements Runnable {

    @Parameters(index = "0", description = "JavaScript code to execute",
            completionCandidates = JsSnippetCandidates.class)
    private String script;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            Object returnValue = ((JavascriptExecutor) driver).executeScript(script);
            String result = returnValue != null ? returnValue.toString() : null;
            CommandResult.success("execute", List.of(script), result).print();
        } catch (Exception e) {
            CommandResult.error("execute", List.of(script), e.getMessage()).print();
        }
    }
}
