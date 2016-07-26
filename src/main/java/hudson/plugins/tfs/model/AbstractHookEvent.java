package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractHookEvent implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(AbstractHookEvent.class.getName());

    protected JSONObject requestPayload;
    private JSONObject response;

    public AbstractHookEvent(final JSONObject requestPayload) {
        this.requestPayload = requestPayload;
    }

    public JSONObject getResponse() {
        return response;
    }

    /**
     * Actually do the work of the hook event, using the supplied
     * {@code requestPayload} and returning the output as a {@link JSONObject}.
     *
     * @param requestPayload a {@link JSONObject} representing the hook event's input
     *
     * @return a {@link JSONObject} representing the hook event's output
     */
    public abstract JSONObject perform(final JSONObject requestPayload);

    public void run() {
        try {
            response = perform(requestPayload);
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while performing reaction to event.", e);
            // TODO: serialize it to JSON and set as the response
            //response = toJSON(e);
        }
    }
}
