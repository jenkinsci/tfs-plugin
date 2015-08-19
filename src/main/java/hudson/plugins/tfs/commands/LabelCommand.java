package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LabelChildOption;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LabelResult;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.VersionControlLabel;
import com.microsoft.tfs.core.clients.versioncontrol.specs.ItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.WorkspaceVersionSpec;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;

import java.io.PrintStream;

/**
 * Command to create a label on TFS.
 * @author Rodrigo Lopes (rodrigolopes)
 */
public class LabelCommand extends AbstractCallableCommand implements Callable<Void, Exception> {

    private static final String CreatingTemplate = "Creating label '%s' on '%s' as of the current version in workspace '%s'...";
    private static final String CreatedTemplate = "Created label '%s'.";

    private final String labelName;
    private final String workspaceName;
    private final String projectPath;

    public LabelCommand(final ServerConfigurationProvider configurationProvider,
                        final String labelName,
                        final String workspaceName,
                        final String projectPath) {
        super(configurationProvider);
        this.labelName = labelName;
        this.workspaceName = workspaceName;
        this.projectPath = projectPath;
    }

    private String getLabelComment() {
        // TODO 1. Solve issue with quotes and spaces
        // TODO 2. Include build information in the comment.
        return "Automatically_applied_by_Jenkins_TFS_plugin";
    }

    @Override
    public Callable<Void, Exception> getCallable() {
        return this;
    }

    public Void call() throws Exception {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();
        final String userName = VersionControlConstants.AUTHENTICATED_USER;

        final String creatingMessage = String.format(CreatingTemplate, labelName, projectPath, workspaceName);
        logger.println(creatingMessage);

        final VersionControlLabel versionControlLabel = new VersionControlLabel(labelName, userName, userName, null, getLabelComment());
        final ItemSpec itemSpec = new ItemSpec(projectPath, RecursionType.FULL);
        final WorkspaceVersionSpec workspaceVersionSpec = new WorkspaceVersionSpec(workspaceName, userName, userName);
        final LabelItemSpec labelItemSpec = new LabelItemSpec(itemSpec, workspaceVersionSpec, false);
        final LabelItemSpec[] items = {labelItemSpec};
        final LabelResult[] labelResults = vcc.createLabel(versionControlLabel, items, LabelChildOption.FAIL);

        if (labelResults == null || labelResults.length == 0) {
            throw new RuntimeException("Label creation failed.");
        } else {
            final String createdMessage = String.format(CreatedTemplate, labelName);
            logger.println(createdMessage);
        }

        return null;
    }
}
