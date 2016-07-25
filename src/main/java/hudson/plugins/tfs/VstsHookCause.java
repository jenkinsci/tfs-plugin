package hudson.plugins.tfs;

import hudson.plugins.git.GitStatus;

public class VstsHookCause extends GitStatus.CommitHookCause {

    public VstsHookCause(final String sha1) {
        super(sha1);
    }

    @Override
    public String getShortDescription() {
        return "Started by TFS/Team Services web hook for commit " + sha1;
    }
}
