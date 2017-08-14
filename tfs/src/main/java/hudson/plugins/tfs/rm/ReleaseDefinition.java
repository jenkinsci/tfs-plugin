//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ankit Goyal
 */

public class ReleaseDefinition
{
    private Integer id;
    private Integer revision;
    private String name;
    private CreatedBy createdBy;
    private String createdOn;
    private ModifiedBy modifiedBy;
    private String modifiedOn;
    private List<Artifact> artifacts = new ArrayList<Artifact>();
    private String releaseNameFormat;
    private RetentionPolicy retentionPolicy;
    private String url;
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
    * 
    * @return
    * The id
    */
    public Integer getId()
    {
        return id;
    }

    /**
    * 
    * @param id
    * The id
    */
    public void setId(Integer id)
    {
        this.id = id;
    }

    /**
    * 
    * @return
    * The revision
    */
    public Integer getRevision()
    {
        return revision;
    }

    /**
    * 
    * @param revision
    * The revision
    */
    public void setRevision(Integer revision)
    {
        this.revision = revision;
    }

    /**
    * 
    * @return
    * The name
    */
    public String getName()
    {
        return name;
    }

    /**
    * 
    * @param name
    * The name
    */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
    * 
    * @return
    * The createdBy
    */
    public CreatedBy getCreatedBy()
    {
        return createdBy;
    }

    /**
    * 
    * @param createdBy
    * The createdBy
    */
    public void setCreatedBy(CreatedBy createdBy)
    {
        this.createdBy = createdBy;
    }

    /**
    * 
    * @return
    * The createdOn
    */
    public String getCreatedOn()
    {
        return createdOn;
    }

    /**
    * 
    * @param createdOn
    * The createdOn
    */
    public void setCreatedOn(String createdOn)
    {
        this.createdOn = createdOn;
    }

    /**
    * 
    * @return
    * The modifiedBy
    */
    public ModifiedBy getModifiedBy()
    {
        return modifiedBy;
    }

    /**
    * 
    * @param modifiedBy
    * The modifiedBy
    */
    public void setModifiedBy(ModifiedBy modifiedBy)
    {
        this.modifiedBy = modifiedBy;
    }

    /**
    * 
    * @return
    * The modifiedOn
    */
    public String getModifiedOn() 
    {
        return modifiedOn;
    }

    /**
    * 
    * @param modifiedOn
    * The modifiedOn
    */
    public void setModifiedOn(String modifiedOn)
    {
        this.modifiedOn = modifiedOn;
    }

    /**
    * 
    * @return
    * The artifacts
    */
    public List<Artifact> getArtifacts()
    {
        return artifacts;
    }

    /**
    * 
    * @param artifacts
    * The artifacts
    */
    public void setArtifacts(List<Artifact> artifacts)
    {
        this.artifacts = artifacts;
    }

    /**
    * 
    * @return
    * The releaseNameFormat
    */
    public String getReleaseNameFormat()
    {
        return releaseNameFormat;
    }

    /**
    * 
    * @param releaseNameFormat
    * The releaseNameFormat
    */
    public void setReleaseNameFormat(String releaseNameFormat)
    {
        this.releaseNameFormat = releaseNameFormat;
    }

    /**
    * 
    * @return
    * The retentionPolicy
    */
    public RetentionPolicy getRetentionPolicy()
    {
        return retentionPolicy;
    }

    /**
    * 
    * @param retentionPolicy
    * The retentionPolicy
    */
    public void setRetentionPolicy(RetentionPolicy retentionPolicy)
    {
        this.retentionPolicy = retentionPolicy;
    }

    public String getUrl()
    {
        return url;
    }


    public void setUrl(String url)
    {
        this.url = url;
    }

    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

}
