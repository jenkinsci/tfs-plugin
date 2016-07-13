package hudson.plugins.tfs.model;

import java.net.URI;

public class Link {
    public URI href;

    @Override
    public String toString() {
        return href.toString();
    }
}
