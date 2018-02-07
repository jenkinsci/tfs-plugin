package hudson.plugins.tfs.telemetry;

import com.microsoft.applicationinsights.extensibility.ContextInitializer;
import com.microsoft.applicationinsights.extensibility.context.ComponentContext;
import com.microsoft.applicationinsights.extensibility.context.ContextTagKeys;
import com.microsoft.applicationinsights.extensibility.context.DeviceContext;
import com.microsoft.applicationinsights.extensibility.context.SessionContext;
import com.microsoft.applicationinsights.extensibility.context.UserContext;
import com.microsoft.applicationinsights.telemetry.TelemetryContext;
import jenkins.model.Jenkins;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * This class is provided as a ContextInitializer for the application insights TelemetryClient.
 */
public class TelemetryContextInitializer implements ContextInitializer {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryContextInitializer.class);

    private static final String PRODUCT_NAME = "TFS-Jenkins";
    private static final String SYS_PROP_OS_NAME = "os.name";
    private static final String SYS_PROP_OS_VERSION = "os.version";
    private static final String SYS_PROP_USER_NAME = "user.name";
    private static final String SYS_PROP_JAVA_RUNTIME = "java.runtime.name";
    private static final String SYS_PROP_JAVA_VERSION = "java.version";
    private static final String USER_AGENT_FORMAT = "{0}/{1} {2}/{3} {4}/{5} {6}/{7} ({8})";

    private static final String PROPERTY_USER_ID = "User.Id";
    private static final String PROPERTY_JENKINS_VERSION = "Jenkins.Version";
    private static final String PROPERTY_PLUGIN_VERSION = "Plugin.Version";
    private static final String PROPERTY_JAVA_NAME = "Java.Name";
    private static final String PROPERTY_JAVA_VERSION = "Java.Version";
    private static final String PROPERTY_OS_PLATFORM = "VSTS.Core.Machine.OS.Platform";
    private static final String PROPERTY_OS_VERSION = "VSTS.Core.Machine.OS.Version";
    private static final String PROPERTY_LOCALE = "Locale";

    private String hostname = StringUtils.EMPTY;
    private boolean isInitialized = false;
    private final boolean isDeveloperMode;

    public TelemetryContextInitializer(final boolean isDeveloperMode) {
        this.isDeveloperMode = isDeveloperMode;
    }

    @Override
    public void initialize(final TelemetryContext context) {
        if (!isInitialized) {
            logger.info("Starting TelemetryContext initialization");
            initializeInstrumentationKey(context, isDeveloperMode);
            initializeProperties(context.getProperties());
            initializeUser(context.getUser());
            initializeComponent(context.getComponent());
            initializeDevice(context.getDevice());
            initializeTags(context.getTags());
            initializeSession(context.getSession());
            isInitialized = true;
            logger.info("Ending TelemetryContext initialization");
        }
    }

    /**
     * Gets the full User Agent used to make requests to TFS/Team Services.
     */
    public String getUserAgent(final String defaultUserAgent) {
        try {
            return MessageFormat.format(USER_AGENT_FORMAT,
                    PRODUCT_NAME,
                    getPluginVersion(),
                    "Jenkins",
                    Jenkins.getVersion(),
                    getPlatformName(),
                    getPlatformVersion(),
                    getJavaName(),
                    getJavaVersion(),
                    defaultUserAgent);
        } catch (final Throwable t) {
            logger.warn("Error getting UserAgent", t);
            return defaultUserAgent;
        }
    }

    private void initializeDevice(final DeviceContext device) {
        device.setOperatingSystem(getPlatformName());
        device.setOperatingSystemVersion(getPlatformVersion());
    }

    private void initializeInstrumentationKey(final TelemetryContext context, final boolean isDeveloperMode) {
        if (isDeveloperMode) {
            context.setInstrumentationKey("149da81b-a0ab-4bdf-a7e9-11e5af9e39bd");
        } else {
            context.setInstrumentationKey("0f243a28-b3c3-41f2-b7cc-d10feec45a81");
        }
    }

    private void initializeUser(final UserContext user) {
        user.setId(getUserId());
        user.setUserAgent(getUserAgent(""));
    }

    private String getUserId() {
        final String computerName = getComputerName();
        final String userName = getSystemProperty(SYS_PROP_USER_NAME);
        final String fakeUserId = MessageFormat.format("{0}@{1}", userName, computerName);

        //FIXME: was sha1Hex, but this is available only in commons codec 1.7. TFS SDK 14.0.3 bundles older version
        return DigestUtils.shaHex(fakeUserId);
    }

    private String getComputerName() {
        if (StringUtils.isEmpty(hostname)) {
            hostname = TelemetryHelper.UNKNOWN;

            try {
                // on Mac this call can take > 10 secs so don't call multiple times
                final InetAddress address = InetAddress.getLocalHost();
                hostname = address.getHostName();
            } catch (UnknownHostException ex) {
                // This case is covered by the initial value of hostname above
            }
        }

        return hostname;
    }

    private void initializeComponent(final ComponentContext component) {
        component.setVersion(getPluginVersion());
    }

    private void initializeTags(final Map<String, String> tags) {
        tags.put(ContextTagKeys.getKeys().getApplicationId(), PRODUCT_NAME);
        tags.put(ContextTagKeys.getKeys().getDeviceOS(), getPlatformName());
        tags.put(ContextTagKeys.getKeys().getDeviceOSVersion(), getPlatformVersion());
    }

    private void initializeSession(final SessionContext sessionContext) {
        sessionContext.setId(UUID.randomUUID().toString());
    }

    private void initializeProperties(final Map<String, String> properties) {
        properties.put(PROPERTY_USER_ID, getUserId());

        // Get Jenkins version info
        properties.put(PROPERTY_JENKINS_VERSION, Jenkins.getVersion().toString());
        properties.put(PROPERTY_PLUGIN_VERSION, getPluginVersion());

        // Get OS info
        properties.put(PROPERTY_LOCALE, getLocaleName());
        properties.put(PROPERTY_OS_PLATFORM, getPlatformName());
        properties.put(PROPERTY_OS_VERSION, getPlatformVersion());

        // Get Java info
        properties.put(PROPERTY_JAVA_NAME, getJavaName());
        properties.put(PROPERTY_JAVA_VERSION, getJavaVersion());
    }

    private String getSystemProperty(final String propertyName) {
        return System.getProperty(propertyName, StringUtils.EMPTY);
    }

    private String getPlatformName() {
        return getSystemProperty(SYS_PROP_OS_NAME);
    }

    private String getPlatformVersion() {
        return getSystemProperty(SYS_PROP_OS_VERSION);
    }

    private String getLocaleName() {
        return Locale.getDefault().getDisplayName();
    }

    private String getJavaName() {
        return getSystemProperty(SYS_PROP_JAVA_RUNTIME);
    }

    private String getJavaVersion() {
        return getSystemProperty(SYS_PROP_JAVA_VERSION);
    }

    private String getPluginVersion() {
        final Jenkins instance = Jenkins.getInstance();
        if (instance != null && instance.getPluginManager() != null) {
            return instance.getPluginManager().getPlugin("tfs").getVersion();
        }
        return StringUtils.EMPTY;
    }
}
