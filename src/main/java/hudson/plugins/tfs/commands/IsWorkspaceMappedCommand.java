package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.Workstation;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.config.persistence.PersistenceStoreProvider;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;

import java.io.PrintStream;

public class IsWorkspaceMappedCommand extends AbstractCallableCommand implements Callable<Boolean, Exception> {

    private static final String CheckingMappingTemplate = "Checking if there exists a mapping for %s...";

    private final String localPath;

    public IsWorkspaceMappedCommand(final ServerConfigurationProvider serverConfig, final String localPath) {
        super(serverConfig);
        this.localPath = localPath;
    }

    @Override
    public Callable<Boolean, Exception> getCallable() {
        return this;
    }

    @Override
    public Boolean call() throws Exception {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TFSTeamProjectCollection connection = vcc.getConnection();
        updateCache(connection);
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();
        final String listWorkspacesMessage = String.format(CheckingMappingTemplate, localPath);
        logger.print(listWorkspacesMessage);

        final Workspace workspace = vcc.tryGetWorkspace(localPath);
        final boolean existsMapping = workspace != null;

        logger.println(existsMapping ? "yes" : "no");

        return existsMapping;
    }

    static void updateCache(final TFSTeamProjectCollection connection) {
        final PersistenceStoreProvider persistenceStoreProvider = connection.getPersistenceStoreProvider();
        final Workstation workstation = Workstation.getCurrent(persistenceStoreProvider);
        final VersionControlClient vcc = connection.getVersionControlClient();
        workstation.updateWorkspaceInfoCache(vcc, VersionControlConstants.AUTHENTICATED_USER);
    }
}
