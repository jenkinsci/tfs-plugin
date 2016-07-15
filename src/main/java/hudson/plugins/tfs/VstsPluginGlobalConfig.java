package hudson.plugins.tfs;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;

/**
 * All the settings that apply globally.
 */
@Extension
public class VstsPluginGlobalConfig extends GlobalConfiguration {

    @Override
    public String getDisplayName() {
        return "Visual Studio Team Services and Team Foundation Server";
    }
}
