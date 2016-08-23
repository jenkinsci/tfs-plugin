package hudson.plugins.tfs.model;

import java.net.URI;
import java.util.Map;

public class WorkItem {
    public long id;
    public int rev;
    public Map<String, Object> fields;
    public Map<String, Link> _links;
    public URI url;
}
