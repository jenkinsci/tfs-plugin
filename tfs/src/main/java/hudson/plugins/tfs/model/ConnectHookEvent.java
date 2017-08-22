package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import hudson.plugins.tfs.model.servicehooks.Event;
import net.sf.json.JSONObject;

import java.net.URI;

/**
 * This HookEvent is for the TeamEventsEndpoint "Connect" event.
 */
public class ConnectHookEvent extends AbstractHookEvent {

    /**
     * Factory to create ConnectHookEvent.
     */
    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public AbstractHookEvent create() {
            return new ConnectHookEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n"
                 + "    \"eventType\": \"connect\",\n"
                 + "    \"resource\":\n"
                 + "    {\n"
                 + "        \"teamCollectionUrl\": \"https://xplatalm.visualstudio.com\"\n"
                 + "        \"connectionKey\": \"MyJenkinsServer\"\n"
                 + "        \"connectionSignature\": \"ABC13ABC123ABC13ABC123ABC13ABC123\"\n"
                 + "        \"sendJobCompletionEvents\": true\n"
                 + "    }\n"
                 + "}";
        }
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage) {
        final Object resource = serviceHookEvent.getResource();
        final ConnectionParameters parameters = mapper.convertValue(resource, ConnectionParameters.class);

        //TODO permissions?

        // Store the key and the information for the correct collection
        final TeamCollectionConfiguration collection = TeamCollectionConfiguration.findCollection(URI.create(parameters.getTeamCollectionUrl()));
        if (collection != null) {
            // Store key and signature with collection
            collection.getConnectionParameters().setConnectionKey(parameters.getConnectionKey());
            collection.getConnectionParameters().setConnectionSignature(parameters.getConnectionSignature());
            collection.getConnectionParameters().setSendJobCompletionEvents(parameters.isSendJobCompletionEvents());
            collection.getConnectionParameters().setTeamCollectionUrl(parameters.getTeamCollectionUrl());

            // Save collection info
            final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
            config.save();
        } else {
            throw new IllegalArgumentException("Unable to connect to unknown server: " + parameters.getTeamCollectionUrl());
        }
        return JSONObject.fromObject(serviceHookEvent);
    }
}
