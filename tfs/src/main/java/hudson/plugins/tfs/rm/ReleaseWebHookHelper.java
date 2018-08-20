package hudson.plugins.tfs.rm;

import hudson.plugins.tfs.TeamPluginGlobalConfig;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kalyan
 */
public final class ReleaseWebHookHelper {
    private ReleaseWebHookHelper() {
    }

    /**
     * Gets the release webHook configuration from global config.
     * @return list of release webHook
     */
    public static List<ReleaseWebHook> getReleaseWebHookConfigurations() {
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        if (config == null) {
            throw new InternalError("Cannot load TFS global configuration");
        }

        //return config.getReleaseWebHookConfigurations();
        return new ArrayList<ReleaseWebHook>();
    }

    /**
     * Saves the release webHooks with the global config.
     * @param releaseWebHooks
     */
    public static void saveReleaseWebHookConfigurations(final List<ReleaseWebHook> releaseWebHooks) {
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        if (config == null) {
            throw new InternalError("Cannot load TFS global configuration");
        }

        //config.setReleaseWebHookConfigurations(releaseWebHooks);
        config.save();
    }

    /**
     * Gets the payload signature for the given event payload.
     * @param secret
     * @param payload
     * @return
     */
    public static String getPayloadSignature(final String secret, final String payload) {
        //JenkinsEventNotifier.getPayloadSignature(secret payload);
        return null;
    }
}
