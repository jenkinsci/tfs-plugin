package hudson.plugins.tfs.model;

import hudson.plugins.tfs.util.MediaType;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class GitPullRequestMergedEvent extends GitPushEvent {

    public static class Factory implements AbstractHookEvent.Factory {

        @Override
        public AbstractHookEvent create(final JSONObject requestPayload) {
            return new GitPullRequestMergedEvent(requestPayload);
        }

        @Override
        public String getSampleRequestPayload() {
            final Class<? extends Factory> me = this.getClass();
            final InputStream stream = me.getResourceAsStream("GitPullRequestMergedEvent.json");
            try {
                return IOUtils.toString(stream, MediaType.UTF_8);
            }
            catch (final IOException e) {
                throw new Error(e);
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    public GitPullRequestMergedEvent(final JSONObject requestPayload) {
        super(requestPayload);
    }
}
