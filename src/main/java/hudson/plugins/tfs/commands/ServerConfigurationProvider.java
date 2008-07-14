package hudson.plugins.tfs.commands;

public interface ServerConfigurationProvider {

    public String getUrl();

    public String getUserName();

    public String getUserPassword();
}
