package hudson.plugins.tfs.model.servicehooks;

import java.util.UUID;

/**
 * The base class for all resource containers, i.e. Account, Collection, Project
 */
public class ResourceContainer {
    private UUID id;
    private String baseUrl;
    private String url;
    private String name;

    public ResourceContainer() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
