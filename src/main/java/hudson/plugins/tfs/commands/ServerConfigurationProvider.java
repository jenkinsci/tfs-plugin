package hudson.plugins.tfs.commands;

import hudson.model.TaskListener;

public interface ServerConfigurationProvider {

    public String getUrl();

    public String getUserName();

    public String getUserPassword();

    public TaskListener getListener();
}
