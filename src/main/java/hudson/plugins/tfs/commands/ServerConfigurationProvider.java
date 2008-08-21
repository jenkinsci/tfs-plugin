package hudson.plugins.tfs.commands;

import java.io.IOException;

public interface ServerConfigurationProvider {

    public String getUrl();

    public String getUserName();

    public String getUserPassword();
    
    public String getLocalHostname() throws IOException, InterruptedException;
}
