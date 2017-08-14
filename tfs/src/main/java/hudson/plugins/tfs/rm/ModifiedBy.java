//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ankit Goyal
 */
public class ModifiedBy {

    private String id;
    private String displayName;
    private String uniqueName;
    private String url;
    private String imageUrl;
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName The displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     *
     * @return The uniqueName
     */
    public String getUniqueName() {
        return uniqueName;
    }

    /**
     *
     * @param uniqueName The uniqueName
     */
    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    /**
     *
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return The imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @param imageUrl The imageUrl
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
