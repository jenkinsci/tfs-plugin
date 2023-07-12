//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Workaround for missing Link model class in current version of vso-httpclient-java
 */
@SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Used by JsonPathOperation")
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
