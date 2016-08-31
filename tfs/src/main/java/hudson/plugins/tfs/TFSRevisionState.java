package hudson.plugins.tfs;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.scm.SCMRevisionState;

@ExportedBean
public class TFSRevisionState extends SCMRevisionState {

    @Exported(visibility=2)
    public final int changesetVersion;
    @Exported(visibility=1)
    public final String projectPath;

    public TFSRevisionState(String changesetVersion, String projectPath) {
        this.changesetVersion = Integer.parseInt(changesetVersion, 10);
        this.projectPath = projectPath;
    }
    
    public TFSRevisionState(int changesetVersion, String projectPath) {
        this.changesetVersion = changesetVersion;
        this.projectPath = projectPath;
    }
}
