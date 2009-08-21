package hudson.plugins.tfs.model;

/**
 * Mapping between a working folder and a project path that exists in a workspace.
 */
public class WorkspaceMapping {        

    private final String projectPath;
    private final String localPath;

    public WorkspaceMapping(String projectPath, String localPath) {
        this.projectPath = projectPath;
        this.localPath = localPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getLocalPath() {
        return localPath;
    }
}