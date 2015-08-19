package hudson.plugins.tfs.commands;

import hudson.model.TaskListener;
import hudson.plugins.tfs.model.Server;

import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class AbstractCallableCommand {

    private final ServerConfigurationProvider serverConfig;

    protected AbstractCallableCommand(final ServerConfigurationProvider server) {
        this.serverConfig = server;
    }

    public Server createServer() throws IOException {
        final String url = serverConfig.getUrl();
        final String userName = serverConfig.getUserName();
        final String userPassword = serverConfig.getUserPassword();
        final TaskListener listener = serverConfig.getListener();
        final Server server = new Server(null, listener, url, userName, userPassword);
        return server;
    }

    public abstract <T> Callable<T> getCallable();
}
