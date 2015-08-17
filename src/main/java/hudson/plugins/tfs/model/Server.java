package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.TFSConfigurationServer;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementException;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementService;
import hudson.model.TaskListener;
import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.commands.ServerConfigurationProvider;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import com.microsoft.tfs.util.Closable;

public class Server implements ServerConfigurationProvider, Closable {
    
    private static final String nativeFolderPropertyName = "com.microsoft.tfs.jni.native.base-directory";
    private final String url;
    private final String userName;
    private final String userPassword;
    private Workspaces workspaces;
    private Map<String, Project> projects = new HashMap<String, Project>();
    private final TfTool tool;
    private final TFSTeamProjectCollection tpc;
    private MockableVersionControlClient mockableVcc;

    public Server(TfTool tool, String url, String username, String password) throws IOException {
        this.tool = tool;
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
            this.tpc = new TFSTeamProjectCollection(uri, credentials);
        }
        else {
            this.tpc = null;
        }
    }

    Server(String url) throws IOException {
        this(null, url, null, null);
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

    public Reader execute(MaskedArgumentListBuilder arguments) throws IOException, InterruptedException {
        return tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
    }

    public <T> T execute(final Callable<T> callable) {
        try {
            return callable.call();
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

    public TaskListener getListener() {
        // TODO: rip out TfTool and accept the TaskListener in our constructor
        return tool.getListener();
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
