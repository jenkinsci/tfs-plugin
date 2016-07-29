package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.tfs.model.AbstractCommand;
import hudson.plugins.tfs.model.BuildCommand;
import hudson.plugins.tfs.model.PingCommand;
import hudson.plugins.tfs.util.MediaType;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The endpoint that TFS/Team Services will PUT to when it wants to schedule a build in Jenkins.
 */
@Extension
public class TeamBuildEndpoint implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TeamBuildEndpoint.class.getName());
    private static final Map<String, AbstractCommand.Factory> COMMAND_FACTORIES_BY_NAME;
    public static final String URL_NAME = "team-build";
    static final String URL_PREFIX = "/" + URL_NAME + "/";

    static {
        final Map<String, AbstractCommand.Factory> map = new TreeMap<String, AbstractCommand.Factory>(String.CASE_INSENSITIVE_ORDER);
        map.put("ping", new PingCommand.Factory());
        map.put("build", new BuildCommand.Factory());
        COMMAND_FACTORIES_BY_NAME = Collections.unmodifiableMap(map);
    }

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
                    final String encodedJobName = restOfPath.substring(firstSlash + 1);
                    try {
                        jobName = URLDecoder.decode(encodedJobName, MediaType.UTF_8.name());
                    }
                    catch (final UnsupportedEncodingException e) {
                        throw new Error(e);
                    }
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
        final String commandRows = describeCommands(COMMAND_FACTORIES_BY_NAME, URL_NAME);
        try {
            final String template = IOUtils.toString(stream, MediaType.UTF_8);
            final String content = String.format(template, URL_NAME, commandRows, rootUrl);
            return HttpResponses.html(content);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }
    static String describeCommands(final Map<String, AbstractCommand.Factory> commandMap, final String urlName) {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, AbstractCommand.Factory> commandPair : commandMap.entrySet()) {
            final String commandName = commandPair.getKey();
            final AbstractCommand.Factory factory = commandPair.getValue();
            sb.append("<tr>").append(newLine);
            sb.append("<td valign='top'>").append(commandName).append("</td>").append(newLine);
            sb.append("<td valign='top'>").append('/').append(urlName).append('/').append(commandName).append('/').append("JOB_NAME").append("</td>").append(newLine);
            final String rawSample = factory.getSampleRequestPayload();
            final String escapedSample = StringEscapeUtils.escapeHtml4(rawSample);
            sb.append("<td><pre>").append(escapedSample).append("</pre></td>").append(newLine);
            sb.append("</tr>").append(newLine);
        }
        return sb.toString();
    }


    void checkPermission(final AbstractProject project, final StaplerRequest req, final StaplerResponse rsp) {
        if (!Jenkins.getInstance().isUseSecurity()) {
            return;    // everyone is authorized
        }

        project.checkPermission(Item.BUILD);

        if (req.getMethod().equals("PUT")) {
            return;
        }

        throw HttpResponses.error(SC_METHOD_NOT_ALLOWED, "Only PUT is supported");
    }

    void dispatch(final StaplerRequest req, final StaplerResponse rsp, final TimeDuration delay) {
        // TODO: it looks like command and jobName might preserve their values across requests?
        final String pathInfo = req.getPathInfo();
        if (!decodeCommandAndJobNames(pathInfo)) {
            if (commandName == null) {
                throw HttpResponses.error(SC_BAD_REQUEST, "Command not provided");
            }
            if (jobName == null) {
                throw HttpResponses.error(SC_BAD_REQUEST, "Job name not provided after command");
            }
        }

        if (!COMMAND_FACTORIES_BY_NAME.containsKey(commandName)) {
            throw HttpResponses.error(SC_BAD_REQUEST, "Command not implemented");
        }

        final Jenkins jenkins = Jenkins.getInstance();
        final AbstractProject project = jenkins.getItemByFullName(jobName, AbstractProject.class);
        if (project == null) {
            throw HttpResponses.error(SC_BAD_REQUEST, "Project not found");
        }
        checkPermission(project, req, rsp);
        final TimeDuration actualDelay =
                delay == null ? new TimeDuration(project.getQuietPeriod()) : delay;

        final AbstractCommand.Factory factory = COMMAND_FACTORIES_BY_NAME.get(commandName);
        try {
            final JSONObject formData = req.getSubmittedForm();
            final AbstractCommand command = factory.create();
            final JSONObject response = command.perform(project, actualDelay, formData);

            rsp.setStatus(SC_OK);
            rsp.setContentType(MediaType.APPLICATION_JSON_UTF_8);
            final PrintWriter w = rsp.getWriter();
            final String responseJsonString = response.toString();
            w.print(responseJsonString);
            w.println();
        }
        catch (final IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "IllegalArgumentException", e);
            // TODO: serialize it to JSON and set as the response
            throw HttpResponses.error(SC_BAD_REQUEST, e.getMessage());
        }
        catch (final Exception e) {
            final String template = "Error while performing reaction to '%s' command.";
            final String message = String.format(template, commandName);
            LOGGER.log(Level.SEVERE, message, e);
            // TODO: serialize it to JSON and set as the response
            throw HttpResponses.error(SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    public void doPing(
            final StaplerRequest request,
            final StaplerResponse response,
            @QueryParameter final TimeDuration delay
    ) {
        dispatch(request, response, delay);
    }

    public void doBuild(
            final StaplerRequest request,
            final StaplerResponse response,
            @QueryParameter final TimeDuration delay
    ) {
        dispatch(request, response, delay);
    }

}
