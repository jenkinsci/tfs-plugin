package hudson.plugins.tfs.rm;

import com.google.gson.Gson;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.*;

/**
 * @author Ankit Goyal
 */
public class ReleaseManagementCI extends Notifier{

    public final String collectionUrl;
    public final String projectName;
    public final String releaseDefinitionName;
    public final String username;
    public final Secret password;
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ReleaseManagementCI(String collectionUrl, String projectName, String releaseDefinitionName, String username, Secret password)
    {
        if (collectionUrl.endsWith("/"))
        {
            this.collectionUrl = collectionUrl;
        }
        else
        {
            this.collectionUrl = collectionUrl + "/";
        }
        
        //this.collectionUrl = this.collectionUrl.toLowerCase().replaceFirst(".visualstudio.com", ".vsrm.visualstudio.com");
        this.projectName = projectName;
        this.releaseDefinitionName = releaseDefinitionName;
        this.username = username;
        this.password = password;
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.tasks.BuildStep#getRequiredMonitorService()
     */
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

        /*
     * (non-Javadoc)
     *
     * @see
     * hudson.tasks.BuildStepCompatibilityLayer#perform(hudson.model.AbstractBuild
     * , hudson.Launcher, hudson.model.BuildListener)
     */
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException
    {
        String jobName = build.getProject().getName();
        int buildId = build.number;
        String buildNumber = build.getDisplayName();
        if (build.getResult() == Result.SUCCESS)
        {
            ReleaseManagementHttpClient releaseManagementHttpClient = 
                    new ReleaseManagementHttpClient(
                            this.collectionUrl.toLowerCase().replaceFirst(".visualstudio.com", ".vsrm.visualstudio.com"),
                            this.username,
                            this.password);
            
            try 
            {
                ReleaseDefinition releaseDefinition = null;
                List<ReleaseDefinition> releaseDefinitions = releaseManagementHttpClient.GetReleaseDefinitions(this.projectName);
                for(final ReleaseDefinition rd : releaseDefinitions)
                {
                    if(rd.getName().equalsIgnoreCase(this.releaseDefinitionName))
                    {
                        releaseDefinition = rd;
                        break;
                    }
                }

                if(releaseDefinition == null)
                {
                    listener.getLogger().printf("No release definition found with name: %s%n", this.releaseDefinitionName);
                    listener.getLogger().println("Release will not be triggered.");
                }
                else
                {
                    CreateRelease(releaseManagementHttpClient, releaseDefinition, jobName, buildNumber, buildId, listener);
                }
            }
            catch (ReleaseManagementException ex)
            {
                ex.printStackTrace(listener.error("Failed to trigger release.%n"));
            }
            catch (JSONException ex)
            {
                ex.printStackTrace(listener.error("Failed to trigger release.%n"));
            }
        }
        
        return true;
    }
    
    void CreateRelease(
            ReleaseManagementHttpClient releaseManagementHttpClient,
            ReleaseDefinition releaseDefinition,
            String jobName,
            String buildNumber,
            int buildId,
            BuildListener listener) throws ReleaseManagementException, JSONException
    {
        Artifact jenkinsArtifact = null;
        for(final Artifact artifact : releaseDefinition.getArtifacts())
        {
            if(artifact.getType().equalsIgnoreCase("jenkins") && artifact.getDefinitionReference().getDefinition().getName().equalsIgnoreCase(jobName))
            {
                jenkinsArtifact = artifact;
                break;
            }
        }
        
        if(jenkinsArtifact == null)
        {
            listener.getLogger().printf("No jenkins artifact found with name: %s%n", jobName);
        }
        else
        {
            List<ReleaseArtifact> releaseArtifacts = PrepareReleaseArtifacts(
                    releaseDefinition,
                    jenkinsArtifact,
                    buildNumber,
                    buildId,
                    listener,
                    releaseManagementHttpClient);            
            String description = "Triggered by " + buildNumber;
            ReleaseBody releaseBody = new ReleaseBody();
            releaseBody.setDescription(description);
            releaseBody.setDefinitionId(releaseDefinition.getId());
            releaseBody.setArtifacts(releaseArtifacts);
            releaseBody.setIsDraft(false);
            String body  = new Gson().toJson(releaseBody);

            listener.getLogger().printf("Triggering release...%n");
            String response = releaseManagementHttpClient.CreateRelease(this.projectName, body);
            listener.getLogger().printf("Successfully triggered release.%n");
            JSONObject object = new JSONObject(response);
            listener.getLogger().printf("Release Name: %s%n", object.getString("name"));
            listener.getLogger().printf("Release id: %s%n", object.getString("id"));
        }
    }

    private List<ReleaseArtifact> PrepareReleaseArtifacts(ReleaseDefinition releaseDefinition, Artifact jenkinsArtifact, String buildNumber, int buildId, BuildListener listener, ReleaseManagementHttpClient releaseManagementHttpClient) throws ReleaseManagementException {
        List<ReleaseArtifact> releaseArtifacts = new ArrayList<ReleaseArtifact>();
        InstanceReference instanceReference = new InstanceReference();
        for(final Artifact artifact : releaseDefinition.getArtifacts())
        {
            ReleaseArtifact releaseArtifact = new ReleaseArtifact();
            if(artifact == jenkinsArtifact)
            {
                instanceReference.setName(buildNumber);
                instanceReference.setId(Integer.toString(buildId));
            }
            else
            {
                listener.getLogger().printf("Fetching latest version for artifact: %s%n", artifact.getAlias());
                ReleaseArtifactVersionsResponse response = releaseManagementHttpClient.GetVersions(this.projectName, new ArrayList<Artifact>(Arrays.asList(artifact)));
                if(response.getArtifactVersions().isEmpty())
                {
                    throw new ReleaseManagementException("Could not fetch versions for the linked artifact sources");
                }
                if(response.getArtifactVersions().get(0).getVersions().isEmpty())
                {
                    throw new ReleaseManagementException("Could not fetch versions for the linked artifact: " + artifact.getAlias());
                }
                
                instanceReference.setName(response.getArtifactVersions().get(0).getVersions().get(0).getName());
                instanceReference.setId(response.getArtifactVersions().get(0).getVersions().get(0).getId());
            }
            
            releaseArtifact.setAlias(artifact.getAlias());
            releaseArtifact.setInstanceReference(instanceReference);
            releaseArtifacts.add(releaseArtifact);
        }
        return releaseArtifacts;
    }
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>
    {

        /*
         * (non-Javadoc)
         *
         * @see hudson.tasks.BuildStepDescriptor#isApplicable(java.lang.Class)
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) 
        {
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see hudson.model.Descriptor#getDisplayName()
         */
        @Override
        public String getDisplayName() 
        {
            return "Trigger release in TFS/Team Services";
        }

    }
}