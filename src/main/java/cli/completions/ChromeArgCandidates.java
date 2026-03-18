package cli.completions;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tab-completion candidates for common Chrome command-line arguments.
 */
public class ChromeArgCandidates implements Iterable<String> {

    private static final List<String> CANDIDATES = Arrays.asList(
            "--headless",
            "--headless=new",
            "--start-maximized",
            "--start-fullscreen",
            "--incognito",
            "--disable-gpu",
            "--disable-extensions",
            "--disable-popup-blocking",
            "--disable-infobars",
            "--disable-notifications",
            "--disable-translate",
            "--disable-default-apps",
            "--disable-dev-shm-usage",
            "--disable-background-networking",
            "--disable-sync",
            "--disable-web-security",
            "--disable-features=VizDisplayCompositor",
            "--no-sandbox",
            "--no-first-run",
            "--no-default-browser-check",
            "--ignore-certificate-errors",
            "--allow-insecure-localhost",
            "--allow-running-insecure-content",
            "--window-size=1920,1080",
            "--window-size=1366,768",
            "--window-size=1280,720",
            "--window-size=1440,900",
            "--window-position=0,0",
            "--force-device-scale-factor=1",
            "--remote-debugging-port=9222",
            "--user-agent=CustomAgent",
            "--proxy-server=http://host:port",
            "--lang=en-US",
            "--mute-audio",
            "--auto-open-devtools-for-tabs",
            "--enable-logging",
            "--log-level=0",
            "--blink-settings=imagesEnabled=false",
            "--disable-blink-features=AutomationControlled"
    );

    @Override
    public Iterator<String> iterator() {
        return CANDIDATES.iterator();
    }
}

