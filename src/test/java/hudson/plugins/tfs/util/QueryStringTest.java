package hudson.plugins.tfs.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link QueryString}.
 */
public class QueryStringTest {

    @Test public void toString_typical() throws Exception {
        final QueryString cut = new QueryString();
        cut.put("answer", "42");

        final String actual = cut.toString();

        Assert.assertEquals("answer=42", actual);
    }

}
