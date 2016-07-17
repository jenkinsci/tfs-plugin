package hudson.plugins.tfs.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link GitStatusContext}.
 */
public class GitStatusContextTest {

    public static final String FORMATTED_INPUT =
        "{\n" +
        "    \"name\":\"Build123\",\n" +
        "    \"genre\":\"continuous-integration\"\n" +
        "}";

    @Test public void fromJsonString_typical() throws Exception {

        final GitStatusContext actual = GitStatusContext.fromJsonString(FORMATTED_INPUT);

        Assert.assertEquals("Build123", actual.name);
        Assert.assertEquals("continuous-integration", actual.genre);
    }
}
