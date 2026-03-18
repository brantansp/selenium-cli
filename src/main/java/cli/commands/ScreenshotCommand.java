package cli.commands;

import cli.completions.ScreenshotPathCandidates;
import cli.model.CommandResult;
import cli.session.SessionManager;
import org.openqa.selenium.OutputType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Saves a screenshot of the current page.
 *
 * <pre>
 *   selenium&gt; screenshot
 *   selenium&gt; screenshot ./captures/home.png
 * </pre>
 */
@Command(name = "screenshot", description = "Take a screenshot of the current page")
public class ScreenshotCommand implements Runnable {

    @Parameters(index = "0", defaultValue = "screenshot.png",
            description = "File path to save the screenshot (default: screenshot.png)",
            completionCandidates = ScreenshotPathCandidates.class)
    private String filepath;

    @Override
    public void run() {
        try {
            var driver = SessionManager.getInstance().getDriverOrThrow();
            File tmpFile = driver.getScreenshotAs(OutputType.FILE);
            File dest = new File(filepath).getAbsoluteFile();
            if (dest.getParentFile() != null) {
                dest.getParentFile().mkdirs();
            }
            Files.copy(tmpFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            CommandResult.success("screenshot", List.of(filepath), dest.getAbsolutePath()).print();
        } catch (Exception e) {
            CommandResult.error("screenshot", List.of(filepath), e.getMessage()).print();
        }
    }
}
