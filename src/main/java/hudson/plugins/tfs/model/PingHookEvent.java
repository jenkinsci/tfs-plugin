package hudson.plugins.tfs.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public JSONObject perform(final ObjectMapper mapper, final JsonParser resourceParser) {
        return null;
    }
}
