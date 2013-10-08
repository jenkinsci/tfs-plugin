package hudson.plugins.tfs.model;

import org.jvnet.hudson.test.JenkinsRule;
import org.junit.Rule;
import org.junit.Test;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.exceptions.TECoreException;

public class ServerIntegrationTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    @Test(expected = TECoreException.class)
    /**
     * It's OK for this test to throw a TECoreException for "unknown host";
     * it means we were able to load the native libraries,
     * otherwise an @link UnsatisfiedLinkError would have been thrown earlier.
     */
    public void canFindTfsSdkNativeLibraries() {
        final Server server = new Server(null, "http://tfs.invalid:8080/tfs", "username", "password");
        final TFSTeamProjectCollection tpc = server.getTeamProjectCollection();
        tpc.getVersionControlClient();
    }
}
