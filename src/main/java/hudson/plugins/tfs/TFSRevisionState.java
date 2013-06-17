package hudson.plugins.tfs;

import hudson.scm.SCMRevisionState;

public class TFSRevisionState extends SCMRevisionState {

    final String changesetVersion;
    
    public TFSRevisionState(String changesetVersion) {
        this.changesetVersion = changesetVersion;
    }
}
