package hudson.plugins.tfs.commands;

import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import org.apache.commons.collections.IteratorUtils;
import org.junit.Assert;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;

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
}
