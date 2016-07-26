package hudson.plugins.tfs;

import hudson.Extension;
import hudson.ExtensionList;
import jenkins.model.GlobalConfiguration;
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
    private static final TeamPluginGlobalConfig DEFAULT_CONFIG = new TeamPluginGlobalConfig();

    private List<TeamCollectionConfiguration> collectionConfigurations = new ArrayList<TeamCollectionConfiguration>();


    public TeamPluginGlobalConfig() {
        load();
    }

    public TeamPluginGlobalConfig(final List<TeamCollectionConfiguration> collectionConfigurations) {
        this.collectionConfigurations = collectionConfigurations;
    }

    public static TeamPluginGlobalConfig get() {
        final ExtensionList<GlobalConfiguration> configurationExtensions = all();
        final TeamPluginGlobalConfig config = configurationExtensions.get(TeamPluginGlobalConfig.class);
        final TeamPluginGlobalConfig result = ObjectUtils.defaultIfNull(config, DEFAULT_CONFIG);
        return result;
    }

    public List<TeamCollectionConfiguration> getCollectionConfigurations() {
        return collectionConfigurations;
    }

    public void setCollectionConfigurations(final List<TeamCollectionConfiguration> collectionConfigurations) {
        this.collectionConfigurations = collectionConfigurations;
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        try {
            req.bindJSON(this, json);
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
