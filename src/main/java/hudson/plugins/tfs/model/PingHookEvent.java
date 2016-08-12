package hudson.plugins.tfs.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.StringWriter;

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
    public JSONObject perform(final JSONObject requestPayload) {
        return requestPayload;
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final JsonParser resourceParser) {
        final TreeNode treeNode;
        try {
            treeNode = mapper.readTree(resourceParser);
            final JsonFactory factory = mapper.getFactory();
            final StringWriter sw = new StringWriter();
            final JsonGenerator generator = factory.createGenerator(sw);
            mapper.writeTree(generator, treeNode);
            final String jsonString = sw.toString();
            return JSONObject.fromObject(jsonString);
        }
        catch (final IOException e) {
            throw new Error(e);
        }
    }
}
