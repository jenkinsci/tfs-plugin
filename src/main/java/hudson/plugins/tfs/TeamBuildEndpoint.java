package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.tfs.util.MediaType;
import jenkins.model.Jenkins;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * The endpoint that TFS/Team Services will PUT to when it wants to schedule a build in Jenkins.
 */
@Extension
public class TeamBuildEndpoint implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TeamBuildEndpoint.class.getName());
    public static final String URL_NAME = "team-build";
    static final String URL_PREFIX = "/" + URL_NAME + "/";

    private String commandName;
    private String jobName;

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

    public String getCommandName() {
        return commandName;
    }

    public String getJobName() {
        return jobName;
    }

    boolean decodeCommandAndJobNames(final String pathInfo) {
        if (pathInfo.startsWith(URL_PREFIX)) {
            final String restOfPath = pathInfo.substring(URL_PREFIX.length());
            final int firstSlash = restOfPath.indexOf('/');
            if (firstSlash != -1) {
                commandName = restOfPath.substring(0, firstSlash);
                if (firstSlash < restOfPath.length() - 1) {
                    jobName = restOfPath.substring(firstSlash + 1);
                    return true;
                }
            }
            else {
                commandName = restOfPath;
            }
        }

        return false;
    }

    public HttpResponse doIndex(final HttpServletRequest request) throws IOException {
        final Class<? extends TeamBuildEndpoint> me = this.getClass();
        final InputStream stream = me.getResourceAsStream("TeamBuildEndpoint.html");
        final Jenkins instance = Jenkins.getInstance();
        final String rootUrl = instance.getRootUrl();
        final String commandRows = "<tr><td>TODO</td><td>TODO</td><td>TODO</td></tr>";
        try {
            final String template = IOUtils.toString(stream, MediaType.UTF_8);
            final String content = String.format(template, URL_NAME, commandRows, rootUrl);
            return HttpResponses.html(content);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
