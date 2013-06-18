package hudson.plugins.tfs;

import hudson.scm.SCMRevisionState;

public class TFSRevisionState extends SCMRevisionState {

    final int changesetVersion;
    final String projectPath; 

    public TFSRevisionState(String changesetVersion, String projectPath) {
        this.changesetVersion = Integer.parseInt(changesetVersion, 10);
        this.projectPath = projectPath;
    }
    
    public TFSRevisionState(int changesetVersion, String projectPath) {
        this.changesetVersion = changesetVersion;
        this.projectPath = projectPath;
    }
}
