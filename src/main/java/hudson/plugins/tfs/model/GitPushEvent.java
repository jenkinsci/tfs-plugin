package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public class GitPushEvent extends AbstractHookEvent {

    public static class Factory implements AbstractHookEvent.Factory {

        @Override
        public AbstractHookEvent create(final JSONObject requestPayload) {
            return new GitPushEvent(requestPayload);
        }

        @Override
        public String getSampleRequestPayload() {
            return "TODO";
        }
    }

    public GitPushEvent(final JSONObject requestPayload) {
        super(requestPayload);
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        // TODO: implement
        return null;
    }
}
