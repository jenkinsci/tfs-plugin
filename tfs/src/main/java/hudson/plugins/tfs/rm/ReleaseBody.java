/*
 * The MIT License
 *
 * Copyright 2016 angoya.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
