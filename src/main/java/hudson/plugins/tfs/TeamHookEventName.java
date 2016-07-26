package hudson.plugins.tfs;

import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;

/**
 * Represent the different types of notifications that TFS/Team Services can POST to Jenkins.
 */
public enum TeamHookEventName {
    /**
     * The PING event is raised when testing the connection from TFS/Team Services to Jenkins.
     */
    PING {
        @Override public Object parse(final String body) {
            return body;
        }
    },
    /**
     * The BUILD_COMPLETED event is raised when a TFS/Team Services build completes.
     */
    BUILD_COMPLETED,
    /**
     * The GIT_CODE_PUSHED event is raised when a user pushes to a Git repository.
     */
    GIT_CODE_PUSHED {
        @Override public Object parse(final String body) {
            return GitCodePushedEventArgs.fromJsonString(body);
        }
    },
    /**
     * The TFVC_CODE_CHECKED_IN event is raised when a user checks in to a TFVC repository.
     */
    TFVC_CODE_CHECKED_IN,
    /**
     * The PULL_REQUEST_MERGE_COMMIT_CREATED event is raised when the merge commit
     * for a pull request has been successfully created, usually as a result of a
     * push of the corresponding branch.
     */
    PULL_REQUEST_MERGE_COMMIT_CREATED {
        @Override public Object parse(final String body) {
            return PullRequestMergeCommitCreatedEventArgs.fromJsonString(body);
        }
    },
    /**
     * The DEPLOYMENT_COMPLETED event is raised when one of the release's deployments have completed.
     */
    DEPLOYMENT_COMPLETED,
    ;

    public Object parse(final String body) {
        return new IllegalStateException("Not implemented");
    }
}
