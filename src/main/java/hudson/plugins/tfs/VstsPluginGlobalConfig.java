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
public class VstsPluginGlobalConfig extends GlobalConfiguration {

    private static final Logger LOGGER = Logger.getLogger(VstsPluginGlobalConfig.class.getName());
    private static final VstsPluginGlobalConfig DEFAULT_CONFIG = new VstsPluginGlobalConfig();

    private List<VstsCollectionConfiguration> collectionConfigurations = new ArrayList<VstsCollectionConfiguration>();


    public VstsPluginGlobalConfig() {
        load();
    }

    public VstsPluginGlobalConfig(final List<VstsCollectionConfiguration> collectionConfigurations) {
        this.collectionConfigurations = collectionConfigurations;
    }

    public static VstsPluginGlobalConfig get() {
        final ExtensionList<GlobalConfiguration> configurationExtensions = all();
        final VstsPluginGlobalConfig config = configurationExtensions.get(VstsPluginGlobalConfig.class);
        final VstsPluginGlobalConfig result = ObjectUtils.defaultIfNull(config, DEFAULT_CONFIG);
        return result;
    }

    public List<VstsCollectionConfiguration> getCollectionConfigurations() {
        return collectionConfigurations;
    }

    public void setCollectionConfigurations(final List<VstsCollectionConfiguration> collectionConfigurations) {
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
            throw new FormException(message, e, "vsts-configuration");
        }
        save();
        return true;
    }

    @Override
    public String getDisplayName() {
        return "Visual Studio Team Services and Team Foundation Server";
    }
}
