package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.util.SecretOverride;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A class to test {@link VstsRestClient}.
 */
public class VstsRestClientTest {

    private SecretOverride secretOverride = null;

    @Before public void setUp() throws Exception {
        secretOverride = new SecretOverride();
    }

    @After public void tearDown() throws Exception {
        if (secretOverride != null) {
            secretOverride.close();
        }
    }

    @Test public void createAuthorization_typical() throws Exception {
        final String personalAccessToken = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        final StandardUsernamePasswordCredentials creds = new UsernamePasswordCredentialsImpl(
            CredentialsScope.SYSTEM,
            "vstsBuildAccount",
            null,
            "PAT",
            personalAccessToken);

        final String actual = VstsRestClient.createAuthorization(creds);

        Assert.assertEquals("Basic UEFUOmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=", actual);
    }
}
