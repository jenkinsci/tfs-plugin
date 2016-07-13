package hudson.plugins.tfs;

/**
 * Represent the different types of notifications that VSTS can POST to Jenkins.
 */
public enum VstsHookEventName {
    /**
     * The PING event is raised when testing the connection from VSTS to Jenkins.
     */
    PING,
    /**
     * The BUILD_COMPLETED event is raised when a VSTS build completes.
     */
    BUILD_COMPLETED,
    /**
     * The GIT_CODE_PUSHED event is raised when a user pushes to a Git repository.
     */
    GIT_CODE_PUSHED,
    /**
     * The TFVC_CODE_CHECKED_IN event is raised when a user checks in to a TFVC repository.
     */
    TFVC_CODE_CHECKED_IN,
    /**
     * The PULL_REQUEST_MERGE_COMMIT_CREATED event is raised when the merge commit
     * for a pull request has been successfully created, usually as a result of a
     * push of the corresponding branch.
     */
    PULL_REQUEST_MERGE_COMMIT_CREATED,
    // TODO: clarify the following events
    DEPLOYMENT_COMPLETED,
    RELEASE_CREATED,
}
