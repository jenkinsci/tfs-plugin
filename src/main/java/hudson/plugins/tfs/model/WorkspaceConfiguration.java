package hudson.plugins.tfs.model;

import java.io.Serializable;
import java.util.Collection;

import hudson.model.InvisibleAction;

/**
 * An action for storing TFS configuration data in a build 
 * 
 * @author Erik Ramfelt, redsolo
 */
public class WorkspaceConfiguration extends InvisibleAction implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String workspaceName;
    private final String workfolder;
    private final String projectPath;
    private Collection<String> cloakPaths;
    private final String serverUrl;
    private boolean workspaceExists;

    public WorkspaceConfiguration(String serverUrl, String workspaceName, String projectPath, Collection<String> cloakPaths, String workfolder) {
        this.workspaceName = workspaceName;
        this.workfolder = workfolder;
        this.projectPath = projectPath;
        this.cloakPaths = cloakPaths;
        this.serverUrl = serverUrl;
        this.workspaceExists = true;
    }

    public WorkspaceConfiguration(WorkspaceConfiguration configuration) {
        this.workspaceName = configuration.workspaceName;
        this.workfolder = configuration.workfolder;
        this.projectPath = configuration.projectPath;
        this.cloakPaths = configuration.cloakPaths;
        this.serverUrl = configuration.serverUrl;
        this.workspaceExists = configuration.workspaceExists;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public String getWorkfolder() {
        return workfolder;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public Collection<String> getCloakPaths() {
        return cloakPaths;
    }
    public String getServerUrl() {
        return serverUrl;
    }
    
    public boolean workspaceExists() {
        return workspaceExists;
    }
    
    public void setWorkspaceWasRemoved() {
        this.workspaceExists = false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((projectPath == null) ? 0 : projectPath.hashCode());
        result = prime * result + ((cloakPaths == null) ? 0 : cloakPaths.hashCode());
        result = prime * result + ((serverUrl == null) ? 0 : serverUrl.hashCode());
        result = prime * result + ((workfolder == null) ? 0 : workfolder.hashCode());
        result = prime * result + (workspaceExists ? 1231 : 1237);
        result = prime * result + ((workspaceName == null) ? 0 : workspaceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof WorkspaceConfiguration))
            return false;
        WorkspaceConfiguration other = (WorkspaceConfiguration) obj;
        if (projectPath == null) {
            if (other.projectPath != null)
                return false;
        } else if (!projectPath.equals(other.projectPath))
            return false;
        if (cloakPaths == null) {
        	if (other.cloakPaths != null)
        		return false;
        } else if (other.cloakPaths == null)
        	return false;
        else if (cloakPaths.size() != other.cloakPaths.size())
        	return false;
        else if (!cloakPaths.containsAll(other.cloakPaths))
        	return false;
        if (serverUrl == null) {
            if (other.serverUrl != null)
                return false;
        } else if (!serverUrl.equals(other.serverUrl))
            return false;
        if (workfolder == null) {
            if (other.workfolder != null)
                return false;
        } else if (!workfolder.equals(other.workfolder))
            return false;
        if (workspaceExists != other.workspaceExists)
            return false;
        if (workspaceName == null) {
            if (other.workspaceName != null)
                return false;
        } else if (!workspaceName.equals(other.workspaceName))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("WorkspaceConfiguration [projectPath=%s, serverUrl=%s, workfolder=%s, workspaceExists=%s, workspaceName=%s]", 
                projectPath, serverUrl, workfolder, workspaceExists, workspaceName);
    }    
}
