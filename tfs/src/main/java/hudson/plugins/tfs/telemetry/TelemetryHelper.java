package hudson.plugins.tfs.telemetry;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.extensibility.ContextInitializer;
import com.microsoft.applicationinsights.internal.logger.InternalLogger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The TelemetryHelper class is a singleton that allows the plugin to capture
 * telemetry data when the user initiates events.
 */
public class TelemetryHelper {
    public static final String UNKNOWN = "unknown";
    private static final String UNIQUE_PREFIX = "ai-log";
    private static final String BASE_FOLDER = "AppInsights";

    private static final String PROPERTY_VSTS_IS_HOSTED = "VSTS.TeamFoundationServer.IsHostedServer";
    private static final String PROPERTY_VSTS_SERVER_ID = "VSTS.TeamFoundationServer.ServerId";
    private static final String PROPERTY_VSTS_COLLECTION_ID = "VSTS.TeamFoundationServer.CollectionId";

    private static final String ACTION_NAME_FORMAT = "Action/%s";

    private static final Logger logger = LoggerFactory.getLogger(TelemetryHelper.class);

    // Instance members
    private TelemetryClient telemetryClient;

    // A private static class to allow safe lazy initialization of the singleton
    private static class TfsTelemetryHelperHolder {
        private static final TelemetryHelper INSTANCE = new TelemetryHelper();
    }

    /**
     * The getInstance method returns the singleton instance. Creating it if necessary
     */
    private static TelemetryHelper getInstance() {
        // Using the Initialization-on-demand holder pattern to make sure this is thread-safe
        return TfsTelemetryHelperHolder.INSTANCE;
    }

    // The private constructor keeps the class from being inherited or misused
    private TelemetryHelper() {
        final String skip = System.getProperties().getProperty("hudson.plugins.tfs.telemetry.skipClientInitialization");
        if (StringUtils.isNotEmpty(skip) && StringUtils.equalsIgnoreCase(skip, "true")) {
            // this flag is here for testing purposes in which case we do not want to create a telemetry channel
            // or client.
            return;
        }

        // Initialize the internal logger
        final Map<String, String> loggerData = new HashMap<String, String>();
        loggerData.put("Level", InternalLogger.LoggingLevel.ERROR.toString());
        loggerData.put("UniquePrefix", UNIQUE_PREFIX);
        loggerData.put("BaseFolder", BASE_FOLDER);
        InternalLogger.INSTANCE.initialize(InternalLogger.LoggerOutputType.FILE.toString(), loggerData);

        // Initialize the active TelemetryConfiguration
        final String isDeveloperModeProperty = System.getProperty("hudson.plugins.tfs.telemetry.isDeveloperMode", "false");
        boolean isDeveloperMode = StringUtils.equalsIgnoreCase(isDeveloperModeProperty, "true");
        ContextInitializer initializer = new TelemetryContextInitializer(isDeveloperMode);
        TelemetryConfiguration.getActive().getContextInitializers().add(initializer);

        // Create a channel to AppInsights
        final TelemetryChannel channel = TelemetryConfiguration.getActive().getChannel();
        if (channel != null) {
            channel.setDeveloperMode(isDeveloperMode);
        } else {
            logger.error("Failed to load telemetry channel");
            return;
        }

        logger.debug("AppInsights telemetry initialized");
        logger.debug("    Developer Mode: ", channel.isDeveloperMode());

        // Create the telemetry client and cache it for later use
        telemetryClient = new TelemetryClient();
    }

    /**
     * Call sendEvent to track an occurrence of a named event.
     *
     * @param name       is the name of the event to be tracked.
     * @param properties are additional properties to track with the event.
     */
    public static void sendEvent(final String name, final Map<String, String> properties) {
        try {
            getInstance().sendEventInternal(name, properties);
        } catch (Exception e) {
            logger.warn("Error sending event telemetry", e);
        }
    }

