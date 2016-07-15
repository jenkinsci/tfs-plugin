package hudson.plugins.tfs.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * A class to test {@link UriHelper}.
 */
public class UriHelperTest {

    @Test public void join_noSlash_pathComponents() throws Exception {
        final String collectionUrl = "https://fabrikam-fiber-inc.visualstudio.com";

        final URI actual = UriHelper.join(collectionUrl, "_home", "About");

        Assert.assertEquals(URI.create("https://fabrikam-fiber-inc.visualstudio.com/_home/About"), actual);
    }

}
