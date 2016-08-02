package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import jenkins.util.TimeDuration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;
import java.util.Map;

public class PingCommand extends AbstractCommand {

    public static class Factory implements AbstractCommand.Factory {
        @Override
        public AbstractCommand create() {
            return new PingCommand();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n" +
                    "    \"parameter\":\n" +
                    "    [\n" +
                    "        {\"name\":\"id\",\"value\":\"123\"},\n" +
                    "        {\"name\":\"verbosity\",\"value\":\"high\"}\n" +
                    "    ],\n" +
                    "    \"team-parameters\":\n" +
                    "    {\n" +
                    "        \"name\":\"value\"\n" +
                    "    }\n" +
                    "}\n";
        }
    }

    @Override
    public JSONObject perform(final AbstractProject project, final JSONObject requestPayload, final TimeDuration delay) {
        return requestPayload;
    }

    @Override
    public JSONObject perform(final AbstractProject project, final StaplerRequest req, final TimeDuration delay) {
        final JSONObject result = new JSONObject();
        final Map<String, String[]> parameters = req.getParameterMap();
        for (final Map.Entry<String, String[]> entry : parameters.entrySet()) {
            final String name = entry.getKey();
            final String[] values = entry.getValue();
            if (values != null) {
                if (values.length == 1) {
                    result.put(name, values[0]);
                }
                else {
                    final JSONArray array = new JSONArray();
                    Collections.addAll(array, values);
                    result.put(name, array);
                }
            }
        }
        return result;
    }
}
