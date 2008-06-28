package hudson.plugins.tfs.model;

public class TeamFoundationProject {

    private final String project;
    private final String server;
    private TeamFoundationCredentials credentials;

    public TeamFoundationProject(String server, String project) {
        this.server = server;
        this.project = project;
    }

    public String getProject() {
        return project;
    }

    public String getServer() {
        return server;
    }

    public TeamFoundationCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(TeamFoundationCredentials credentials) {
        this.credentials = credentials;        
    }

}
