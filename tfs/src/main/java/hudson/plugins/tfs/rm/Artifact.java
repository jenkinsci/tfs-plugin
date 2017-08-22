//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ankit Goyal
 */

public class Artifact
{

    private Integer id;
    private String type;
    private String alias;
    private DefinitionReference definitionReference;
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
    * The type
    */
    public String getType()
    {
        return type;
    }

    /**
    * 
    * @param type
    * The type
    */
    public void setType(String type)
    {
        this.type = type;
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
    * The definitionReference
    */
    public DefinitionReference getDefinitionReference()
    {
        return definitionReference;
    }

    /**
    * 
    * @param definitionReference
    * The definitionReference
    */
    public void setDefinitionReference(DefinitionReference definitionReference)
    {
        this.definitionReference = definitionReference;
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
