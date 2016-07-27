package hudson.plugins.tfs.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link PullRequestMergeCommitCreatedEventArgs}.
 */
public class PullRequestMergeCommitCreatedEventArgsTest {

    @Test
    public void fromJsonString_scenario() throws Exception {

        final PullRequestMergeCommitCreatedEventArgs actual = PullRequestMergeCommitCreatedEventArgs.fromJsonString(PullRequestMergeCommitCreatedEventArgs.SAMPLE_REQUEST_PAYLOAD);

        Assert.assertEquals("https://fabrikam-fiber-inc.visualstudio.com", actual.collectionUri.toString());
        Assert.assertEquals("https://fabrikam-fiber-inc.visualstudio.com/Personal/_git/olivida.tfs-plugin", actual.repoUri.toString());
        Assert.assertEquals("Personal", actual.projectId);
        Assert.assertEquals("olivida.tfs-plugin", actual.repoId);
        Assert.assertEquals("6a23fc7afec31f0a14bade6544bed4f16492e6d2", actual.commit);
        Assert.assertEquals("olivida", actual.pushedBy);
        Assert.assertEquals(42, actual.pullRequestId);
        Assert.assertEquals(2, actual.iterationId);

        final WorkItem firstWorkItem = actual.workItems.get(0);
        final Link htmlLink = firstWorkItem._links.get("html");
        Assert.assertEquals("https://fabrikam-fiber-inc.visualstudio.com/web/wi.aspx?pcguid=d81542e4-cdfa-4333-b082-1ae2d6c3ad16&id=297", htmlLink.href.toString());

        final Object systemTitle = firstWorkItem.fields.get("System_Title");
        Assert.assertEquals("Customer can sign in using their Microsoft Account", systemTitle);
    }

}
