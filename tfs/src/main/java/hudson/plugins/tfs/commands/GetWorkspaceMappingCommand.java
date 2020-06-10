//CHECKSTYLE:OFF
package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;

import java.io.PrintStream;

public class GetWorkspaceMappingCommand extends AbstractCallableCommand<String, Exception> {

    private static final String CheckingMappingTemplate = "Checking if there exists a mapping for %s...";
    private static final String FoundResultTemplate = "yes, in workspace '%s'.";

    private final String localPath;

    public GetWorkspaceMappingCommand(final ServerConfigurationProvider serverConfig, final String localPath) {
        super(serverConfig);
        this.localPath = localPath;
    }

    @Override
    public Callable<String, Exception> getCallable() {
        return this;
    }

    @Override
    public String call() throws Exception {
        final Server server = createServer();
        try {
            final MockableVersionControlClient vcc = server.getVersionControlClient();
            final TFSTeamProjectCollection connection = vcc.getConnection();
            updateCache(connection);
            final TaskListener listener = server.getListener();
            final PrintStream logger = listener.getLogger();

            final String checkingMessage = String.format(CheckingMappingTemplate, localPath);
            logger.print(checkingMessage);

            final Workspace workspace = vcc.tryGetWorkspace(localPath);
            final boolean existsMapping = workspace != null;
            final String result = existsMapping ? workspace.getName() : null;

            final String resultMessage = existsMapping ? String.format(FoundResultTemplate, result) : "no.";
            logger.println(resultMessage);

            return result;
        } finally {
            server.close();
        }
    }

}
