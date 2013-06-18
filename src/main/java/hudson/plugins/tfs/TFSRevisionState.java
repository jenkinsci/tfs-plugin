package hudson.plugins.tfs;

import hudson.scm.SCMRevisionState;

public class TFSRevisionState extends SCMRevisionState {

    final int changesetVersion;

    public TFSRevisionState(String changesetVersion) {
        this.changesetVersion = Integer.parseInt(changesetVersion, 10);
    }
    
    public TFSRevisionState(int changesetVersion) {
        this.changesetVersion = changesetVersion;
    }
}
