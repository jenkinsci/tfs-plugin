//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import com.microsoft.teamfoundation.core.webapi.model.TeamProjectReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.ResourceRef;
import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.tfs.model.GitPullRequestEx;
import hudson.plugins.tfs.model.GitPushEvent;
import hudson.plugins.tfs.util.UriHelper;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Captures details of the TFS/Team Services pull request event which triggered us.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamPullRequestMergedDetailsAction implements Action, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String URL_NAME = "team-pullRequestMergedDetails";

    public transient GitPullRequestEx gitPullRequest;
    public String message;
    public String detailedMessage;
    public String collectionUri;

    public TeamPullRequestMergedDetailsAction() {

    }

    public TeamPullRequestMergedDetailsAction(final GitPullRequestEx gitPullRequest, final String message, final String detailedMessage, final String collectionUri) {
        this.gitPullRequest = gitPullRequest;
        this.message = message;
        this.detailedMessage = detailedMessage;
        this.collectionUri = collectionUri;
    }

    public static URI addWorkItemsForRun(final Run<?, ?> run, final List<ResourceRef> destination) {
        final TeamPullRequestMergedDetailsAction action = run.getAction(TeamPullRequestMergedDetailsAction.class);
        if (action != null && action.hasWorkItems()) {
            Collections.addAll(destination, action.getWorkItems());
            final GitPullRequestEx gitPullRequest = action.gitPullRequest;
            final URI collectionUri = URI.create(action.collectionUri);
            return collectionUri;
        }
        return null;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/tfs/48x48/logo.png";
    }

    @Override
    public String getDisplayName() {
        return "TFS/Team Services pull request";
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    // the following methods are called from this/summary.jelly and/or this/index.jelly

    @Exported
    public String getMessage() {
        return message;
    }

    @Exported
    public String getDetailedMessage() {
        return detailedMessage;
    }

    @Exported
    public ResourceRef[] getWorkItems() {
        return gitPullRequest.getWorkItemRefs();
    }

    @Exported
    public boolean hasWorkItems() {
        final ResourceRef[] workItemRefs = gitPullRequest.getWorkItemRefs();
        return workItemRefs != null && workItemRefs.length > 0;
    }

    @Exported
    public String getPullRequestUrl() {
        final GitRepository repository = gitPullRequest.getRepository();
        final URI collectionUri = URI.create(this.collectionUri);
        final TeamProjectReference project = repository.getProject();
        final URI pullRequestUrl = UriHelper.join(collectionUri,
                project.getName(),
                "_git",
                repository.getName(),
                "pullrequest",
                gitPullRequest.getPullRequestId()
        );
        final String result = pullRequestUrl.toString();
        return result;
    }
}
