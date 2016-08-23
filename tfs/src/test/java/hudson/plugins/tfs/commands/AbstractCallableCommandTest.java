package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.WebProxySettings;
import hudson.remoting.Callable;
import hudson.util.Secret;
import hudson.util.SecretOverride;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractCallableCommandTest {

    protected Server server;
    protected MockableVersionControlClient vcc;
    protected TaskListener listener;
    protected ByteArrayOutputStream outputStream;

    @Before
    public void configureDefaultMocks() {
        server = mock(Server.class);
        vcc = mock(MockableVersionControlClient.class);
        listener = mock(TaskListener.class);
        outputStream = new ByteArrayOutputStream();

        when(server.getVersionControlClient()).thenReturn(vcc);
        when(server.getListener()).thenReturn(listener);
        when(listener.getLogger()).thenReturn(new PrintStream(outputStream));
    }

    protected void assertLog(final String... expectedLines) throws IOException {
        final byte[] outputBytes = outputStream.toByteArray();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputBytes);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        final Iterator<String> expectedIterator = IteratorUtils.arrayIterator(expectedLines);

        String line;
        int lineCounter = 1;
        while ((line = bufferedReader.readLine()) != null) {
            if (expectedIterator.hasNext()) {
                final String expected = expectedIterator.next();
                Assert.assertEquals("Lines differ at line #" + lineCounter + ".", expected, line);
            } else {
                Assert.fail("Log contained more lines than expected.");
            }
        }
        Assert.assertFalse("Log contained less lines than expected.", expectedIterator.hasNext());
    }

    protected abstract AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig);

    @Test public void verifySerializable() throws IOException {
        final ServerConfigurationProvider server = new ServerConfigurationProvider() {
            public String getUrl() {
                return null;
            }

            public String getUserName() {
                return null;
            }

            public String getUserPassword() {
                return null;
            }

            public TaskListener getListener() {
                return null;
            }

            public WebProxySettings getWebProxySettings() {
                final List<Pattern> patterns = Arrays.asList(
                        Pattern.compile(".+\\.com"),
                        Pattern.compile(".+\\.org")
                );
                final Secret secret;
                final SecretOverride secretOverride = new SecretOverride();
                try {
                    secret = Secret.fromString("password");
                }
                finally {
                    try {
                        secretOverride.close();
                    }
                    catch (final IOException ignored) {
                    }
                }
                return new WebProxySettings("localhost", 8080, patterns, "user", secret);
            }
        };
        final AbstractCallableCommand command = createCommand(server);
        final Callable<?, Exception> callable = command.getCallable();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(callable);
    }

}
