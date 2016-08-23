package hudson.plugins.tfs.rm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ankit Goyal
 */

public class Definition
{

    private String id;
    private String name;
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

}
