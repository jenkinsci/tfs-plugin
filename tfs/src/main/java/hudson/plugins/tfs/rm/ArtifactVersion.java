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
