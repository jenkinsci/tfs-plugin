package hudson.plugins.tfs.model.servicehooks;

import hudson.plugins.tfs.util.EndpointHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link EventScope}.
 */
public class EventScopeTest {

    @Test
    public void deserialize_enumCasing() throws Exception {
        final String input = "{\"scope\": \"all\"}";

        final Event actual = EndpointHelper.MAPPER.readValue(input, Event.class);

        Assert.assertEquals(EventScope.All, actual.getScope());
    }

}
