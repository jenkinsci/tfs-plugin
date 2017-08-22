//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author angoya
 */
public class InstanceReference 
{
    @SerializedName("name")
    private String name;
    @SerializedName("id")
    private String id;
    @SerializedName("sourceBranch")
    private String sourceBranch;

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
    * The id
    */
    public String getId()
    {
        return id;
    }

    /**
    * 
    * @param id
    * The id
    */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
    * 
    * @return
    * The sourceBranch
    */
    public String getSourceBranch()
    {
        return sourceBranch;
    }

    /**
    * 
    * @param sourceBranch
    * The sourceBranch
    */
    public void setSourceBranch(String sourceBranch)
    {
        this.sourceBranch = sourceBranch;
    }
}
