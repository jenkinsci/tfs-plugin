package hudson.plugins.tfs.commands;

import java.io.PrintStream;

import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.events.GetEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.GetListener;
import com.microsoft.tfs.core.clients.versioncontrol.events.VersionControlEventEngine;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;

public class GetFilesToWorkFolderCommand extends AbstractCallableCommand<Void, Exception> implements GetListener {

    private static final String GettingTemplate = "Getting version '%s' to '%s'...";
    private static final String GotTemplate = "Finished getting version '%s'. Retrieved %d resources.";

    private final String workFolder;
    private final String versionSpec;
    private final boolean shouldLogEachGet;
    private PrintStream logger;
    private int getCount = 0;

    public GetFilesToWorkFolderCommand(final ServerConfigurationProvider server, final String workFolder, final String versionSpec) {
        this(server, workFolder, versionSpec, false);
        // using shouldLogEachGet false as default, could be controlled by a config option at a later stage if desired, just adds noise to log though
    }

    public GetFilesToWorkFolderCommand(final ServerConfigurationProvider server, final String workFolder, final String versionSpec, 
        final boolean shouldLogEachGet) {
        super(server);
        this.workFolder = workFolder;
        this.versionSpec = versionSpec;
        this.shouldLogEachGet = shouldLogEachGet;
    }

    @Override
    public Callable<Void, Exception> getCallable() {
        return this;
    }

    void setLogger(final PrintStream logger) {
        this.logger = logger;
    }

    public Void call() throws Exception {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TaskListener listener = server.getListener();
        logger = listener.getLogger();

        final VersionSpec getVersionSpec;
        if (versionSpec != null) {
            getVersionSpec = VersionSpec.parseSingleVersionFromSpec(versionSpec, null);
        } else {
            getVersionSpec = LatestVersionSpec.INSTANCE;
        }
        final String versionSpecString = RemoteChangesetVersionCommand.toString(getVersionSpec);
        final String gettingMessage = String.format(GettingTemplate, versionSpecString, workFolder);
        logger.println(gettingMessage);

        final Workspace workspace = vcc.getWorkspace(workFolder);
        final VersionControlEventEngine eventEngine = vcc.getEventEngine();
        eventEngine.addGetListener(this);
        workspace.get(getVersionSpec, GetOptions.NONE);
        eventEngine.removeGetListener(this);

        final String gotMessage = String.format(GotTemplate, versionSpecString, getCount);
        logger.println(gotMessage);

        return null;
    }

    public void onGet(final GetEvent getEvent) {
        getCount++;
        if (shouldLogEachGet) {
            logger.println(getEvent.getTargetLocalItem());
        }
    }

}
