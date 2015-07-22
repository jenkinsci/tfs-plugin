package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.model.Server;

import java.util.concurrent.Callable;

public abstract class AbstractCallableCommand {

    private final Server server;

    protected AbstractCallableCommand(final Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public abstract <T> Callable<T> getCallable();
}
