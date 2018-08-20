package hudson.plugins.tfs.rm;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import hudson.plugins.tfs.model.AbstractHookEvent;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 * This HookEvent is for to create/delete Release Webhook.
*/
public class ConnectReleaseWebHookEvent extends AbstractHookEvent {

    private static final Logger logger = Logger.getLogger(ReleaseWebHookAction.class.getName());
    private static final String CREATE_WEBHOOK_EVENTNAME = "rmwebhook-create";
    private static final String REMOVE_WEBHOOK_ENENTNAME = "rmwebhook-remove";
    private static final String LIST_WEBHOOK_EVENTNAME = "rmwebhook-list";
    private static final String LINK_WEBHOOK_EVENTNAME = "rmwebhook-link";
    private static final String UNLINK_WEBHOOK_EVENTNAME = "rmwebhook-unlink";

    /**
     * Factory to create ConnectReleaseWebHookEvent.
     */
    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public ConnectReleaseWebHookEvent create() {
            return new ConnectReleaseWebHookEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n"
                 + "    \"eventType\": rmwebhook-create\n"
                 + "    \"resource\": {"
                 + "       \"webhookName\": \"webhook name\"\n"
                 + "       \"payloadUrl\": \"https://xplatalm.visualstudio.com/_apis/Release/receiveExternalEvent/wenhookId\"\n"
                 + "       \"secret\": \"secret\"\n"
                 + "     }"
                 + "}";
        }
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final Event event, final String message, final String detailedMessage) {
        final Object resource = event.getResource();
        final ReleaseWebHookResource parameters = mapper.convertValue(resource, ReleaseWebHookResource.class);

        if (event.getEventType().equalsIgnoreCase(CREATE_WEBHOOK_EVENTNAME)) {
            createReleaseWebHook(parameters);
        } else if (event.getEventType().equalsIgnoreCase(REMOVE_WEBHOOK_ENENTNAME)) {
            deleteReleaseWebHook(parameters);
        } else if (event.getEventType().equalsIgnoreCase(LIST_WEBHOOK_EVENTNAME)) {
            return listReleaseWebHook();
        } else if (event.getEventType().equalsIgnoreCase(LINK_WEBHOOK_EVENTNAME)) {
            linkWebHook(parameters);
        } else if (event.getEventType().equalsIgnoreCase(UNLINK_WEBHOOK_EVENTNAME)) {
            unlinkWebHook(parameters);
        } else {
            throw new UnsupportedOperationException("Webhook operation " + parameters.getOperationType() + " is not supported");
        }

        return JSONObject.fromObject(event);
    }

    private String validateAndGetPayloadUrl(final ReleaseWebHookResource parameters) {
        String payloadUrl = parameters.getPayloadUrl();

        if (StringUtils.isBlank(payloadUrl)) {
            throw new InvalidParameterException("PayloadUrl is empty");
        }

        final URI uri;
        try {
            uri = new URI(payloadUrl);
        } catch (final URISyntaxException e) {
            throw new InvalidParameterException("Malformed Payload URL " + e.getMessage());
        }

        final String hostName = uri.getHost();
        if (StringUtils.isBlank(hostName)) {
            throw new InvalidParameterException("Payload URL is empty");
        }

        return StringUtils.stripEnd(StringUtils.trim(payloadUrl), "/");
    }

    private AbstractProject validateAndGetJenkinsProject(final ReleaseWebHookResource resource) {
        String projectName = resource.getProjectName();

        if (StringUtils.isBlank(projectName)) {
            throw new InvalidParameterException("Project name is empty");
        }

        for (final Item project : Jenkins.getActiveInstance().getAllItems()) {
            if (project instanceof AbstractProject && project.getName().equalsIgnoreCase(projectName)) {
                return (AbstractProject) project;
            }
        }

        throw new InvalidParameterException("Cannot find Jenkins Project with the name " + resource.getProjectName());
    }

    private ReleaseWebHook validateAndGetReleaseWebHookByName(final String webHookName) {
        ReleaseWebHook webHook = null;
        for (ReleaseWebHook webHookItem : ReleaseWebHookHelper.getReleaseWebHookConfigurations()) {
            if (webHookItem.getWebHookName().equalsIgnoreCase(webHookName)) {
                webHook = webHookItem;
                logger.fine(String.format("WebHook found for webhook name %s", webHookName));
                break;
            }
        }

        if (webHook == null) {
            throw new InvalidParameterException(String.format("Cannot find webhook with the name %s", webHookName));
        }

        return webHook;
    }

    private void createReleaseWebHook(final ReleaseWebHookResource resource) {
        if (resource == null) {
            throw new InvalidParameterException("resource is null");
        }

        String payloadUrl = validateAndGetPayloadUrl(resource);
        String secret = resource.getSecret();
        String webHookName = resource.getWebHookName();

        if (StringUtils.isBlank(webHookName)) {
            throw new InvalidParameterException("webhook name is empty");
        }

        List<ReleaseWebHook> releaseWebHooks = ReleaseWebHookHelper.getReleaseWebHookConfigurations();
        for (ReleaseWebHook webHook : releaseWebHooks) {
            if (webHook.getWebHookName().equalsIgnoreCase(webHookName)) {
                if (webHook.getPayloadUrl().equalsIgnoreCase(payloadUrl)) {
                    logger.fine(String.format("WebHook with the name %s and payloadUrl %s already exists", webHookName, payloadUrl));
                    return;
                } else {
                    throw new InvalidParameterException(String.format("WebHook with the name %s already exists. Provide a unique name for the ReleaseWebHook", webHookName));
                }
            }
        }

        ReleaseWebHook webHook = new ReleaseWebHook(webHookName, payloadUrl, secret);
        releaseWebHooks.add(webHook);
        ReleaseWebHookHelper.saveReleaseWebHookConfigurations(releaseWebHooks);
    }

    private void deleteReleaseWebHook(final ReleaseWebHookResource resource) {
        if (resource == null) {
            throw new InvalidParameterException("resource is null");
        }

        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        if (config == null) {
            throw new InternalError("Cannot load TFS global configuration");
        }

        String webHookNameToDelete = resource.getWebHookName();
        ReleaseWebHook webHook = validateAndGetReleaseWebHookByName(webHookNameToDelete);

        for (final Item projectItem : Jenkins.getActiveInstance().getAllItems()) {
            if (projectItem instanceof AbstractProject) {
                AbstractProject project = (AbstractProject) projectItem;

                ReleaseWebHookAction action = getReleaseWebHookActionFromProject(project);
                if (action != null) {
                    for (ReleaseWebHookReference webHookReference : action.getWebHookReferences()) {
                        if (webHookReference.getWebHookName().equalsIgnoreCase(webHookNameToDelete)) {
                            throw new UnsupportedOperationException(String.format("WebHook %s is referenced in project %s. Cannot delete until all the references are removed.", webHookNameToDelete, project.getName()));
                        }
                    }
                }
            }
        }

        List<ReleaseWebHook> releaseWebHooks = ReleaseWebHookHelper.getReleaseWebHookConfigurations();
        releaseWebHooks.remove(webHook);
        ReleaseWebHookHelper.saveReleaseWebHookConfigurations(releaseWebHooks);
    }

    private JSONObject listReleaseWebHook() {
        JSONArray webHooks = new JSONArray();

        for (ReleaseWebHook webHookConfig : ReleaseWebHookHelper.getReleaseWebHookConfigurations()) {
            JSONObject webHook = new JSONObject();
            webHook.put("WebHookName", webHookConfig.getWebHookName());
            webHook.put("PayloadUrl", webHookConfig.getPayloadUrl());

            webHooks.add(webHook);
        }

        JSONObject result = new JSONObject();
        result.put("ReleaseWebHooks", webHooks);

        return result;
    }

    private void linkWebHook(final ReleaseWebHookResource resource) {
        if (resource == null) {
            throw new InvalidParameterException("resource is null");
        }

        AbstractProject project = validateAndGetJenkinsProject(resource);
        String webHookNameToLink = resource.getWebHookName();

        // validate the webhook exists in the server config
        ReleaseWebHook webHook = validateAndGetReleaseWebHookByName(webHookNameToLink);

        ReleaseWebHookAction action = getReleaseWebHookActionFromProject(project);

        if (action != null && action.getWebHookReferences() != null) {
            for (ReleaseWebHookReference webHookReference : action.getWebHookReferences()) {
                if (webHookReference.getWebHookName().equalsIgnoreCase(webHookNameToLink)) {
                    logger.fine(String.format("WebHook %s already added in the project %s", webHookNameToLink, project.getName()));
                    return;
                }
            }
        }

        ReleaseWebHookReference webHookReference = new ReleaseWebHookReference(webHookNameToLink);

        if (action == null) {
            List<ReleaseWebHookReference> webHookReferences = new ArrayList<ReleaseWebHookReference>();
            webHookReferences.add(webHookReference);

            action = new ReleaseWebHookAction(webHookReferences);
            DescribableList<Publisher, Descriptor<Publisher>> publishersList = project.getPublishersList();
            publishersList.add(action);
        } else {
            List<ReleaseWebHookReference> webHookReferences = action.getWebHookReferences();
            if (webHookReferences == null) {
                webHookReferences = new ArrayList<ReleaseWebHookReference>();
            }

            webHookReferences.add(webHookReference);
            action.setWebHookReferences(webHookReferences);

            logger.fine(String.format("WebHook %s is linked to project %s", webHookReference, project));
        }

        saveProject(project);
    }

    private void unlinkWebHook(final ReleaseWebHookResource resource) {
        if (resource == null) {
            throw new InvalidParameterException("resource is null");
        }

        AbstractProject project = validateAndGetJenkinsProject(resource);
        String webHookNameToUnlink = resource.getWebHookName();

        if (StringUtils.isBlank(webHookNameToUnlink)) {
            throw new InvalidParameterException("webhook name is empty");
        }

        ReleaseWebHookAction action = getReleaseWebHookActionFromProject(project);

        if (action == null) {
            logger.fine(String.format("Cannot find ReleaseWebHook post build action"));
            return;
        }

        List<ReleaseWebHookReference> webHookReferences = action.getWebHookReferences();
        for (ReleaseWebHookReference webHookReference : webHookReferences) {
            if (webHookReference.getWebHookName().equalsIgnoreCase(webHookNameToUnlink)) {
                logger.fine(String.format("Found webhook %s in project %s. Removing it", webHookNameToUnlink, project.getName()));

                webHookReferences.remove(webHookReference);
                if (webHookReferences.isEmpty()) {
                    // no webhook exists, removing the post build action itself
                    logger.fine(String.format("ReleaseWebHook post build action is empty, removing the post build action from project"));
                    DescribableList<Publisher, Descriptor<Publisher>> publishersList = project.getPublishersList();
                    publishersList.remove(action);
                }

                saveProject(project);
                return;
            }
        }

        logger.fine(String.format("Cannnot find webhook with the name %s in project %s", webHookNameToUnlink, project.getName()));
    }

    private ReleaseWebHookAction getReleaseWebHookActionFromProject(final AbstractProject project) {
        if (project == null) {
            return null;
        }

        ReleaseWebHookAction action = null;
        DescribableList<Publisher, Descriptor<Publisher>> publishersList = project.getPublishersList();

        for (Publisher publisher : publishersList) {
            if (publisher instanceof ReleaseWebHookAction) {
                action = (ReleaseWebHookAction) publisher;
            }
        }

        return action;
    }

    private void saveProject(final AbstractProject project) {
        if (project != null) {
            try {
                project.save();
            } catch (IOException ex) {
                throw new InternalError(String.format("Updating project %s failed with an error %s", project.getName(), ex.getMessage()));
            }
        }
    }
}
