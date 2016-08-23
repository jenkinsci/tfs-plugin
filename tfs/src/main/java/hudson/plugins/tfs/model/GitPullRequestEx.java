package hudson.plugins.tfs.model;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPullRequest;
import com.microsoft.visualstudio.services.webapi.model.ResourceRef;

/**
 * Work around some missing fields in the version of vso-httpclient-java
 * by extending {@link GitPullRequest}.
 */
public class GitPullRequestEx extends GitPullRequest {
    private ResourceRef[] workItemRefs;

    public ResourceRef[] getWorkItemRefs() {
        return workItemRefs;
    }

    public void setWorkItemRefs(final ResourceRef[] workItemRefs) {
        this.workItemRefs = workItemRefs;
    }
}
