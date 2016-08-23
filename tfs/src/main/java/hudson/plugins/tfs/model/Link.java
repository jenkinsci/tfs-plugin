package hudson.plugins.tfs.model;

/**
 * Workaround for missing Link model class in current version of vso-httpclient-java
 */
public class Link {
    public String rel;
    public String url;

    public Link() {

    }

    public Link(final String rel, final String url) {
        this.rel = rel;
        this.url = url;
    }

}
