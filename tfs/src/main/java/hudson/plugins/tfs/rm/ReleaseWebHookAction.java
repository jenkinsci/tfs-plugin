package hudson.plugins.tfs.rm;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
 * @author Kalyan
 */
public class ReleaseWebHookAction extends Notifier implements Serializable {

    private static final Logger logger = Logger.getLogger(ReleaseWebHookAction.class.getName());
    private List<ReleaseWebHookReference> webHookReferences;
    private final String apiVersion = "5.0-preview";

    @DataBoundConstructor
    public ReleaseWebHookAction(final List<ReleaseWebHookReference> webHookReferences) {
        this.webHookReferences = webHookReferences;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public List<ReleaseWebHookReference> getWebHookReferences() {
        return this.webHookReferences;
    }

    public void setWebHookReferences(final List<ReleaseWebHookReference> webHookReferences) {
        this.webHookReferences = webHookReferences;
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
        json.put("id", build.getNumber());
        json.put("startedBy", getStartedBy(build));

        HashMap<String, ReleaseWebHook> nameToWebHookMap = new HashMap<String, ReleaseWebHook>();
        for (ReleaseWebHook webHook : ReleaseWebHookHelper.getReleaseWebHookConfigurations()) {
            nameToWebHookMap.put(webHook.getWebHookName(), webHook);
        }

        List<ReleaseWebHookStatus> webHookStatus = new ArrayList<ReleaseWebHookStatus>();
        ReleaseWebHook webHook = null;
        for (ReleaseWebHookReference webHookName : webHookReferences) {
            if (nameToWebHookMap.containsKey(webHookName.getWebHookName())) {
                try {
                    webHook = nameToWebHookMap.get(webHookName.getWebHookName());
                    logger.fine(String.format("Sending payload event to %s", webHook.getPayloadUrl()));
                    ReleaseWebHookStatus status = sendJobCompletedEvent(json, webHook);

                    webHookStatus.add(status);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, StringUtils.EMPTY, ex);
                    webHookStatus.add(new ReleaseWebHookStatus(webHook.getPayloadUrl(), HttpURLConnection.HTTP_INTERNAL_ERROR, ex.toString()));
                }
            }
        }

        build.addAction(new ReleaseWebHookSummaryAction((webHookStatus)));
        return true;
    }

    private ReleaseWebHookStatus sendJobCompletedEvent(final JSONObject json, final ReleaseWebHook webHook) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        HttpClient client = HttpClientBuilder.create().build();
        final HttpPost request = new HttpPost(webHook.getPayloadUrl());
        final String payload = json.toString();

        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json; api-version=" + apiVersion);

        if (!StringUtils.isBlank(webHook.getSecret())) {
            String signature = ReleaseWebHookHelper.getPayloadSignature(webHook.getSecret(), payload);
            request.addHeader("X-Jenkins-Signature", signature);
        }

        request.setEntity(new StringEntity(payload));
        final HttpResponse response = client.execute(request);
        final int statusCode = response.getStatusLine().getStatusCode();

        ReleaseWebHookStatus status = null;
        if (statusCode == HttpURLConnection.HTTP_OK) {
            logger.log(Level.INFO, "sent event payload successfully");
            status = new ReleaseWebHookStatus(webHook.getPayloadUrl(), statusCode);
        } else {
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            logger.log(Level.WARNING, "Cannot send the event to webhook. Content:" + content);
            status = new ReleaseWebHookStatus(webHook.getPayloadUrl(), statusCode, content);
        }

        return status;
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
