package hudson.plugins.tfs.rm;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author angoya
 */
public class ReleaseBody
{
    
    @SerializedName("definitionId")
    private Integer definitionId;
    @SerializedName("description")
    private String description;
    @SerializedName("artifacts")
    private List<ReleaseArtifact> artifacts = new ArrayList<ReleaseArtifact>();
    @SerializedName("isDraft")
    private Boolean isDraft;
    @SerializedName("manualEnvironments")
    private List<Object> manualEnvironments = new ArrayList<Object>();

    /**
    * 
    * @return
    * The definitionId
    */
    public Integer getDefinitionId()
    {
        return definitionId;
    }

    /**
    * 
    * @param definitionId
    * The definitionId
    */
    public void setDefinitionId(Integer definitionId)
    {
        this.definitionId = definitionId;
    }

    /**
    * 
    * @return
    * The description
    */
    public String getDescription()
    {
        return description;
    }

    /**
    * 
    * @param description
    * The description
    */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
    * 
    * @return
    * The artifacts
    */
    public List<ReleaseArtifact> getArtifacts()
    {
        return artifacts;
    }

    /**
    * 
    * @param artifacts
    * The artifacts
    */
    public void setArtifacts(List<ReleaseArtifact> artifacts)
    {
        this.artifacts = artifacts;
    }

    /**
    * 
    * @return
    * The isDraft
    */
    public Boolean getIsDraft()
    {
        return isDraft;
    }

    /**
    * 
    * @param isDraft
    * The isDraft
    */
    public void setIsDraft(Boolean isDraft)
    {
        this.isDraft = isDraft;
    }

    /**
    * 
    * @return
    * The manualEnvironments
    */
    public List<Object> getManualEnvironments()
    {
        return manualEnvironments;
    }

    /**
    * 
    * @param manualEnvironments
    * The manualEnvironments
    */
    public void setManualEnvironments(List<Object> manualEnvironments)
    {
        this.manualEnvironments = manualEnvironments;
    }
}
