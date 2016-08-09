package hudson.plugins.tfs;

import hudson.util.FormValidation;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * A class to test {@link TeamCollectionConfiguration}.
 */
public class TeamCollectionConfigurationTest {

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
