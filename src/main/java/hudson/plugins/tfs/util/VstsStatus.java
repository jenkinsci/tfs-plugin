package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Run;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.VstsCollectionConfiguration;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.VstsGitStatus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;

public class VstsStatus {
    public static void createFromRun(@Nonnull Run<?, ?> run) throws IOException {
        // TODO: also add support for a build triggered from a pull request
        final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
        final GitCodePushedEventArgs gitCodePushedEventArgs;
        if (commitParameter != null) {
            gitCodePushedEventArgs = commitParameter.getGitCodePushedEventArgs();
        }
        else {
            // TODO: try to guess based on what we _do_ have (i.e. RevisionParameterAction)
            return;
        }

        final URI collectionUri = gitCodePushedEventArgs.collectionUri;
        final StandardUsernamePasswordCredentials credentials =
                VstsCollectionConfiguration.findCredentialsForCollection(collectionUri);
        final VstsRestClient client = new VstsRestClient(collectionUri, credentials);

        final VstsGitStatus status = VstsGitStatus.fromRun(run);
        // TODO: when code is pushed and polling happens, are we sure we built against the requested commit?
        client.addCommitStatus(gitCodePushedEventArgs, status);

        // TODO: we could contribute an Action to the run, recording the ID of the status we created
    }
}
