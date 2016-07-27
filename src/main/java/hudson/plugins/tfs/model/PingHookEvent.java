package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public class PingHookEvent extends AbstractHookEvent {

    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public AbstractHookEvent create(final JSONObject requestPayload) {
            return new PingHookEvent(requestPayload);
        }
    }

    public PingHookEvent(final JSONObject requestPayload) {
        super(requestPayload);
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        return requestPayload;
    }
}
