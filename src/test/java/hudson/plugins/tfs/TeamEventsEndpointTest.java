package hudson.plugins.tfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.common.model.ProjectState;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPush;
import hudson.plugins.tfs.model.AbstractHookEvent;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.model.servicehooks.ResourceContainer;
import hudson.plugins.tfs.util.ResourceHelper;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to test {@link TeamEventsEndpoint}.
 */
public class TeamEventsEndpointTest {

    private static final String GIT_PUSH_SAMPLE_JSON =
            ResourceHelper.fetchAsString(TeamEventsEndpointTest.class, "git.push-sample.json");

    @Test
    public void deserializeEvent_sample() throws Exception {
        final Event actual = TeamEventsEndpoint.deserializeEvent(GIT_PUSH_SAMPLE_JSON);

        Assert.assertEquals("git.push", actual.getEventType());
        final Map<String, ResourceContainer> containers = actual.getResourceContainers();
        final ResourceContainer collection = containers.get("collection");
        Assert.assertEquals("https://fabrikam-fiber-inc.visualstudio.com/", collection.getBaseUrl());
    }

    @Test
    public void innerDispatch_fakedGitPushEventHandling() throws Exception {
        final Map<String, AbstractHookEvent.Factory> factories = new HashMap<String, AbstractHookEvent.Factory>();
        final String eventName = "fakedGitPush";
        factories.put(eventName, FakedGitPush.FACTORY);

        TeamEventsEndpoint.innerDispatch(GIT_PUSH_SAMPLE_JSON, eventName, factories);
    }

    private static class FakedGitPush extends AbstractHookEvent {

        public static final AbstractHookEvent.Factory FACTORY = new Factory() {
            @Override
            public AbstractHookEvent create() {
                return new FakedGitPush();
            }

            @Override
            public String getSampleRequestPayload() {
                return null;
            }
        };

        @Override
        public JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage) {
            final Object resource = serviceHookEvent.getResource();
            final GitPush actual = mapper.convertValue(resource, GitPush.class);
            Assert.assertEquals(ProjectState.WELL_FORMED, actual.getRepository().getProject().getState());
            return null;
        }
    }
}
