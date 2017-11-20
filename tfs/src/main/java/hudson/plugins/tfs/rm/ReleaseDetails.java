//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.List;
import java.util.Map;

public class ReleaseDetails {
    private Integer id;
    private String name;
    private String status;
    private String createdOn;
    private String modifiedOn;
    private Map<String, Object> modifiedBy;
    private Map<String, Object> createdBy;
    private List<Map<String, Object>> environments;
    private Map<String, Object> variables;
    private List<Object> variableGroups;
    private List<Object> artifacts;
    private Map<String, Object> releaseDefinition;
    private String description;
    private String reason;
    private String releaseNameFormat;
    private Boolean keepForever;
    private Integer definitionSnapshotRevision;
    private String logsContainerUrl;
    private String url;
    private Map<String, Object> _links;
    private List<Object> tags;
    private Map<String, Object> projectReference;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(String modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Map<String, Object> getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Map<String, Object> modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Map<String, Object> getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Map<String, Object> createdBy) {
        this.createdBy = createdBy;
    }

    public List<Map<String, Object>> getEnvironments() {
        return environments;
    }

    public void setEnvironments(List<Map<String, Object>> environments) {
        this.environments = environments;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public List<Object> getVariableGroups() {
        return variableGroups;
    }

    public void setVariableGroups(List<Object> variableGroups) {
        this.variableGroups = variableGroups;
    }

    public List<Object> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Object> artifacts) {
        this.artifacts = artifacts;
    }

    public Map<String, Object> getReleaseDefinition() {
        return releaseDefinition;
    }

    public void setReleaseDefinition(Map<String, Object> releaseDefinition) {
        this.releaseDefinition = releaseDefinition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReleaseNameFormat() {
        return releaseNameFormat;
    }

    public void setReleaseNameFormat(String releaseNameFormat) {
        this.releaseNameFormat = releaseNameFormat;
    }

    public Boolean getKeepForever() {
        return keepForever;
    }

    public void setKeepForever(Boolean keepForever) {
        this.keepForever = keepForever;
    }

    public Integer getDefinitionSnapshotRevision() {
        return definitionSnapshotRevision;
    }

    public void setDefinitionSnapshotRevision(Integer definitionSnapshotRevision) {
        this.definitionSnapshotRevision = definitionSnapshotRevision;
    }

    public String getLogsContainerUrl() {
        return logsContainerUrl;
    }

    public void setLogsContainerUrl(String logsContainerUrl) {
        this.logsContainerUrl = logsContainerUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> get_links() {
        return _links;
    }

    public void set_links(Map<String, Object> _links) {
        this._links = _links;
    }

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getProjectReference() {
        return projectReference;
    }

    public void setProjectReference(Map<String, Object> projectReference) {
        this.projectReference = projectReference;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    private Map<String, Object> properties;

}
