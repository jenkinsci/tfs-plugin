package hudson.plugins.tfs.model;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

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

}
