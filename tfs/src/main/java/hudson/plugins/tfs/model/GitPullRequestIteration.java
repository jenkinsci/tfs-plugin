//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitCommitRef;

public class GitPullRequestIteration {
    public int id;
    public GitCommitRef sourceRefCommit;
    public GitCommitRef targetRefCommit;
}
