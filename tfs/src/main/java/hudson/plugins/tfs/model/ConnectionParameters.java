package hudson.plugins.tfs.model;

/**
 * These parameters are stored and modified from outside Jenkins through the TeamEventsEndpoint "connect" event.
 * An instance of this class is owned by every TeamCollectionConfiguration instance.
 */
public class ConnectionParameters {
    private String teamCollectionUrl;
    private String connectionKey;
    private String connectionSignature;
    private boolean sendJobCompletionEvents;

    /**
     * Constructor.
     */
    public ConnectionParameters() {
    }

    public String getConnectionKey() {
        return connectionKey;
    }

    public void setConnectionKey(final String connectionKey) {
        this.connectionKey = connectionKey;
    }

    public String getConnectionSignature() {
        return connectionSignature;
    }

    public void setConnectionSignature(final String connectionSignature) {
        this.connectionSignature = connectionSignature;
    }

    public String getTeamCollectionUrl() {
        return teamCollectionUrl;
    }

    public void setTeamCollectionUrl(final String teamCollectionUrl) {
        this.teamCollectionUrl = teamCollectionUrl;
    }

    public boolean isSendJobCompletionEvents() {
        return sendJobCompletionEvents;
    }

    public void setSendJobCompletionEvents(final boolean sendJobCompletionEvents) {
        this.sendJobCompletionEvents = sendJobCompletionEvents;
    }
}
