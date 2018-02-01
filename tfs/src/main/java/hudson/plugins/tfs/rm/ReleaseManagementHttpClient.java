//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import com.google.gson.Gson;
import hudson.util.Secret;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ankit Goyal
 */

public class ReleaseManagementHttpClient
{
    private final HttpClient httpClient;
    private final String username;
    private final Secret password;
    private final String accountUrl;
    private final String basicAuth;
    
    ReleaseManagementHttpClient(String accountUrl, String username, Secret password)
    {
        this.accountUrl = accountUrl;
        this.username = username;
        this.password = password;
        this.httpClient = new HttpClient();
        this.basicAuth = "Basic " + new String(Base64.encodeBase64((this.username + ":" + Secret.toString(this.password)).getBytes(Charset.defaultCharset())), Charset.defaultCharset());
    }
    
    public List<ReleaseDefinition> GetReleaseDefinitions(String project) throws ReleaseManagementException
    {
        String url = this.accountUrl + project + "/_apis/release/definitions?$expand=artifacts";
        String response = this.ExecuteGetMethod(url);
        DefinitionResponse definitionResponse = new Gson().fromJson(response, DefinitionResponse.class);
        return definitionResponse.getValue();
    }
    
    public String CreateRelease(String project, String body) throws ReleaseManagementException
    {
        String url = this.accountUrl + project + "/_apis/release/releases?api-version=3.0-preview.2";
        return this.ExecutePostmethod(url, body);
    }
    
    public ReleaseArtifactVersionsResponse GetVersions(String project, List<Artifact> artifacts) throws ReleaseManagementException
    {
        String url = this.accountUrl + project + "/_apis/release/artifacts/versions?api-version=3.0-preview.1";
        final String body = new Gson().toJson(artifacts);
        String response = this.ExecutePostmethod(url, body);
        return new Gson().fromJson(response, ReleaseArtifactVersionsResponse.class);
    }

    public List<Project> GetProjectItems() throws ReleaseManagementException
    {
        String url = this.accountUrl + "/_apis/projects?api-version=1.0";
        String response = this.ExecuteGetMethod(url);
        try {
            String values = new JSONObject(response).getString("value");
            return Arrays.asList(new Gson().fromJson(values, Project[].class));
        } catch (JSONException ex) {
            throw new ReleaseManagementException(ex);
        }
    }
    
    private String ExecutePostmethod(String url, String body) throws ReleaseManagementException
    {
        PostMethod postMethod = new PostMethod(url);
        postMethod.addRequestHeader("Authorization", this.basicAuth);
        postMethod.addRequestHeader("Content-Type", "application/json");
        postMethod.setRequestBody(body);
        String response;
        try
        {
            int status = this.httpClient.executeMethod(postMethod);
            response = postMethod.getResponseBodyAsString();
            if(status >= 300)
            {
                throw new ReleaseManagementException("Error occurred.%nStatus: " + status + "%nResponse: " + response + "%n");
            }
        }
        catch(Exception ex)
        {
            throw new ReleaseManagementException(ex);
        }
        
        return response;
    }
    
    private String ExecuteGetMethod(String url) throws ReleaseManagementException
    {
        GetMethod getMethod = new GetMethod(url);
        getMethod.addRequestHeader("Authorization", this.basicAuth);
        String response;
        try
        {
            int status = this.httpClient.executeMethod(getMethod);
            response = getMethod.getResponseBodyAsString();
            if(status >= 300)
            {
                throw new ReleaseManagementException("Error occurred.%nStatus: " + status + "%nResponse: " + response + "%n");
            }
        }
        catch(Exception ex)
        {
            throw new ReleaseManagementException(ex);
        }
        
        return response;
    }
    
    private class DefinitionResponse
    {

        private Integer count;
        private List<ReleaseDefinition> value = new ArrayList<ReleaseDefinition>();
        private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

        /**
        * 
        * @return
        * The count
        */
        public Integer getCount()
        {
            return count;
        }

        /**
        * 
        * @param count
        * The count
        */
        public void setCount(Integer count)
        {
            this.count = count;
        }

        /**
        * 
        * @return
        * The value
        */
        public List<ReleaseDefinition> getValue()
        {
            return value;
        }

        /**
        * 
        * @param value
        * The value
        */
        public void setValue(List<ReleaseDefinition> value)
        {
            this.value = value;
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
}
