package hudson.plugins.tfs.model;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.microsoft.tfs.core.TFSConfigurationServer;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementException;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementService;
import com.microsoft.tfs.core.config.persistence.DefaultPersistenceStoreProvider;
import com.microsoft.tfs.core.config.persistence.PersistenceStoreProvider;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import com.microsoft.tfs.jni.helpers.LocalHost;
import com.microsoft.tfs.util.Closable;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import hudson.plugins.tfs.commands.ServerConfigurationProvider;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.util.Secret;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Server implements ServerConfigurationProvider, Closable {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final String nativeFolderPropertyName = "com.microsoft.tfs.jni.native.base-directory";
    private final String url;
    private final String userName;
    private final String userPassword;
    private Workspaces workspaces;
    private Map<String, Project> projects = new HashMap<String, Project>();
    private final Launcher launcher;
    private final TaskListener taskListener;
    private final TFSTeamProjectCollection tpc;
    private final WebProxySettings webProxySettings;
    private final ExtraSettings extraSettings;
    private MockableVersionControlClient mockableVcc;

    /**
     * This constructor overload assumes a Jenkins instance is present.
     */
    public Server(final Launcher launcher, final TaskListener taskListener, final String url, final String username, final String password) throws IOException {
        this(launcher, taskListener, url, username, password, null, null);
    }

    public static Server create(final Launcher launcher, final TaskListener taskListener, final String url, final StandardUsernamePasswordCredentials credentials, final WebProxySettings webProxySettings, final ExtraSettings extraSettings) throws IOException {

        final String username;
        final String userPassword;
        if (credentials == null) {
            username = null;
            userPassword = null;
        }
        else {
            username = credentials.getUsername();
            final Secret password = credentials.getPassword();
            userPassword = password.getPlainText();
        }
        return new Server(launcher, taskListener, url, username, userPassword, webProxySettings, extraSettings);
    }

    public Server(final Launcher launcher, final TaskListener taskListener, final String url, final String username, final String password, final WebProxySettings webProxySettings, final ExtraSettings extraSettings) throws IOException {
        this.launcher = launcher;
        this.taskListener = taskListener;
        this.url = url;
        this.userName = username;
        this.userPassword = password;
        final URI uri = URIUtils.newURI(url);

        NativeLibraryManager.initialize();

        Credentials credentials = null;
        // In case no user name is provided and the current platform supports
        // default credentials, use default credentials
        if ((username == null || username.length() == 0) && CredentialsUtils.supportsDefaultCredentials()) {
            credentials = new DefaultNTCredentials();
        }
        else if (username != null && password != null) {
            credentials = new UsernamePasswordCredentials(username, password);
        }

        if (credentials != null) {
            final VirtualChannel channel = launcher != null ? launcher.getChannel() : null;
            if (webProxySettings != null) {
                this.webProxySettings = webProxySettings;
            }
            else {
                final ProxyConfiguration proxyConfiguration = determineProxyConfiguration(channel);
                this.webProxySettings = new WebProxySettings(proxyConfiguration);
            }
            final String host = uri.getHost();
            final ProxyHostEx proxyHost = this.webProxySettings.toProxyHost(host);

            if (extraSettings != null) {
                this.extraSettings = extraSettings;
            }
            else {
                final TeamPluginGlobalConfig globalConfig = determineGlobalConfig(channel);
                this.extraSettings = new ExtraSettings(globalConfig);
            }
            final PersistenceStoreProvider defaultProvider = DefaultPersistenceStoreProvider.INSTANCE;
            final PersistenceStoreProvider provider;
            if (this.extraSettings.isConfigFolderPerNode()) {
                final String hostName = LocalHost.getShortName();
                provider = new ClonePersistenceStoreProvider(defaultProvider, hostName);
            }
            else {
                provider = defaultProvider;
            }
            final ModernConnectionAdvisor advisor = new ModernConnectionAdvisor(proxyHost, provider);
            this.tpc = new TFSTeamProjectCollection(uri, credentials, advisor);
        }
        else {
            this.webProxySettings = null;
            this.extraSettings = null;
            this.tpc = null;
        }
    }

    static TeamPluginGlobalConfig determineGlobalConfig(final VirtualChannel channel) {
        final Jenkins jenkins = Jenkins.getInstance();
        final TeamPluginGlobalConfig result;
        if (jenkins == null) {
            if (channel != null) {
                try {
                    result = channel.call(new Callable<TeamPluginGlobalConfig, Throwable>() {
                        @Override
                        public TeamPluginGlobalConfig call() throws Throwable {
                            final Jenkins jenkins = Jenkins.getInstance();
                            final TeamPluginGlobalConfig result = jenkins != null ? TeamPluginGlobalConfig.get() : null;
                            return result;
                        }
                    });
                }
                catch (final Throwable throwable) {
                    throw new Error(throwable);
                }
            }
            else {
                result = TeamPluginGlobalConfig.DEFAULT_CONFIG;
            }
        }
        else {
            result = TeamPluginGlobalConfig.get();
        }
        return result;
    }

    static ProxyConfiguration determineProxyConfiguration(final VirtualChannel channel) {
        final Jenkins jenkins = Jenkins.getInstance();
        final ProxyConfiguration proxyConfiguration;
        if (jenkins == null) {
            if (channel != null) {
                try {
                    proxyConfiguration = channel.call(new Callable<ProxyConfiguration, Throwable>() {
                        public ProxyConfiguration call() throws Throwable {
                            final Jenkins jenkins = Jenkins.getInstance();
                            final ProxyConfiguration result = jenkins != null ? jenkins.proxy : null;
                            return result;
                        }
                    });
                } catch (final Throwable throwable) {
                    throw new Error(throwable);
                }
            }
            else {
                proxyConfiguration = null;
            }
        }
        else {
            proxyConfiguration = jenkins.proxy;
        }
        return proxyConfiguration;
    }

    public Project getProject(String projectPath) {
        if (! projects.containsKey(projectPath)) {
            projects.put(projectPath, new Project(this, projectPath));
        }
        return projects.get(projectPath);
    }

    public Workspaces getWorkspaces() {
        if (workspaces == null) {
            workspaces = new Workspaces(this);
        }
        return workspaces;
    }

    public MockableVersionControlClient getVersionControlClient() {
        if (mockableVcc == null) {
            synchronized (this) {
                if (mockableVcc == null) {
                    final VersionControlClient vcc = tpc.getVersionControlClient();
                    mockableVcc = new MockableVersionControlClient(vcc);
                }
            }
        }
        return mockableVcc;
    }

    public <T, E extends Exception> T execute(final Callable<T, E> callable) {
        try {
            final VirtualChannel channel = launcher.getChannel();
            final T result = channel.call(callable);
            return result;
        } catch (final Exception e) {
            // convert from checked to unchecked exception
            throw new RuntimeException(e);
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public Launcher getLauncher() {
        return launcher;
    }

    public WebProxySettings getWebProxySettings() {
        return webProxySettings;
    }

    public ExtraSettings getExtraSettings() {
        return extraSettings;
    }

    public TaskListener getListener() {
        return taskListener;
    }

    public synchronized void close() {
        if (this.mockableVcc != null) {
            this.mockableVcc.close();
        }
        if (this.tpc != null) {
            // Close the configuration server connection that should be closed by
            // TFSTeamProjectCollection
            // The field is private, so use reflection
            // This should be removed when the TFS SDK is fixed
            // Post in MSDN forum: social.msdn.microsoft.com/Forums/vstudio/en-US/79985ef1-b35d-4fc5-af0b-b95e28402b83
            try {
                Field f = TFSTeamProjectCollection.class.getDeclaredField("configurationServer");
                f.setAccessible(true);
                TFSConfigurationServer configurationServer = (TFSConfigurationServer) f.get(this.tpc);
                if (configurationServer != null) {
                    configurationServer.close();
                }
                f.setAccessible(false);
            } catch (NoSuchFieldException ignore) {
            } catch (IllegalAccessException ignore) {
            }
            this.tpc.close();
        }
    }

    public IIdentityManagementService createIdentityManagementService() {
        IIdentityManagementService ims;
        try {
            ims = new IdentityManagementService(tpc);
        } catch (IdentityManagementException e) {
            ims = new LegacyIdentityManagementService();
        }
        return ims;
    }
}
