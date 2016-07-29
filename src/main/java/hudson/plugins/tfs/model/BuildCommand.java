package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;

public class BuildCommand extends AbstractCommand {

    public static class Factory implements AbstractCommand.Factory {
        @Override
        public AbstractCommand create() {
            return new BuildCommand();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n" +
                    "    \"team-parameters\":\n" +
                    "    {\n" +
                    "        \"collectionUri\":\"https://fabrikam-fiber-inc.visualstudio.com\",\n" +
                    "        \"repoUri\":\"https://fabrikam-fiber-inc.visualstudio.com/Personal/_git/olivida.tfs-plugin\",\n" +
                    "        \"projectId\":\"Personal\",\n" +
                    "        \"repoId\":\"olivida.tfs-plugin\",\n" +
                    "        \"commit\":\"6a23fc7afec31f0a14bade6544bed4f16492e6d2\",\n" +
                    "        \"pushedBy\":\"olivida\"\n" +
                    "    }\n" +
                    "}";
        }
    }

    @Override
    public JSONObject perform(final AbstractProject project, final TimeDuration delay, final JSONObject requestPayload) {
        // TODO: implement
        return requestPayload;
    }
}
