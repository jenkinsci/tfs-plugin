package hudson.plugins.tfs.model;

import org.eclipse.jgit.transport.URIish;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link AbstractHookEvent}.
 */
public class AbstractHookEventTest {

    private static void isTeamServicesNearMatch(final String a, final String b, final boolean expected) throws Exception {
        final URIish uriA = a == null ? null : new URIish(a);
        final URIish uriB = b == null ? null : new URIish(b);
        final String template = "Expected '%s' and '%s' to be considered%s the same.";
        final String message = String.format(template, a, b, expected ? "" : " NOT");
        Assert.assertEquals(message, expected, AbstractHookEvent.isTeamServicesNearMatch(uriA, uriB));
        Assert.assertEquals(message, expected, AbstractHookEvent.isTeamServicesNearMatch(uriB, uriA));
    }

    private static void assertIsTeamServicesNearMatch(final String a, final String b) throws Exception {
        isTeamServicesNearMatch(a, b, true);
    }

    private static void assertIsNotTeamServicesNearMatch(final String a, final String b) throws Exception {
        isTeamServicesNearMatch(a, b, false);
    }

    @Test public void isTeamServicesNearMatch_identity() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/Project/_git/Fabrikam";
        assertIsTeamServicesNearMatch(a, a);
    }

    @Test public void isTeamServicesNearMatch_typical() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/Project/_git/Fabrikam";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/Project/_git/Fabrikam";

        assertIsTeamServicesNearMatch(a, b);
    }

    @Test public void isTeamServicesNearMatch_typicalWithSlashWithoutSlash() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/Project/_git/Fabrikam/";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/Project/_git/Fabrikam";

        assertIsTeamServicesNearMatch(a, b);
    }

    @Test public void isTeamServicesNearMatch_typicalWithoutSlashWithSlash() throws Exception {
        final String a = "https://fabrikam-fiber-inc.visualstudio.com/Project/_git/Fabrikam";
        final String b = "https://fabrikam-fiber-inc.visualstudio.com/DefaultCollection/Project/_git/Fabrikam/";

        assertIsTeamServicesNearMatch(a, b);
    }

    @Test public void isTeamServicesNearMatch_tfs() throws Exception {
        final String a = "http://tfs:8080/tfs/DefaultCollection/Project/_git/Fabrikam";
        final String b = "http://tfs:8080/tfs/DefaultCollection/Project/_git/Fabrikam/";

        assertIsNotTeamServicesNearMatch(a, b);
    }

    @Test public void isTeamServicesNearMatch_gitHub() throws Exception {
        final String a = "https://github.com/jenkinsci/tfs-plugin.git";
        final String b = "git@github.com:jenkinsci/tfs-plugin.git";

        assertIsNotTeamServicesNearMatch(a, b);
    }

}
