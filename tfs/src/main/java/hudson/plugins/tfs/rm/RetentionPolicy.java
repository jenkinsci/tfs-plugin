package hudson.plugins.tfs.rm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ankit Goyal
 */

public class RetentionPolicy
{
    
    private Integer daysToKeep;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
    * 
    * @return
    * The daysToKeep
    */
    public Integer getDaysToKeep()
    {
        return daysToKeep;
    }

    /**
    * 
    * @param daysToKeep
    * The daysToKeep
    */
    public void setDaysToKeep(Integer daysToKeep)
    {
        this.daysToKeep = daysToKeep;
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

