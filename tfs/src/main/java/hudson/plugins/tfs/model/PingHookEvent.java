//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.plugins.tfs.model.servicehooks.Event;
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
                    "    \"eventType\": \"ping\",\n" +
                    "    \"resource\":\n" +
                    "    {\n" +
                    "        \"message\": \"Hello, world!\"\n" +
                    "    }\n" +
                    "}";
        }
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage) {
        return JSONObject.fromObject(serviceHookEvent);
    }
}
