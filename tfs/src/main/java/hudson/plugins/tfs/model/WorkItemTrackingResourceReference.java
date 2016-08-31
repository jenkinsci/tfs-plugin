package hudson.plugins.tfs.model;

/**
 * Workaround for missing classes in vso-httpclient-java
 */
public class WorkItemTrackingResourceReference {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
