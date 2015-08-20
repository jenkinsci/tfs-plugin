package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import hudson.Util;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.util.TextTableParser;
import hudson.remoting.Callable;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ListWorkspacesCommand extends AbstractCallableCommand implements Callable<List<Workspace>, Exception> {

    private static final String ListingWorkspacesTemplate = "Listing workspaces from %s...";

    private final WorkspaceFactory factory;
    private final String computer;

    public interface WorkspaceFactory {
        Workspace createWorkspace(String name, String computer, String owner, String comment);
    }
    
    public ListWorkspacesCommand(final WorkspaceFactory factory, final ServerConfigurationProvider server) {
        this(factory, server, null);
    }

    public ListWorkspacesCommand(final WorkspaceFactory factory, final ServerConfigurationProvider server, final String computer) {
        super(server);
        this.computer = computer;
        this.factory = factory;
    }

    @Override
    public Callable<List<Workspace>, Exception> getCallable() {
        return this;
    }

    public List<Workspace> call() throws Exception {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();

        final String listWorkspacesMessage = String.format(ListingWorkspacesTemplate, server.getUrl());
        logger.println(listWorkspacesMessage);

        final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace[] sdkWorkspaces
                = vcc.queryWorkspaces(
                null,
                null,
                Util.fixEmpty(computer),
                WorkspacePermissions.NONE_OR_NOT_SUPPORTED
        );

        final List<Workspace> result = new ArrayList<Workspace>(sdkWorkspaces.length);
        for (final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace sdkWorkspace : sdkWorkspaces) {
            final String name = sdkWorkspace.getName();
            final String computer = sdkWorkspace.getComputer();
            final String ownerName = sdkWorkspace.getOwnerName();
            final String comment = Util.fixNull(sdkWorkspace.getComment());

            final Workspace workspace = new Workspace(
                    name,
                    computer,
                    ownerName,
                    comment);
            result.add(workspace);
        }

        log(result, logger);

        return result;
    }

    public List<Workspace> parse(Reader consoleReader) throws IOException {
        List<Workspace> list = new ArrayList<Workspace>();
        
        TextTableParser parser = new TextTableParser(consoleReader, 1);
        while (parser.nextRow()) {
            Workspace workspace = new Workspace(
                parser.getColumn(0), 
                parser.getColumn(2),
                parser.getColumn(1),
                Util.fixNull(parser.getColumn(3)));
            list.add(workspace);            
        }
        return list;
    }

    static void log(final List<Workspace> workspaces, final PrintStream logger) {
        int maxName = "Workspace".length();
        int maxOwner = "Owner".length();
        int maxComputer = "Computer".length();
        int maxComment = "Comment".length();
        for (final Workspace workspace : workspaces) {
            final String name = workspace.getName();
            maxName = Math.max(maxName, name.length());
            final String ownerName = workspace.getOwner();
            maxOwner = Math.max(maxOwner, ownerName.length());
            final String computer = workspace.getComputer();
            maxComputer = Math.max(maxComputer, computer.length());
            final String comment = workspace.getComment();
            maxComment = Math.max(maxComment, comment.length());
        }
        final String template =
                "%1$-" + maxName + "s %2$-" + maxOwner + "s %3$-" + maxComputer + "s %4$-" + maxComment + "s";
        final String header = String.format(template, "Workspace", "Owner", "Computer", "Comment");
        logger.println(header);
        final String divider = String.format(template,
                StringUtils.repeat("-", maxName),
                StringUtils.repeat("-", maxOwner),
                StringUtils.repeat("-", maxComputer),
                StringUtils.repeat("-", maxComment));
        logger.println(divider);

        for (final Workspace workspace : workspaces) {
            final String name = workspace.getName();
            final String ownerName = workspace.getOwner();
            final String computer = workspace.getComputer();
            final String comment = workspace.getComment();
            final String line = String.format(template, name, ownerName, computer, comment);
            logger.println(line);
        }
    }
}
