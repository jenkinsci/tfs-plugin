//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.Date;

@ExportedBean(defaultVisibility = 999)
public class GitPullRequestWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public String createdBy;
    public Date creationDate;
    public String description;
    public int pullRequestId;
    public String sourceRefName;
    public String targetRefName;
    public String title;
    public String url;
    public ResourceRefWrapper[] workItemRefs;
    public GitRepositoryWrapper repository;

    public GitPullRequestWrapper(final GitPullRequestEx gitPullRequest) {
        createdBy = gitPullRequest.getCreatedBy().getDisplayName();
        creationDate = gitPullRequest.getCreationDate();
        description = gitPullRequest.getDescription();
        pullRequestId = gitPullRequest.getPullRequestId();
        sourceRefName = gitPullRequest.getSourceRefName();
        targetRefName = gitPullRequest.getTargetRefName();
        title = gitPullRequest.getTitle();
        url = gitPullRequest.getUrl();

        if (gitPullRequest.getWorkItemRefs() != null) {
            ResourceRefWrapper[] workItems = new ResourceRefWrapper[gitPullRequest.getWorkItemRefs().length];
            for (int i = 0; i < gitPullRequest.getWorkItemRefs().length; i++) {
                workItems[i] = new ResourceRefWrapper(gitPullRequest.getWorkItemRefs()[i]);
            }
            workItemRefs = workItems;
        }

        repository = new GitRepositoryWrapper(gitPullRequest.getRepository());
    }
}
