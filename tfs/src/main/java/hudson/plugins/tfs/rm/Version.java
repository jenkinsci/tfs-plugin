//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

/**
 *
 * @author angoya
 */
public class Version
{
    
    private String id;
    private String name;
    private String sourceBranch;

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
