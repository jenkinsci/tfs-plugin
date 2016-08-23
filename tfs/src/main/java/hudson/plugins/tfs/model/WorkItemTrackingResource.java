package hudson.plugins.tfs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.visualstudio.services.webapi.model.ReferenceLinks;

/**
 * Workaround for missing classes in vso-httpclient-java
 */
public class WorkItemTrackingResource
    extends WorkItemTrackingResourceReference {

    private ReferenceLinks _links;

    @JsonProperty("_links")
    public ReferenceLinks getLinks() {
        return _links;
    }

    @JsonProperty("_links")
    public void setLinks(final ReferenceLinks _links) {
        this._links = _links;
    }
}
