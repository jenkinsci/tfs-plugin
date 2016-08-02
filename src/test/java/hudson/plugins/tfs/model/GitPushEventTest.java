package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * A class to test {@link GitPushEvent}.
 */
public class GitPushEventTest {

    @Test
    public void determineCollectionUri_sample() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_apis/git/repositories/278d5cd2-584d-4b63-824a-2ba458937249");

        final URI actual = GitPushEvent.determineCollectionUri(input);

        final URI expected = URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void decodeGitPush() throws Exception {
        final GitPushEvent.Factory factory = new GitPushEvent.Factory();
        final String inputString = factory.getSampleRequestPayload();
        final JSONObject input = JSONObject.fromObject(inputString);

        final GitCodePushedEventArgs actual = GitPushEvent.decodeGitPush(input);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/"), actual.collectionUri);
        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_git/Fabrikam-Fiber-Git"), actual.repoUri);
        Assert.assertEquals("Fabrikam-Fiber-Git", actual.projectId);
        Assert.assertEquals("Fabrikam-Fiber-Git", actual.repoId);
        Assert.assertEquals("33b55f7cb7e7e245323987634f960cf4a6e6bc74", actual.commit);
        Assert.assertEquals("Jamal Hartnett", actual.pushedBy);
    }

}
