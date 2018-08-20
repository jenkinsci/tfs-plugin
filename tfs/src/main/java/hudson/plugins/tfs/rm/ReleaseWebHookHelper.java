package hudson.plugins.tfs.rm;

import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

        return config.getReleaseWebHookConfigurations();
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

        config.setReleaseWebHookConfigurations(releaseWebHooks);
        config.save();
    }

    /**
     * Gets the payload signature for the given event payload.
     * @param secret
     * @param payload
     * @return
     */
    public static String getPayloadSignature(final String secret, final String payload) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        return JenkinsEventNotifier.getPayloadSignature(secret, payload);
    }
}
