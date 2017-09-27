package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.core.webapi.model.TeamProjectReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRefUpdate;
import com.microsoft.visualstudio.services.webapi.model.IdentityRef;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.model.servicehooks.ResourceContainer;
import hudson.plugins.tfs.util.EndpointHelper;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class to test {@link GitPushEvent}.
 */
public class GitPushEventTest {

    @Test
    public void determineCollectionUri_sample() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/_apis/git/repositories/278d5cd2-584d-4b63-824a-2ba458937249");

        final URI actual = GitPushEvent.determineCollectionUri(input);

        final URI expected = URI.create("https://fabrikam-fiber-inc.visualstudio.com/");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void perform_noCommitsInPayload() throws Exception {

        final GitPushEvent gpe = new GitPushEvent();
        final ObjectMapper mapper = EndpointHelper.MAPPER;
        final Event event = new Event();
        final ResourceContainer collectionResourceContainer = new ResourceContainer() {{
            setId(UUID.fromString("c12d0eb8-e382-443b-9f9c-c52cba5014c2"));
        }};
        final Map<String, ResourceContainer> resourceContainers = new LinkedHashMap<String, ResourceContainer>() {{
            put("collection", collectionResourceContainer);
        }};
        event.setResourceContainers(resourceContainers);
        final TeamProjectReference project = new TeamProjectReference() {{
            setName("Fabrikam-Fiber-Git");
        }};
        final Map<String, Object> repository = new LinkedHashMap<String, Object>() {{
            put("url", "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_apis/git/repositories/278d5cd2-584d-4b63-824a-2ba458937249");
            put("project", project);
            put("remoteUrl", "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_git/Fabrikam-Fiber-Git");
        }};
        final IdentityRef pushedBy = new IdentityRef() {{
            setDisplayName("Jamal Hartnett");
        }};
        final ArrayList<GitRefUpdate> refUpdates = new ArrayList<GitRefUpdate>();
        final GitRefUpdate gitRefUpdate = new GitRefUpdate();
        gitRefUpdate.setName("master");
        refUpdates.add(gitRefUpdate);
        final Map<String, Object> resource = new LinkedHashMap<String, Object>() {{
            put("commits", null);
            put("repository", repository);
            put("pushedBy", pushedBy);
            put("refUpdates", refUpdates);
        }};
        event.setResource(resource);


        final JSONObject actual = gpe.perform(mapper, event, null, null);

        final JSONArray messages = actual.getJSONArray("messages");
        Assert.assertEquals(1, messages.size());
        final String message = messages.getString(0).trim();
        Assert.assertEquals("No commits were pushed, skipping further event processing.", message);
    }

}