    /**
     * Call sendMetric to track the new value of the named metric.
     *
     * @param name  is the name of the metric to be tracked.
     * @param value is the current value of the metric as a double.
     */
    public static void sendMetric(final String name, final double value) {
        try {
            getInstance().sendMetricInternal(name, value);
        } catch (Exception e) {
            logger.warn("Error sending metric telemetry", e);
        }
    }

    /**
     * Call sendException to track an exception that occurred that should be tracked.
     *
     * @param exception is the exception to track.
     */
    public static void sendException(final Exception exception, final Map<String, String> properties) {
        try {
            getInstance().sendExceptionInternal(exception, properties);
        } catch (Exception e) {
            logger.warn("Error sending exception telemetry", e);
        }
    }

    protected void sendMetricInternal(final String name, final double value) {
        // Log that the event occurred (this log is used in testing)
        logger.debug(String.format("sendMetric(%s, %f)", name, value));

        if (telemetryClient != null) {
            telemetryClient.trackMetric(name, value);
        }
    }

    protected void sendEventInternal(final String name, final Map<String, String> properties) {
        final String eventName = String.format(ACTION_NAME_FORMAT, name);
        final PropertyMapBuilder builder = new PropertyMapBuilder(properties);

        // Log that the event occurred (this log is used in testing)
        logger.debug(String.format("sendEvent(%s, %s)", name, builder.toString()));

        if (telemetryClient != null) {
            telemetryClient.trackEvent(eventName, builder.build(), null);
        }
    }

    protected void sendExceptionInternal(final Exception exception, final Map<String, String> properties) {
        final PropertyMapBuilder builder = new PropertyMapBuilder(properties);

        // Log that the event occurred (this log is used in testing)
        logger.debug(String.format("sendException(%s, %s)", exception.getMessage(), builder.toString()));

        if (telemetryClient != null) {
            telemetryClient.trackException(exception, builder.build(), null);
        }
    }

    public static class PropertyMapBuilder {
        public static final Map<String, String> EMPTY = new PropertyMapBuilder().build();

        private Map<String, String> properties = new HashMap<String, String>();

        public PropertyMapBuilder() {
            this(null);
        }

        public PropertyMapBuilder(final Map<String, String> properties) {
            if (properties != null) {
                this.properties = new HashMap<String, String>(properties);
            } else {
                this.properties = new HashMap<String, String>();
            }
        }

        public Map<String, String> build() {
            // Make a copy and return it
            return new HashMap<String, String>(properties);
        }

        public PropertyMapBuilder serverContext(final String serverUrl, final String collectionUrl) {
            if (serverUrl != null) {
                final boolean isHosted = StringUtils.containsIgnoreCase(serverUrl, ".visualstudio.com");
                add(PROPERTY_VSTS_IS_HOSTED, Boolean.toString(isHosted));
                add(PROPERTY_VSTS_SERVER_ID, getServerId(serverUrl));
                add(PROPERTY_VSTS_COLLECTION_ID, getCollectionId(collectionUrl));
            }
            return this;
        }

        public PropertyMapBuilder pair(final String key, final String value) {
            if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
                add(key, value);
            }
            return this;
        }

        private String getServerId(final String serverUrl) {
            try {
                if (serverUrl != null) {
                    return URI.create(serverUrl).getHost();
                }
            } catch(Exception ex) {
                logger.error("failed to get server URI", ex);
            }
            return UNKNOWN;
        }

        private String getCollectionId(final String collectionUrl) {
            try {
                if (collectionUrl != null) {
                    return URI.create(collectionUrl).getPath();
                }
            } catch(Exception ex) {
                logger.error("failed to get server URI", ex);
            }

            return UNKNOWN;
        }

        private void add(final String key, final String value) {
            if (value != null) {
                // remove any newlines from the value field. Newlines currently cause the event to be lost in AppInsights
                properties.put(key, value.replace("\r", "").replace("\n", " "));
            } else {
                properties.put(key, ""); //null values cause exceptions
            }
        }

        @Override
        public String toString() {
            return properties.toString();
        }
    }
}
