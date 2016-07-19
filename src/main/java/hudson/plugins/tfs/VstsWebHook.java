package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.util.MediaType;
import hudson.plugins.tfs.util.StringBodyParameter;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
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
        LOGGER.log(Level.FINER, "{}/?event={}\n{}", new String[]{URL_NAME, event.name(), body});
        final Object parsedBody = event.parse(body);
        List<? extends GitStatus.ResponseContributor> contributors = null;
        switch (event) {
            case PING:
                final String message = "Pong from the Jenkins TFS plugin! Here's your body:\n" + parsedBody;
                final GitStatus.MessageResponseContributor contributor;
                contributor = new GitStatus.MessageResponseContributor(message);
                contributors = Collections.singletonList(contributor);
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
        }
        if (contributors == null) {
            return HttpResponses.error(SC_BAD_REQUEST, "Not implemented");
        }
        else {
            final List<? extends GitStatus.ResponseContributor> finalContributors = contributors;
            return new HttpResponse() {
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                        throws IOException, ServletException {
                    rsp.setStatus(SC_OK);
                    rsp.setContentType(MediaType.TEXT_PLAIN);
                    for (GitStatus.ResponseContributor c : finalContributors) {
                        c.addHeaders(req, rsp);
                    }
                    rsp.setCharacterEncoding(MediaType.UTF_8.name());
                    PrintWriter w = rsp.getWriter();
                    for (GitStatus.ResponseContributor c : finalContributors) {
                        c.writeBody(req, rsp, w);
                    }
                }
            };

        }
    }
}
