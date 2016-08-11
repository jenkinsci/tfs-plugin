package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

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
    public JSONObject perform(final AbstractProject project, final StaplerRequest request, final JSONObject requestPayload, final TimeDuration delay) {
        return requestPayload;
    }

}
