package hudson.plugins.tfs.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

/**
 * A class to test {@link UriHelper}.
 */
public class UriHelperTest {

    private QueryString lifeUniverseEverything;

    @Before public void setUp() {
        lifeUniverseEverything = new QueryString();
        lifeUniverseEverything.put("answer", "42");
    }


    @Test public void join_uriNoSlash_pathComponents() throws Exception {
        final URI collectionUri = URI.create("https://fabrikam-fiber-inc.visualstudio.com");

        final URI actual = UriHelper.join(collectionUri, "_home", "About");

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About"), actual);
    }

    @Test public void join_noSlash_pathComponents() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com";

        final URI actual = UriHelper.join(collectionUrl, "_home", "About");

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About"), actual);
    }

    @Test public void join_withSlash_pathComponents() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";

        final URI actual = UriHelper.join(collectionUrl, "_home", "About");

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About"), actual);
    }

    @Test public void join_noSlash_queryString() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";

        final URI actual = UriHelper.join(collectionUrl, lifeUniverseEverything);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/?answer=42"), actual);
    }

    @Test public void join_withSlash_queryString() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";

        final URI actual = UriHelper.join(collectionUrl, lifeUniverseEverything);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/?answer=42"), actual);
    }

    @Test public void join_noSlash_pathAndQueryString() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";

        final URI actual = UriHelper.join(collectionUrl, "_home", "About", lifeUniverseEverything);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About?answer=42"), actual);
    }

    @Test public void join_withSlash_pathAndQueryString() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";

        final URI actual = UriHelper.join(collectionUrl, "_home", "About", lifeUniverseEverything);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About?answer=42"), actual);
    }

    @Test public void join_sampleApiCall() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com/";
        final QueryString qs = new QueryString("api-version", "2.0");

        final URI actual = UriHelper.join(collectionUrl, "DefaultCollection", "_apis", "projects", qs);

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/_apis/projects?api-version=2.0"), actual);
    }

}
