package hudson.plugins.tfs;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPullRequest;
import hudson.model.Action;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

/**
 * Captures details of the TFS/Team Services pull request event which triggered us.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamPullRequestMergedDetailsAction implements Action, Serializable {
    private static final long serialVersionUID = 1L;
    private static final String URL_NAME = "team-pullRequestMergedDetails";

    public GitPullRequest gitPullRequest;
    public String message;
    public String detailedMessage;

    public TeamPullRequestMergedDetailsAction() {

    }

    public TeamPullRequestMergedDetailsAction(final GitPullRequest gitPullRequest, final String message, final String detailedMessage) {
        this.gitPullRequest = gitPullRequest;
        this.message = message;
        this.detailedMessage = detailedMessage;
    }

    @Override
    public String getIconFileName() {
        // TODO: find an appropriate icon
        return "clipboard.png";
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
}
