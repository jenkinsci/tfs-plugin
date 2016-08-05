package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public class PingHookEvent extends AbstractHookEvent {

    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public AbstractHookEvent create() {
            return new PingHookEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n" +
                    "    \"message\": \"Hello, world!\"\n" +
                    "}";
        }
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        return requestPayload;
    }
}
