//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.model.AbstractHookEvent;
import hudson.plugins.tfs.model.ConnectHookEvent;
import hudson.plugins.tfs.model.GitPullRequestMergedEvent;
import hudson.plugins.tfs.model.GitPushEvent;
import hudson.plugins.tfs.model.PingHookEvent;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.rm.ConnectReleaseWebHookEvent;
import hudson.plugins.tfs.telemetry.TelemetryHelper;
import hudson.plugins.tfs.util.EndpointHelper;
import hudson.plugins.tfs.util.MediaType;
import hudson.plugins.tfs.util.StringBodyParameter;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The endpoint that TFS/Team Services will POST to on Git code push, pull request merge commit creation, etc.
 */
@Extension
public class TeamEventsEndpoint implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TeamEventsEndpoint.class.getName());
    private static final Map<String, AbstractHookEvent.Factory> HOOK_EVENT_FACTORIES_BY_NAME;

    static {
        final Map<String, AbstractHookEvent.Factory> eventMap =
                new TreeMap<String, AbstractHookEvent.Factory>(String.CASE_INSENSITIVE_ORDER);
        eventMap.put("ping", new PingHookEvent.Factory());
        eventMap.put("gitPullRequestMerged", new GitPullRequestMergedEvent.Factory());
        eventMap.put("gitPush", new GitPushEvent.Factory());
        eventMap.put("connect", new ConnectHookEvent.Factory());
        eventMap.put("rmWebhook", new ConnectReleaseWebHookEvent.Factory());
        HOOK_EVENT_FACTORIES_BY_NAME = Collections.unmodifiableMap(eventMap);
    }

    public static final String URL_NAME = "team-events";
    static final String URL_PREFIX = "/" + URL_NAME + "/";

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

    public HttpResponse doIndex(final HttpServletRequest request) throws IOException {
        final Class<? extends TeamEventsEndpoint> me = this.getClass();
        final InputStream stream = me.getResourceAsStream("TeamEventsEndpoint.html");
        final Jenkins instance = Jenkins.getActiveInstance();
        final String rootUrl = instance.getRootUrl();
        final String eventRows = describeEvents(HOOK_EVENT_FACTORIES_BY_NAME, URL_NAME);
        try {
            final String template = IOUtils.toString(stream, MediaType.UTF_8);
            final String content = String.format(template, URL_NAME, eventRows, rootUrl);
            return HttpResponses.html(content);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

    static String describeEvents(final Map<String, AbstractHookEvent.Factory> eventMap, final String urlName) {
        final String newLine = System.getProperty("line.separator");
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, AbstractHookEvent.Factory> eventPair : eventMap.entrySet()) {
            final String eventName = eventPair.getKey();
            final AbstractHookEvent.Factory factory = eventPair.getValue();
            sb.append("<tr>").append(newLine);
            sb.append("<td valign='top'>").append(eventName).append("</td>").append(newLine);
            sb.append("<td valign='top'>").append('/').append(urlName).append('/').append(eventName).append("</td>").append(newLine);
            final String rawSample = factory.getSampleRequestPayload();
            final String escapedSample = StringEscapeUtils.escapeHtml4(rawSample);
            sb.append("<td><pre>").append(escapedSample).append("</pre></td>").append(newLine);
            sb.append("</tr>").append(newLine);
        }
        return sb.toString();
    }

    static String pathInfoToEventName(final String pathInfo) {
        if (pathInfo.startsWith(URL_PREFIX)) {
            final String restOfPath = pathInfo.substring(URL_PREFIX.length());
            final int firstSlash = restOfPath.indexOf('/');
            final String eventName;
            if (firstSlash != -1) {
                eventName = restOfPath.substring(0, firstSlash);
            }
            else {
                eventName = restOfPath;
            }
            return eventName;
        }
        return null;
    }

    void dispatch(final StaplerRequest request, final StaplerResponse rsp, final String body) {
        final String pathInfo = request.getPathInfo();
        final String eventName = pathInfoToEventName(pathInfo);
        try {
            final JSONObject response = innerDispatch(body, eventName, HOOK_EVENT_FACTORIES_BY_NAME);

            rsp.setStatus(SC_OK);
            rsp.setContentType(MediaType.APPLICATION_JSON_UTF_8);
            final PrintWriter w = rsp.getWriter();
            final String responseJsonString = response.toString();
            w.print(responseJsonString);
            w.println();
        }
        catch (final IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "IllegalArgumentException", e);
            EndpointHelper.error(SC_BAD_REQUEST, e);
        }
        catch (final Exception e) {
            final String template = "Error while performing reaction to '%s' event.";
            final String message = String.format(template, eventName);
            LOGGER.log(Level.SEVERE, message, e);
            EndpointHelper.error(SC_INTERNAL_SERVER_ERROR, e);
        }
    }

    static JSONObject innerDispatch(final String body, final String eventName, final Map<String, AbstractHookEvent.Factory> factoriesByName) throws IOException {
        if (StringUtils.isBlank(eventName) || !factoriesByName.containsKey(eventName)) {
            throw new IllegalArgumentException("Invalid event");
        }
        final AbstractHookEvent.Factory factory = factoriesByName.get(eventName);
        final Event serviceHookEvent = deserializeEvent(body);
        final String message = serviceHookEvent.getMessage() != null ? serviceHookEvent.getMessage().getText() : "";
        final String detailedMessage = serviceHookEvent.getDetailedMessage() != null ? serviceHookEvent.getDetailedMessage().getText() : "";
        final AbstractHookEvent hookEvent = factory.create();
        return hookEvent.perform(EndpointHelper.MAPPER, serviceHookEvent, message, detailedMessage);
    }

    public static Event deserializeEvent(final String input) throws IOException {
        final Event serviceHookEvent = EndpointHelper.MAPPER.readValue(input, Event.class);
        final String eventType = serviceHookEvent.getEventType();
        if (StringUtils.isEmpty(eventType)) {
            throw new IllegalArgumentException("Payload did not contain 'eventType'.");
        }
        // TODO: assert eventType with what Factory claims to support
        final Object resource = serviceHookEvent.getResource();
        if (resource == null) {
            throw new IllegalArgumentException("Payload did not contain 'resource'.");
        }
        return serviceHookEvent;
    }

    @RequirePOST
    public void doPing(
            final StaplerRequest request,
            final StaplerResponse response,
            @StringBodyParameter @Nonnull final String body) {
        dispatch(request, response, body);
    }

    @RequirePOST
    public void doGitPullRequestMerged(
            final StaplerRequest request,
            final StaplerResponse response,
            @StringBodyParameter @Nonnull final String body) {
        // Send telemetry
        TelemetryHelper.sendEvent("team-events-git-pr-merged", new TelemetryHelper.PropertyMapBuilder()
                .build());

        dispatch(request, response, body);
    }

    @RequirePOST
    public void doGitPush(
            final StaplerRequest request,
            final StaplerResponse response,
            @StringBodyParameter @Nonnull final String body) {
        // Send telemetry
        TelemetryHelper.sendEvent("team-events-git-push", new TelemetryHelper.PropertyMapBuilder()
                .build());
        dispatch(request, response, body);
    }

    @RequirePOST
    public void doConnect(
            final StaplerRequest request,
            final StaplerResponse response,
            @StringBodyParameter @Nonnull final String body) {
        // Send telemetry
        TelemetryHelper.sendEvent("team-events-connect", new TelemetryHelper.PropertyMapBuilder()
                .build());
        dispatch(request, response, body);
    }

    @RequirePOST
    public void doRmwebhook(
            final StaplerRequest request,
            final StaplerResponse response,
            @StringBodyParameter @Nonnull final String body) {
        // Send telemetry
        TelemetryHelper.sendEvent("team-events-rmwebhook", new TelemetryHelper.PropertyMapBuilder().build());
        dispatch(request, response, body);
    }

    public static <T extends Trigger> T findTrigger(final Job<?, ?> job, final Class<T> tClass) {
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            final ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
            for (final Object trigger : pJob.getTriggers().values()) {
                if (tClass.isInstance(trigger)) {
                    return tClass.cast(trigger);
                }
            }
        }
        return null;
    }

    // A job may have multiple triggers of the same type. For example, both TeamPRPushTrigger and TeamPushTrigger are TeamPushTrigger type.
    public static <T extends Trigger> List<T> findTriggers(final Job<?, ?> job, final Class<T> tClass) {
        List<T> triggerList = new ArrayList<>();
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            final ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
            for (final Object trigger : pJob.getTriggers().values()) {
                if (tClass.isInstance(trigger)) {
                    triggerList.add(tClass.cast(trigger));
                }
            }
        }
        return triggerList;
    }

    /**
     * A response contributor for triggering polling of a project.
     *
     * @since 1.4.1
     */
    public static class PollingScheduledResponseContributor extends GitStatus.ResponseContributor {
        /**
         * The project
         */
        private final Item project;

        /**
         * Constructor.
         *
         * @param project the project.
         */
        public PollingScheduledResponseContributor(Item project) {
            this.project = project;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addHeaders(StaplerRequest req, StaplerResponse rsp) {
            rsp.addHeader("Triggered", project.getAbsoluteUrl());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeBody(PrintWriter w) {
            w.println("Scheduled polling of " + project.getFullDisplayName());
        }
    }

    public static class ScheduledResponseContributor extends GitStatus.ResponseContributor {
        /**
         * The project
         */
        private final Item project;

        /**
         * Constructor.
         *
         * @param project the project.
         */
        public ScheduledResponseContributor(Item project) {
            this.project = project;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addHeaders(StaplerRequest req, StaplerResponse rsp) {
            rsp.addHeader("Triggered", project.getAbsoluteUrl());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeBody(PrintWriter w) {
            w.println("Scheduled " + project.getFullDisplayName());
        }
    }
}
