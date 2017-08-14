//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author angoya
 */
public class ArtifactVersion 
{
    private Object sourceId;
    private String alias;
    private List<Version> versions = new ArrayList<Version>();
    private Object errorMessage;

    /**
    * 
    * @return
    * The sourceId
    */
    public Object getSourceId()
    {
        return sourceId;
    }

    /**
    * 
    * @param sourceId
    * The sourceId
    */
    public void setSourceId(Object sourceId)
    {
        this.sourceId = sourceId;
    }

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
    * The versions
    */
    public List<Version> getVersions()
    {
        return versions;
    }

    /**
    * 
    * @param versions
    * The versions
    */
    public void setVersions(List<Version> versions)
    {
        this.versions = versions;
    }

    /**
    * 
    * @return
    * The errorMessage
    */
    public Object getErrorMessage()
    {
        return errorMessage;
    }

    /**
    * 
    * @param errorMessage
    * The errorMessage
    */
    public void setErrorMessage(Object errorMessage)
    {
        this.errorMessage = errorMessage;
    }
}
