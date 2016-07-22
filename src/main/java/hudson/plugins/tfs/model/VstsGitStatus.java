package hudson.plugins.tfs.model;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class VstsGitStatus {

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

    public static VstsGitStatus fromRun(@Nonnull final Run<?, ?> run) {
        final VstsGitStatus status = new VstsGitStatus();
        final Result result = run.getResult();
        status.state = RESULT_TO_STATE.get(result);
        status.description = result.toString();
        status.targetUrl = run.getAbsoluteUrl();
        final Job<?, ?> project = run.getParent();
        final String runDisplayName = run.getDisplayName();
        final String projectDisplayName = project.getDisplayName();
        status.context = new GitStatusContext(runDisplayName, projectDisplayName);
        return status;
    }

    public String toJson() {
        final JSONObject jsonObject = JSONObject.fromObject(this);
        final String result = jsonObject.toString();
        return result;
    }
}
