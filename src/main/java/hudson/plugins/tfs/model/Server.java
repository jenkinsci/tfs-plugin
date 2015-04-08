package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.TFSConfigurationServer;
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

    public Server(TfTool tool, String url, String username, String password) {
        this.tool = tool;
        this.url = url;
        this.userName = username;
        this.userPassword = password;
        final URI uri = URIUtils.newURI(url);

        ensureNativeLibrariesConfigured();

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

    static synchronized void ensureNativeLibrariesConfigured() {
        final String nativeFolder = System.getProperty(nativeFolderPropertyName);
        if (nativeFolder == null) {
            final Class<TFSTeamProjectCollection> metaclass = TFSTeamProjectCollection.class;
            final ProtectionDomain protectionDomain = metaclass.getProtectionDomain();
            final CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null) {
                // TODO: log that we were unable to determine the codeSource
                return;
            }
            final URL location = codeSource.getLocation();
            // inspired by http://hg.netbeans.org/main/file/default/openide.filesystems/src/org/openide/filesystems/FileUtil.java#l1992
            final String u = location.toString();
            URI locationUri;
            if (u.startsWith("jar:file:") && u.endsWith("!/")) {
                locationUri = URI.create(u.substring(4, u.length() - 2));
            }
            else if (u.startsWith("file:")) {
                locationUri = URI.create(u);
            }
            else {
                // TODO: log that we were unable to determine location from codeSource
                return;
            }
            final File pathToJar = new File(locationUri);
            final File pathToLibFolder = pathToJar.getParentFile();
            final File pathToNativeFolder = new File(pathToLibFolder, "native");
            System.setProperty(nativeFolderPropertyName, pathToNativeFolder.toString()); 
        }
    }

    Server(String url) {
        this(null, url, null, null);
    }

    public Project getProject(String projectPath) {
        if (! projects.containsKey(projectPath)) {
            projects.put(projectPath, new Project(this, projectPath));
        }
        return projects.get(projectPath);
    }
    
    public TFSTeamProjectCollection getTeamProjectCollection()
    {
        return this.tpc;
    }
    
    public Workspaces getWorkspaces() {
        if (workspaces == null) {
            workspaces = new Workspaces(this);
        }
        return workspaces;
    }
    
    public Reader execute(MaskedArgumentListBuilder arguments) throws IOException, InterruptedException {
        return tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
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

    public String getLocalHostname() throws IOException, InterruptedException {
        return tool.getHostname();
    }

    public synchronized void close() {
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
}
