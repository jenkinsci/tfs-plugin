package hudson.plugins.tfs;

import hudson.plugins.git.RevisionParameterAction;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;

/**
 * Used as a build parameter to record information about the associated project and
 * Visual Studio Team Services account or TFS server to facilitate integration.
 */
public class CommitParameterAction extends RevisionParameterAction {

    private final GitCodePushedEventArgs gitCodePushedEventArgs;

    public CommitParameterAction(final GitCodePushedEventArgs e) {
        super(e.commit, e.getRepoURIish());

        this.gitCodePushedEventArgs = e;
    }

    public GitCodePushedEventArgs getGitCodePushedEventArgs() {
        return gitCodePushedEventArgs;
    }
}
