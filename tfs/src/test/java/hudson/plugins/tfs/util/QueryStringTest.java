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

    @Test public void constructor_typical() throws Exception {
        //noinspection MismatchedQueryAndUpdateOfCollection
        final QueryString cut = new QueryString("answer", "42");

        final String actual = cut.toString();

        Assert.assertEquals("answer=42", actual);
    }

    @Test public void constructor_twoPairs() throws Exception {
        //noinspection MismatchedQueryAndUpdateOfCollection
        final QueryString cut = new QueryString("answer", "42", "question", "whatdoyoug");

        final String actual = cut.toString();

        Assert.assertEquals("answer=42&question=whatdoyoug", actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_oddParameters() throws Exception {
        new QueryString("answer");
    }

}
