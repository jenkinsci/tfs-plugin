package hudson.plugins.tfs.commands;

import hudson.model.TaskListener;
import hudson.plugins.tfs.model.ExtraSettings;
import hudson.plugins.tfs.model.WebProxySettings;

public interface ServerConfigurationProvider {

    public String getUrl();

    public String getUserName();

    public String getUserPassword();

    public TaskListener getListener();

    public WebProxySettings getWebProxySettings();

    public ExtraSettings getExtraSettings();
}
