package hudson.plugins.tfs.commands;

import java.io.PrintStream;
import java.util.concurrent.Callable;

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

public class GetFilesToWorkFolderCommand extends AbstractCallableCommand implements Callable<Void>, GetListener {

    private static final String GettingTemplate = "Getting version '%s' to '%s'...";
    private static final String GotTemplate = "Finished getting version '%s'.";

    private final String workFolder;
    private final String versionSpec;
    private PrintStream logger;

    public GetFilesToWorkFolderCommand(final Server server, final String workFolder, final String versionSpec) {
        super(server);
        this.workFolder = workFolder;
        this.versionSpec = versionSpec;
    }

    @Override
    public Callable<Void> getCallable() {
        return this;
    }

    void setLogger(final PrintStream logger) {
        this.logger = logger;
    }

    public Void call() throws Exception {
        final Server server = getServer();
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
        //final Workspace workspace = vcc.queryWorkspace(workspaceName, server.getUserName());
        final VersionControlEventEngine eventEngine = vcc.getEventEngine();
        eventEngine.addGetListener(this);
        workspace.get(getVersionSpec, GetOptions.NONE);
        // TODO: (PR #34) throw an exception if not all files could be fetched, there were conflicts, etc.
        eventEngine.removeGetListener(this);

        final String gotMessage = String.format(GotTemplate, versionSpecString);
        logger.println(gotMessage);

        return null;
    }

    public void onGet(final GetEvent getEvent) {
        // TODO: The CLC used to emit folder paths as headings, then files within; should we do that?
        logger.println(getEvent.getTargetLocalItem());
    }

}
