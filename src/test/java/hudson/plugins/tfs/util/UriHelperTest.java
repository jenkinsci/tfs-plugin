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

}
