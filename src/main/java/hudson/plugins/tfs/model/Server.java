package hudson.plugins.tfs.model;

import hudson.Util;
import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Server {
    
    private final String url;
    private final String username;
    private final String password;
    private Workspaces workspaces;
    private Map<String, Project> projects = new HashMap<String, Project>();
    private final TfTool tool;

    public Server(TfTool tool, String url, String username, String password) {
        this.tool = tool;
        this.url = url;
        this.username = username;
        this.password = password;
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
        arguments.add(String.format("/server:%s", url));
        if ((Util.fixEmpty(username) != null) && (password != null)) {
            arguments.addMasked(String.format("/login:%s,%s", username, password));
        }
        return tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
    }

    public String getUrl() {
        return url;
    }
}
