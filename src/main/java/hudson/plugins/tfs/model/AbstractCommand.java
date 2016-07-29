package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public abstract class AbstractCommand {

    public AbstractCommand() {
    }

    public interface Factory {
        AbstractCommand create();
        String getSampleRequestPayload();
    }

    /**
     * Actually do the work of the command, using the supplied
     * {@code requestPayload} and returning the output as a {@link JSONObject}.
     *
     * @param requestPayload a {@link JSONObject} representing the command's input
     *
     * @return a {@link JSONObject} representing the hook event's output
     */
    public abstract JSONObject perform(final JSONObject requestPayload);
}
