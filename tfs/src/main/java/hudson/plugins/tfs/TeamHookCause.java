package hudson.plugins.tfs;

import hudson.plugins.git.GitStatus;

/**
 * Attached to the build if it was started by a TFS/Team Services commit.
 */
public class TeamHookCause extends GitStatus.CommitHookCause {

    public TeamHookCause(final String sha1) {
        super(sha1);
    }

    @Override
    public String getShortDescription() {
        return "Started by TFS/Team Services web hook for commit " + sha1;
    }
}
