package hudson.plugins.tfs.rm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ankit Goyal
 */

public class DefinitionReference
{

    private Definition definition;
    private Project project;
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
    * 
    * @return
    * The definition
    */
    public Definition getDefinition()
    {
        return definition;
    }

    /**
    * 
    * @param definition
    * The definition
    */
    public void setDefinition(Definition definition)
    {
        this.definition = definition;
    }

    /**
    * 
    * @return
    * The project
    */
    public Project getProject()
    {
        return project;
    }

    /**
    * 
    * @param project
    * The project
    */
    public void setProject(Project project)
    {
        this.project = project;
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
