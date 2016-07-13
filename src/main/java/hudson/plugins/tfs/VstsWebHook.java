package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.tfs.util.StringBodyParameter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * The endpoint that VSTS will POST to on push, pull request, etc.
 */
@Extension
public class VstsWebHook implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(VstsWebHook.class.getName());

    public static final String URL_NAME = "vsts-hook";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    @RequirePOST
    public HttpResponse doIndex(
            final HttpServletRequest request,
            @QueryParameter(required = true) @Nonnull final VstsHookEventName event,
            @StringBodyParameter @Nonnull final String body) {
        switch (event) {
            case PING:
                return HttpResponses.plainText("Pong from the Jenkins TFS plugin! Here's your body:\n" + body);
            case BUILD_COMPLETED:
                break;
            case GIT_CODE_PUSHED:
                break;
            case TFVC_CODE_CHECKED_IN:
                break;
            case PULL_REQUEST_MERGE_COMMIT_CREATED:
                break;
            case DEPLOYMENT_COMPLETED:
                break;
            case RELEASE_CREATED:
                break;
        }
        return HttpResponses.error(SC_BAD_REQUEST, "Not implemented");
    }
}
