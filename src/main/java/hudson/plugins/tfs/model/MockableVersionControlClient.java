package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.util.Closable;

/**
 * A non-final wrapper over {@link com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient}
 */
public class MockableVersionControlClient implements Closable {

    private final VersionControlClient vcc;

    public MockableVersionControlClient(final VersionControlClient vcc) {
        this.vcc = vcc;
    }

    public void close() {
        vcc.close();
    }
}
