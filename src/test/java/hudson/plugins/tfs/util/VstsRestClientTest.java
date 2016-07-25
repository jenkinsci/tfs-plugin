package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.plugins.tfs.IntegrationTestHelper;
import hudson.plugins.tfs.IntegrationTests;
import hudson.util.SecretOverride;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;

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
            "buildAccount",
            null,
            "PAT",
            personalAccessToken);

        final String actual = VstsRestClient.createAuthorization(creds);

        Assert.assertEquals("Basic UEFUOmFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFhYWE=", actual);
    }

    @Ignore("Only works on visualstudio.com due to the use of the Authorization header")
    @Category(IntegrationTests.class)
    @Test public void ping() throws Exception {
        final IntegrationTestHelper helper = new IntegrationTestHelper();
        final URI collectionUri = new URI(helper.getServerUrl());
        final StandardUsernamePasswordCredentials creds = new UsernamePasswordCredentialsImpl(
                CredentialsScope.SYSTEM,
                "buildAccount",
                null,
                helper.getUserName(),
                helper.getUserPassword());
        final VstsRestClient cut = new VstsRestClient(collectionUri, creds);

        cut.ping();
    }
}
