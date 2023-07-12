//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TeamGitStatus {

    private static final Map<Result, GitStatusState> RESULT_TO_STATE;

    static {
        final Map<Result, GitStatusState> resultToStatus = new HashMap<Result, GitStatusState>();
        resultToStatus.put(Result.SUCCESS, GitStatusState.Succeeded);
        resultToStatus.put(Result.UNSTABLE, GitStatusState.Failed);
        resultToStatus.put(Result.FAILURE, GitStatusState.Failed);
        resultToStatus.put(Result.NOT_BUILT, GitStatusState.Error);
        resultToStatus.put(Result.ABORTED, GitStatusState.Error);
        RESULT_TO_STATE = Collections.unmodifiableMap(resultToStatus);
    }

    public GitStatusState state;
    public String description;
    public String targetUrl;
    public GitStatusContext context;

    public static TeamGitStatus fromRun(@Nonnull final Run<?, ?> run) {
        final TeamGitStatus status = new TeamGitStatus();
        final Result result = run.getResult();
        if (result == null) {
            status.state = GitStatusState.Pending;
            status.description = status.state.toString();
        } else {
            status.state = RESULT_TO_STATE.get(result);
            status.description = result.toString();
        }
        final Job<?, ?> job = run.getParent();
        status.description = job.getDisplayName() + run.getDisplayName() + ": " + status.description;
        status.targetUrl = run.getAbsoluteUrl();
        status.context = getStatusContext(job);
        return status;
    }

    public static TeamGitStatus fromJob(@Nonnull final Job job) {
        final TeamGitStatus status = new TeamGitStatus();
        status.state = GitStatusState.Pending;
        status.description = "Jenkins Job " + job.getDisplayName() + " queued";
        status.targetUrl = job.getAbsoluteUrl();
        status.context = getStatusContext(job);
        return status;
    }

    private static GitStatusContext getStatusContext(@Nonnull final Job job) {
        final String instanceUrl = StringUtils.stripEnd(Jenkins.getInstance().getRootUrl(), "/");
        final String projectDisplayName = job.getParent().getFullName() + "/" + job.getDisplayName();
        return new GitStatusContext(projectDisplayName, instanceUrl);
    }

    public String toJson() {
        final JSONObject jsonObject = JSONObject.fromObject(this);
        final String result = jsonObject.toString();
        return result;
    }
}
