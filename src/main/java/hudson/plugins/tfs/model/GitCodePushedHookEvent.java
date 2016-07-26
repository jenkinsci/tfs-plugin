package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public class GitCodePushedHookEvent extends AbstractHookEvent {

    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public AbstractHookEvent create(final JSONObject requestPayload) {
            return new GitCodePushedHookEvent(requestPayload);
        }
    }

    public GitCodePushedHookEvent(final JSONObject requestPayload) {
        super(requestPayload);
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        // TODO: implement
        return null;
    }
}
