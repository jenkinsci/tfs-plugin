package hudson.plugins.tfs.model;

import org.kohsuke.stapler.StaplerRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.model.Job;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;

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
                    "    ]\n" +
                    "}\n";
        }
    }

    @Override
    public JSONObject perform(final Job project, final StaplerRequest request, final JSONObject requestPayload, final ObjectMapper mapper, final TeamBuildPayload teamBuildPayload, final TimeDuration delay) {
        return requestPayload;
    }

}
