package hudson.plugins.tfs.model;

import java.io.Serializable;

import hudson.model.InvisibleAction;
import hudson.plugins.tfs.model.ProjectData;

/**
 * An action for storing TFS configuration data in a build 
 * 
 * @author Erik Ramfelt, redsolo
 */
public class WorkspaceConfiguration extends InvisibleAction implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String workspaceName;
    private final ProjectData projects[];
    private final String serverUrl;
    private boolean workspaceExists;

    public WorkspaceConfiguration(String serverUrl, String workspaceName, ProjectData projects[]) {
        this.workspaceName = workspaceName;
        this.projects = projects;
        this.serverUrl = serverUrl;
        this.workspaceExists = true;
    }

    public WorkspaceConfiguration(WorkspaceConfiguration configuration) {
        this.workspaceName = configuration.workspaceName;
        this.projects = configuration.projects;
        this.serverUrl = configuration.serverUrl;
        this.workspaceExists = configuration.workspaceExists;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public ProjectData[] getProjects() {
    	return projects;
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
        result = prime * result + ((projects == null) ? 0 : projects.hashCode());
        result = prime * result + ((serverUrl == null) ? 0 : serverUrl.hashCode());
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
        if (serverUrl == null) {
            if (other.serverUrl != null)
                return false;
        } else if (!serverUrl.equals(other.serverUrl))
            return false;
        if (workspaceExists != other.workspaceExists)
            return false;
        if (workspaceName == null) {
            if (other.workspaceName != null)
                return false;
        } else if (!workspaceName.equals(other.workspaceName))
            return false;
        if (projects == null) {
        	if (other.projects != null)
        		return false;
        } else {
        	if ((other.projects == null) || (projects.length != other.projects.length))
        		return false;
        		
            for (int ndx = 0; ndx < projects.length; ++ndx) {
            	if (!projects[ndx].getProjectPath().equals(other.projects[ndx].getProjectPath()) ||
           			!projects[ndx].getLocalPath().equals(other.projects[ndx].getLocalPath())) {
        			return false;
            	}
            }        	
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("WorkspaceConfiguration [projectPath=%s, serverUrl=%s, workfolder=%s, workspaceExists=%s, workspaceName=%s]", 
                projects[0].projectPath, serverUrl, projects[0].localPath, workspaceExists, workspaceName);
    }    
}
