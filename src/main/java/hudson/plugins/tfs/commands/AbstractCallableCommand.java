package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.Workstation;
import com.microsoft.tfs.core.config.persistence.PersistenceStoreProvider;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.ExtraSettings;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.WebProxySettings;
import hudson.remoting.Callable;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractCallableCommand implements Serializable {

    private final String url;
    private final String userName;
    private final String userPassword;
    private final TaskListener listener;
    private final WebProxySettings webProxySettings;
    private final ExtraSettings extraSettings;

    protected AbstractCallableCommand(final ServerConfigurationProvider serverConfig) {
        url = serverConfig.getUrl();
        userName = serverConfig.getUserName();
        userPassword = serverConfig.getUserPassword();
        listener = serverConfig.getListener();
        webProxySettings = serverConfig.getWebProxySettings();
        extraSettings = serverConfig.getExtraSettings();
    }

    static void updateCache(final TFSTeamProjectCollection connection) {
        final PersistenceStoreProvider persistenceStoreProvider = connection.getPersistenceStoreProvider();
        final Workstation workstation = Workstation.getCurrent(persistenceStoreProvider);
        final VersionControlClient vcc = connection.getVersionControlClient();
        workstation.updateWorkspaceInfoCache(vcc, VersionControlConstants.AUTHENTICATED_USER);
    }

    public Server createServer() throws IOException {
        final Server server = new Server(null, listener, url, userName, userPassword, webProxySettings, extraSettings);
        return server;
    }

    public abstract <V, E extends Throwable> Callable<V, E> getCallable();
}
