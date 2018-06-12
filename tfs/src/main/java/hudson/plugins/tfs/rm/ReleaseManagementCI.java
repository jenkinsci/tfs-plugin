//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.google.gson.Gson;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Result;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.QueryParameter;

/**
 * @author Ankit Goyal
 */
public class ReleaseManagementCI extends Notifier implements Serializable {

    private static final long serialVersionUID = -760016860995557L;
    private static final Logger logger = Logger.getLogger(ReleaseManagementCI.class.getName());


    public final String collectionUrl;
    public final String projectName;
    public final String releaseDefinitionName;
    public transient String username;
    public transient Secret password;
    public String credentialsId;


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

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ReleaseManagementCI(String collectionUrl,
                               String projectName,
                               String releaseDefinitionName,
                               String credentialsId) {
        if (collectionUrl.endsWith("/"))
        {
            this.collectionUrl = collectionUrl;
        }
        else
        {
            this.collectionUrl = collectionUrl + "/";
        }

        this.projectName = projectName;
        this.releaseDefinitionName = releaseDefinitionName;
        StandardUsernamePasswordCredentials credential = TeamCollectionConfiguration.findCredentialsById(credentialsId);
        this.username = credential == null ? "" : credential.getUsername();
        this.password = credential == null ? Secret.fromString("") : credential.getPassword();
        this.credentialsId = credentialsId;
    }

    protected Object readResolve() {
        if (StringUtils.isNotBlank(collectionUrl)
                && password != null
                && StringUtils.isNotBlank(password.getPlainText())) {
            try {
                final URI uri = new URI(collectionUrl);
                String hostName = uri.getHost();
                List<StandardUsernamePasswordCredentials> credentials = TeamCollectionConfiguration.findCredentials(hostName);
                for (StandardUsernamePasswordCredentials credential : credentials) {
                    if ((StringUtils.isBlank(username) || credential.getUsername().equals(username))
                            && credential.getPassword().getPlainText().equals(password.getPlainText())) {
                        this.credentialsId = credential.getId();
                        return this;
                    }
                }
                this.credentialsId
                        = TeamCollectionConfiguration.setCredentials(hostName, username, password.getPlainText());
            } catch (Exception ex) {
                logger.log(Level.WARNING,
                        String.format("Get or generate credentials for collection url: %s and username: %s failed.", collectionUrl, username),
                        ex);
            }
        }
        return this;
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
        StandardUsernamePasswordCredentials credential = TeamCollectionConfiguration.findCredentialsById(credentialsId);
        this.username = credential == null ? "" : credential.getUsername();
        this.password = credential == null ? Secret.fromString("") : credential.getPassword();

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
                    CreateRelease(releaseManagementHttpClient, releaseDefinition, jobName, buildNumber, buildId, listener, build, launcher);
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
            BuildListener listener,
            AbstractBuild<?, ?> build,
            Launcher launcher) throws ReleaseManagementException, JSONException
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
            build.addAction(new ReleaseSummaryAction(jobName, buildId,
                    object.getJSONObject("_links").getJSONObject("web").getString("href")));
        }
    }

    private List<ReleaseArtifact> PrepareReleaseArtifacts(ReleaseDefinition releaseDefinition, Artifact jenkinsArtifact, String buildNumber, int buildId, BuildListener listener, ReleaseManagementHttpClient releaseManagementHttpClient) throws ReleaseManagementException {
        List<ReleaseArtifact> releaseArtifacts = new ArrayList<ReleaseArtifact>();
        for(final Artifact artifact : releaseDefinition.getArtifacts())
        {
            ReleaseArtifact releaseArtifact = new ReleaseArtifact();
            InstanceReference instanceReference = new InstanceReference();
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

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item owner,
                                                     @QueryParameter String collectionUrl) {
            StandardListBoxModel listBoxModel = new StandardListBoxModel();
            listBoxModel.withEmptySelection();
            String hostName = null;
            try {
                final URI uri = new URI(collectionUrl);
                hostName = uri.getHost();
            }
            catch (final URISyntaxException ignored) {
            }
            listBoxModel.withAll(TeamCollectionConfiguration.findCredentials(hostName, owner));
            return listBoxModel;
        }

        public ListBoxModel doFillProjectNameItems(@QueryParameter String collectionUrl,
                                                   @QueryParameter String credentialsId) {
            StandardListBoxModel listBoxModel = new StandardListBoxModel();
            listBoxModel.withEmptySelection();

            if (StringUtils.isBlank(collectionUrl) || StringUtils.isBlank(credentialsId)) {
                return listBoxModel;
            }

            StandardUsernamePasswordCredentials credential
                    = TeamCollectionConfiguration.findCredentialsById(credentialsId);
            if (credential == null) {
                return listBoxModel;
            }

            String username = credential.getUsername();
            Secret password = credential.getPassword();

            try {
                ReleaseManagementHttpClient releaseManagementHttpClient =
                        new ReleaseManagementHttpClient(
                                collectionUrl.toLowerCase(),
                                username,
                                password);

                List<Project> projects = releaseManagementHttpClient.GetProjectItems();
                for (Project project : projects) {
                    listBoxModel.add(project.getName());
                }
            } catch (ReleaseManagementException ex) {
                logger.log(Level.WARNING,
                        String.format("Get team project for collection url: %s failed.", collectionUrl),
                        ex);
            }
            return listBoxModel;
        }

        public ListBoxModel doFillReleaseDefinitionNameItems(@QueryParameter String collectionUrl,
                                                             @QueryParameter String credentialsId,
                                                             @QueryParameter String projectName) {
            StandardListBoxModel listBoxModel = new StandardListBoxModel();
            listBoxModel.withEmptySelection();

            if (StringUtils.isBlank(collectionUrl)
                    || StringUtils.isBlank(credentialsId)
                    || StringUtils.isBlank(projectName)) {
                return listBoxModel;
            }

            StandardUsernamePasswordCredentials credential
                    = TeamCollectionConfiguration.findCredentialsById(credentialsId);
            if (credential == null) {
                return listBoxModel;
            }
            String username = credential.getUsername();
            Secret password = credential.getPassword();

            try {
                ReleaseManagementHttpClient releaseManagementHttpClient =
                        new ReleaseManagementHttpClient(
                                collectionUrl.toLowerCase().replaceFirst(".visualstudio.com", ".vsrm.visualstudio.com"),
                                username,
                                password);

                List<ReleaseDefinition> releaseDefinitions = releaseManagementHttpClient.GetReleaseDefinitions(projectName);
                for (ReleaseDefinition releaseDefinition : releaseDefinitions) {
                    listBoxModel.add(releaseDefinition.getName());
                }
            } catch (ReleaseManagementException ex) {
                logger.log(Level.WARNING,
                        String.format("Get release definition for project: %s failed.", projectName),
                        ex);
            }

            return listBoxModel;
        }

    }
}
