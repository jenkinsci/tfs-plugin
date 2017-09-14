package hudson.plugins.tfs.listeners;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.model.GitStatusContext;
import hudson.plugins.tfs.model.GitStatusState;
import hudson.plugins.tfs.model.TeamGitStatus;
import hudson.plugins.tfs.util.TeamRestClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * This class listens to the events of every Jenkins run instance.
 * Completed runs fire an event back to the JenkinsEventNotifier.
 */
@Extension
public class JenkinsRunListener extends RunListener<Run> {
    protected static final Logger log = Logger.getLogger(JenkinsRunListener.class.getName());

    public JenkinsRunListener() {
        log.fine("JenkinsRunListener: constructor");
    }

    @Override
    public void onDeleted(final Run run) {
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        Job currJob = run.getParent();
        final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
        setPullRequestStatus(run, GitStatusState.Pending, "Jenkins CI build started", targetUrl);
    }

    @Override
    public void onFinalized(final Run run) {
    }

    @Override
    public void onCompleted(final Run run, @Nonnull final TaskListener listener) {
        log.info("onCompleted: " + run.toString());

        Job currJob = run.getParent();
        final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
        setPullRequestStatus(run, GitStatusState.Pending, "Jenkins CI build completed", targetUrl);

        final String payload = JenkinsEventNotifier.getApiJson(run.getUrl());
        JSONObject json = new JSONObject();
        if (payload != null) {
            json = JSONObject.fromObject(payload);
        }

        json.put("name", run.getParent().getDisplayName());
        json.put("startedBy", getStartedBy(run));

        JenkinsEventNotifier.sendJobCompletionEvent(json);
    }

    private String getStartedBy(final Run run) {
        final Cause.UserIdCause cause = (Cause.UserIdCause) run.getCause(Cause.UserIdCause.class);
        String startedBy = "";
        if (cause != null && cause.getUserId() != null) {
            startedBy = cause.getUserId();
        }
        return startedBy;
    }

    private TeamGitStatus setPullRequestStatus(final Run run, final GitStatusState buildState, final String buildDescription, final String targetUrl) {
        try {
            final TeamGitStatus status = new TeamGitStatus();
            status.state = buildState;
            status.description = buildDescription;
            status.targetUrl = targetUrl;
            status.context = new GitStatusContext("ci-build", "jenkins-plugin");

            final TeamRestClient client = new TeamRestClient(URI.create("https://mseng.visualstudio.com/"));
            return client.addPullRequestStatus(URI.create("https://mseng.visualstudio.com/Tools/_apis/git/repositories/Vsts-Git-Integration/pullRequests/232289/statuses"), status);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
