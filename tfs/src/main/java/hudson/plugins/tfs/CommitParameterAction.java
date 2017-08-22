package hudson.plugins.tfs;

import hudson.plugins.git.RevisionParameterAction;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.util.UriHelper;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.net.URI;

/**
 * Used as a build parameter to record information about the associated project and
 * Visual Studio Team Services account or TFS server to facilitate integration.
 */
public class CommitParameterAction extends RevisionParameterAction {

    private final GitCodePushedEventArgs gitCodePushedEventArgs;

    /**
     * Saves the repo uri and GitCodePushedEventArgs.
     */
    public CommitParameterAction(final GitCodePushedEventArgs e) {
        super(e.commit, e.getRepoURIish());

        this.gitCodePushedEventArgs = e;
    }

    /**
     * Returns the GitCodePushedEventArgs.
     */
    public GitCodePushedEventArgs getGitCodePushedEventArgs() {
        return gitCodePushedEventArgs;
    }

    /**
     * Returns true if the git repo uri can originate from any of the remotes passed in.
     */
    @Override
    public boolean canOriginateFrom(final Iterable<RemoteConfig> remotes) {
        final URI repoUri = gitCodePushedEventArgs.repoUri;

        for (final RemoteConfig remote : remotes) {
            for (final URIish remoteURL : remote.getURIs()) {
                final URI remoteUri = URI.create(remoteURL.toString());
                if (UriHelper.areSameGitRepo(remoteUri, repoUri)) {
                    return true;
                }
            }
        }
        return false;
    }
}
