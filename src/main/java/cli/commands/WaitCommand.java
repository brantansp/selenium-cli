package cli.commands;

import cli.completions.WaitSecondsCandidates;
import cli.model.CommandResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Pauses execution for a given number of seconds.
 *
 * <pre>
 *   selenium&gt; wait 3
 * </pre>
 */
@Command(name = "wait", description = "Pause execution for N seconds")
public class WaitCommand implements Runnable {

    @Parameters(index = "0", description = "Seconds to wait",
            completionCandidates = WaitSecondsCandidates.class)
    private int seconds;

    @Override
    public void run() {
        try {
            Thread.sleep(seconds * 1000L);
            CommandResult.success("wait", List.of(String.valueOf(seconds)),
                    "Waited " + seconds + "s").print();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CommandResult.error("wait", List.of(String.valueOf(seconds)),
                    "Wait interrupted").print();
        }
    }
}
