package hudson.plugins.tfs.model.servicehooks;

public enum EventScope {
    /**
     * No input scope specified.
     */
    All,

    /**
     * Team Project scope.
     */
    Project,

    /**
     * Team scope.
     */
    Team,

    /**
     * Collection scope.
     */
    Collection,

    /**
     * Account scope.
     */
    Account,

    /**
     * Deployment scope.
     */
    Deployment,
}
