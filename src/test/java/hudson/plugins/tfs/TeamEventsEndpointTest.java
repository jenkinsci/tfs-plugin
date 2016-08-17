package hudson.plugins.tfs;

import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.model.servicehooks.ResourceContainer;
import hudson.plugins.tfs.util.ResourceHelper;
import org.junit.Assert;
import org.junit.Test;

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

}
