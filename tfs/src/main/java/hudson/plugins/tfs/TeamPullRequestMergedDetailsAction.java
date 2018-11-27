//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import com.microsoft.teamfoundation.core.webapi.model.TeamProjectReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.ResourceRef;
import hudson.model.Action;
import hudson.model.Run;
import hudson.plugins.tfs.model.GitPullRequestEx;
import hudson.plugins.tfs.model.GitPullRequestWrapper;
import hudson.plugins.tfs.model.GitRepositoryWrapper;
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
    public GitPullRequestWrapper gitPullRequestWrapper;

    public TeamPullRequestMergedDetailsAction() {

    }

    public TeamPullRequestMergedDetailsAction(final GitPullRequestEx gitPullRequest, final String message, final String detailedMessage, final String collectionUri) {
        this.gitPullRequest = gitPullRequest;
        this.message = message;
        this.detailedMessage = detailedMessage;
        this.collectionUri = collectionUri;
        this.gitPullRequestWrapper = new GitPullRequestWrapper(gitPullRequest);
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
        if (gitPullRequest == null)
            return null;

        return gitPullRequest.getWorkItemRefs();
    }

    @Exported
    public boolean hasWorkItems() {
        final ResourceRef[] workItemRefs = gitPullRequest.getWorkItemRefs();
        return workItemRefs != null && workItemRefs.length > 0;
    }

    @Exported
    public String getPullRequestUrl() {
        final GitRepositoryWrapper repository = gitPullRequestWrapper.repository;
        final URI collectionUri = URI.create(this.collectionUri);
        final URI pullRequestUrl = UriHelper.join(collectionUri,
                repository.projectName,
                "_git",
                repository.name,
                "pullrequest",
                gitPullRequestWrapper.pullRequestId
        );
        final String result = pullRequestUrl.toString();
        return result;
    }
}
