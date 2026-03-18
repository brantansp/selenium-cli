package cli.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Centralised Gson instance for all JSON serialisation.
 */
public final class JsonOutput {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private JsonOutput() {}

    public static String toJson(Object obj) {
        return GSON.toJson(obj);
    }

    public static Gson gson() {
        return GSON;
    }
}

