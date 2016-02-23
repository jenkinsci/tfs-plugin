package hudson.plugins.tfs.commands;

import hudson.model.TaskListener;
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

    protected AbstractCallableCommand(final ServerConfigurationProvider serverConfig) {
        url = serverConfig.getUrl();
        userName = serverConfig.getUserName();
        userPassword = serverConfig.getUserPassword();
        listener = serverConfig.getListener();
        webProxySettings = serverConfig.getWebProxySettings();
    }

    public Server createServer() throws IOException {
        final Server server = new Server(null, listener, url, userName, userPassword, webProxySettings);
        return server;
    }

    public abstract <V, E extends Throwable> Callable<V, E> getCallable();
}
