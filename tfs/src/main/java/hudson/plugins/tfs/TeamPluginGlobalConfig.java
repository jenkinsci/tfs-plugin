//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.plugins.tfs.model.DomainUserAccountMapper;
import hudson.plugins.tfs.model.UserAccountMapper;
import hudson.plugins.tfs.model.UserAccountMapperDescriptor;
import hudson.plugins.tfs.rm.ReleaseWebHook;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * All the settings that apply globally.
 */
@Extension
public class TeamPluginGlobalConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(TeamPluginGlobalConfig.class.getName());
    public static final TeamPluginGlobalConfig DEFAULT_CONFIG = new TeamPluginGlobalConfig(false);

    private List<TeamCollectionConfiguration> collectionConfigurations = new ArrayList<TeamCollectionConfiguration>();
    private List<ReleaseWebHook> releaseWebHookConfigurations = new ArrayList<ReleaseWebHook>();
    
    private boolean configFolderPerNode;
    private boolean enableTeamPushTriggerForAllJobs;
    private boolean enableTeamStatusForAllJobs;
    private UserAccountMapper userAccountMapper;

    public TeamPluginGlobalConfig() {
        this(true);
    }

    TeamPluginGlobalConfig(final boolean shouldLoadConfig) {
        if (shouldLoadConfig) {
            load();
        }
    }

    public TeamPluginGlobalConfig(final List<TeamCollectionConfiguration> collectionConfigurations) {
        this.collectionConfigurations = collectionConfigurations;
    }

    public static TeamPluginGlobalConfig get() {
        TeamPluginGlobalConfig result = DEFAULT_CONFIG;
        if (Jenkins.getInstance() != null) {
            final ExtensionList<GlobalConfiguration> configurationExtensions = all();
            final TeamPluginGlobalConfig config = configurationExtensions.get(TeamPluginGlobalConfig.class);
            result = ObjectUtils.defaultIfNull(config, DEFAULT_CONFIG);
        }
        return result;
    }

    public List<TeamCollectionConfiguration> getCollectionConfigurations() {
        return collectionConfigurations;
    }

    public void setCollectionConfigurations(final List<TeamCollectionConfiguration> collectionConfigurations) {
        this.collectionConfigurations = collectionConfigurations;
    }
    
    public List<ReleaseWebHook> getReleaseWebHookConfigurations() {
        return this.releaseWebHookConfigurations;
    }
    
    public void setReleaseWebHookConfigurations(final List<ReleaseWebHook> releaseWebHookConfigurations) {
        this.releaseWebHookConfigurations = releaseWebHookConfigurations;
    }

    public boolean isConfigFolderPerNode() {
        return configFolderPerNode;
    }

    public void setConfigFolderPerNode(final boolean configFolderPerNode) {
        this.configFolderPerNode = configFolderPerNode;
    }

    public boolean isEnableTeamPushTriggerForAllJobs() {
        return enableTeamPushTriggerForAllJobs;
    }

    public void setEnableTeamPushTriggerForAllJobs(final boolean enableTeamPushTriggerForAllJobs) {
        this.enableTeamPushTriggerForAllJobs = enableTeamPushTriggerForAllJobs;
    }

    public boolean isEnableTeamStatusForAllJobs() {
        return enableTeamStatusForAllJobs;
    }

    public void setEnableTeamStatusForAllJobs(final boolean enableTeamStatusForAllJobs) {
        this.enableTeamStatusForAllJobs = enableTeamStatusForAllJobs;
    }

    public UserAccountMapper getUserAccountMapper() {
        if (userAccountMapper == null) {
            userAccountMapper = new DomainUserAccountMapper();
        }
        return userAccountMapper;
    }

    public void setUserAccountMapper(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    public List<UserAccountMapperDescriptor> getUserAccountMapperDescriptors() {
        return UserAccountMapper.all();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            req.bindJSON(this, json);
            
            // stapler oddity, empty lists are not set on bean by  "req.bindJSON(this, json)"
            this.releaseWebHookConfigurations = req.bindJSONToList(ReleaseWebHook.class, json.get("releaseWebHookConfigurations"));
        }
        catch (final Exception e) {
            final String message = "Configuration error: " + e.getMessage();
            LOGGER.log(Level.WARNING, message, e);
            LOGGER.log(Level.FINE, "Form data: {}", json.toString());
            throw new FormException(message, e, "team-configuration");
        }
        save();
        return true;
    }

    @Override
    public String getDisplayName() {
        return "TFS/Team Services";
    }
}
