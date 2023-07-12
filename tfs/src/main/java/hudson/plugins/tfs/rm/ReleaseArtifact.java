//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author angoya
 */
public class ReleaseArtifact 
{

    @SerializedName("alias")
    private String alias;
    @SerializedName("instanceReference")
    private InstanceReference instanceReference;

    /**
    * 
    * @return
    * The alias
    */
    public String getAlias()
    {
        return alias;
    }

    /**
    * 
    * @param alias
    * The alias
    */
    public void setAlias(String alias)
    {
        this.alias = alias;
    }

    /**
    * 
    * @return
    * The instanceReference
    */
    public InstanceReference getInstanceReference()
    {
        return instanceReference;
    }

    /**
    * 
    * @param instanceReference
    * The instanceReference
    */
    public void setInstanceReference(InstanceReference instanceReference)
    {
        this.instanceReference = instanceReference;
    }
}
