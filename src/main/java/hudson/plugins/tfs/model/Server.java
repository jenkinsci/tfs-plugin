package hudson.plugins.tfs.model;

import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.commands.ServerConfigurationProvider;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Server implements ServerConfigurationProvider {
    
    private final String url;
    private final String userName;
    private final String userPassword;
    private Workspaces workspaces;
    private Map<String, Project> projects = new HashMap<String, Project>();
    private final TfTool tool;

    public Server(TfTool tool, String url, String username, String password) {
        this.tool = tool;
        this.url = url;
        this.userName = username;
        this.userPassword = password;
    }

    Server(String url) {
        this(null, url, null, null);
    }

    public Project getProject(String projectPath) {
        if (! projects.containsKey(projectPath)) {
            projects.put(projectPath, new Project(this, projectPath));
        }
        return projects.get(projectPath);
    }
    
    public Workspaces getWorkspaces() {
        if (workspaces == null) {
            workspaces = new Workspaces(this);
        }
        return workspaces;
    }
    
    public Reader execute(MaskedArgumentListBuilder arguments) throws IOException, InterruptedException {
        return tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
