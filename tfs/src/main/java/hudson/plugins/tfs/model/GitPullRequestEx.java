package hudson.plugins.tfs.model;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPullRequest;
import com.microsoft.visualstudio.services.webapi.model.ResourceRef;

import java.util.Arrays;

/**
 * Work around some missing fields in the version of vso-httpclient-java
 * by extending {@link GitPullRequest}.
 */
public class GitPullRequestEx extends GitPullRequest {
    private ResourceRef[] workItemRefs;

    /**
     * Returns the work item references.
     */
    public ResourceRef[] getWorkItemRefs() {
        if (workItemRefs != null) {
            return Arrays.copyOf(workItemRefs, workItemRefs.length);
        }
        return null;
    }

    /**
     * Sets the work item references.
     */
    public void setWorkItemRefs(final ResourceRef[] workItemRefs) {
        this.workItemRefs = (workItemRefs != null) ? Arrays.copyOf(workItemRefs, workItemRefs.length) : null;
    }
}
