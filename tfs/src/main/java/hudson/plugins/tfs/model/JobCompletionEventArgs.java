package hudson.plugins.tfs.model;

/**
 * This class holds the information needed to send the Job Completion event.
 */
public class JobCompletionEventArgs {
    private final String serverKey;
    private final String payload;
    private final String payloadSignature;

    /**
     * Construtor.
     * @param serverKey
     * @param payload
     * @param payloadSignature
     */
    public JobCompletionEventArgs(final String serverKey, final String payload, final String payloadSignature) {
        this.serverKey = serverKey;
        this.payload = payload;
        this.payloadSignature = payloadSignature;
    }

    public String getServerKey() {
        return serverKey;
    }

    public String getPayload() {
        return payload;
    }

    public String getPayloadSignature() {
        return payloadSignature;
    }
}
