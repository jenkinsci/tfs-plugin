package hudson.plugins.tfs.rm;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Implements ReleaseWebhook Post build action.
 * @author kasubram
 */
public class ReleaseWebHookAction extends Notifier implements Serializable {

    private static final Logger logger = Logger.getLogger(ReleaseWebHookAction.class.getName());
    private List<ReleaseWebHookName> webHookNames;
    private final String apiVersion = "5.0-preview";

    @DataBoundConstructor
    public ReleaseWebHookAction(final List<ReleaseWebHookName> webHookNames) {
        this.webHookNames = webHookNames;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public List<ReleaseWebHookName> getWebHookNames() {
        return this.webHookNames;
    }

    public void setWebHookNames(final List<ReleaseWebHookName> webHookNames) {
        this.webHookNames = webHookNames;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
        logger.entering("ReleaseWebhookAction", "Perform");

        JSONObject json = new JSONObject();
        final String payload = JenkinsEventNotifier.getApiJson(build.getUrl());
        if (payload != null) {
            json = JSONObject.fromObject(payload);
        }

        json.put("name", build.getProject().getName());
        json.put("startedBy", getStartedBy(build));

        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        if (config == null) {
            throw new InternalError("Cannot load TFS global configuration");
        }

        HashMap<String, ReleaseWebHook> nameToWebHookMap = new HashMap<String, ReleaseWebHook>();
        for (ReleaseWebHook webHook : config.getReleaseWebHookConfigurations()) {
            nameToWebHookMap.put(webHook.getWebHookName(), webHook);
        }

        for (ReleaseWebHookName webHookName : webHookNames) {
            if (nameToWebHookMap.containsKey(webHookName.getWebHookName())) {
                try {
                    ReleaseWebHook webHook = nameToWebHookMap.get(webHookName.getWebHookName());
                    sendJobCompletedEvent(json, webHook);
                } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }

                break;
            }
        }

        return true;
    }

    private void sendJobCompletedEvent(final JSONObject json, final ReleaseWebHook webHook) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        HttpClient client = HttpClientBuilder.create().build();
        final HttpPost request = new HttpPost(webHook.getPayloadUrl());
        final String payload = json.toString();

        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json; api-version=" + apiVersion);

        if (!StringUtils.isEmpty(webHook.getSecret())) {
            String signature = JenkinsEventNotifier.getPayloadSignature(webHook.getSecret(), payload);
            request.addHeader("X-Jenkins-Signature", signature);
        }

        request.setEntity(new StringEntity(payload));
        final HttpResponse response = client.execute(request);
        final int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == HttpURLConnection.HTTP_OK) {
            logger.log(Level.INFO, "sent event payload successfully");
        } else {
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            logger.log(Level.WARNING, "Cannot send the event to webhook. Content:" + content);
        }
    }

    /**
     * Implementation of DescriptorImpl.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "TFS/Team Services Release Webhook";
        }
    }

    private String getStartedBy(final AbstractBuild build) {
        final Cause.UserIdCause cause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
        String startedBy = "";
        if (cause != null && cause.getUserId() != null) {
            startedBy = cause.getUserId();
        }

        return startedBy;
    }
}
