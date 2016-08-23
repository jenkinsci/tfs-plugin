package hudson.plugins.tfs.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link StringHelper}.
 */
public class StringHelperTest {

    @Test public void determineContentTypeWithoutCharset_null() throws Exception {
        final String actual = StringHelper.determineContentTypeWithoutCharset(null);

        Assert.assertEquals(null, actual);
    }

    @Test public void determineContentTypeWithoutCharset_withoutCharset() throws Exception {
        final String input = "application/json";

        final String actual = StringHelper.determineContentTypeWithoutCharset(input);

        Assert.assertEquals("application/json", actual);
    }

    @Test public void determineContentTypeWithoutCharset_withCharset() throws Exception {
        final String input = "application/json; charset=utf-8";

        final String actual = StringHelper.determineContentTypeWithoutCharset(input);

        Assert.assertEquals("application/json", actual);
    }

}
