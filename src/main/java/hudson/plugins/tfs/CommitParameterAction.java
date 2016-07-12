package hudson.plugins.tfs;

import hudson.plugins.git.RevisionParameterAction;

/**
 * Used as a build parameter to record information about the associated project and
 * Visual Studio Team Services account or TFS server to facilitate integration.
 */
public class CommitParameterAction extends RevisionParameterAction {

    public CommitParameterAction(final String commit) {
        super(commit);
    }

}
