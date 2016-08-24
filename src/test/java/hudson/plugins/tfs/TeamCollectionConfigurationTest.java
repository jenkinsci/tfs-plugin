package hudson.plugins.tfs;

import hudson.util.FormValidation;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * A class to test {@link TeamCollectionConfiguration}.
 */
public class TeamCollectionConfigurationTest {

    private static void assertSameCollectionUri(final String a, final String b) {
        areSameCollectionUri(a, b, true);
    }

    private static void areSameCollectionUri(final String a, final String b, boolean expected) {
        final URI uriA = a == null ? null : URI.create(a);
        final URI uriB = b == null ? null : URI.create(b);
        final String template = "Expected '%s' and '%s' to be considered%s the same.";
        final String message = String.format(template, a, b, expected ? "" : " NOT");
        Assert.assertEquals(message, expected, TeamCollectionConfiguration.areSameCollectionUri(uriA, uriB));
        Assert.assertEquals(message, expected, TeamCollectionConfiguration.areSameCollectionUri(uriB, uriA));
    }

    @Test public void areSameCollectionUri_identity() throws Exception {
        final String input = "https://fabrikam-fiber-inc.visualstudio.com/";
        assertSameCollectionUri(input, input);
    }

    @Test public void areSameCollectionUri_typical() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection";

        assertSameCollectionUri(a, b);
    }

    @Test public void areSameCollectionUri_withSlashes() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/";

        assertSameCollectionUri(a, b);
    }

    @Test public void areSameCollectionUri_withoutSlashes() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection";

        assertSameCollectionUri(a, b);
    }


    @Test public void checkTeamServices_serverOnly() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com");

        final FormValidation actual = TeamCollectionConfiguration.checkTeamServices(input);

        Assert.assertEquals(FormValidation.Kind.OK, actual.kind);
    }

    @Test public void checkTeamServices_serverWithSlash() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/");

        final FormValidation actual = TeamCollectionConfiguration.checkTeamServices(input);

        Assert.assertEquals(FormValidation.Kind.OK, actual.kind);
    }

    @Test public void checkTeamServices_serverWithDefaultCollection() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection");

        final FormValidation actual = TeamCollectionConfiguration.checkTeamServices(input);

        Assert.assertEquals(FormValidation.Kind.ERROR, actual.kind);
    }

    @Test public void checkTeamServices_serverWithDefaultCollectionSlash() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/");

        final FormValidation actual = TeamCollectionConfiguration.checkTeamServices(input);

        Assert.assertEquals(FormValidation.Kind.ERROR, actual.kind);
    }

    @Test public void checkTeamServices_gitUrl() throws Exception {
        final URI input = URI.create("https://fabrikam-fiber-inc.visualstudio.com/_git/Fabrikam");

        final FormValidation actual = TeamCollectionConfiguration.checkTeamServices(input);

        Assert.assertEquals(FormValidation.Kind.ERROR, actual.kind);
    }

}
