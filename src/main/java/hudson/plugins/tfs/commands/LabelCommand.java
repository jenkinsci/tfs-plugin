package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

/**
 * Command to create a label on TFS.
 * @author Rodrigo Lopes (rodrigolopes)
 */
public class LabelCommand extends AbstractCommand {

    private final String labelName;
    private final String workspaceName;
    private final String projectPath;

    public LabelCommand(final Server configurationProvider,
                        final String labelName,
                        final String workspaceName,
                        final String projectPath) {
        super(configurationProvider);
        this.labelName = labelName;
        this.workspaceName = workspaceName;
        this.projectPath = projectPath;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();
        arguments.add("label");
        arguments.add(labelName);
        arguments.add(projectPath);
        arguments.add(String.format("-version:W%s", workspaceName));
        arguments.add(String.format("-comment:%s", getLabelComment()));
        arguments.add("-noprompt");
        arguments.add("-recursive");
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }

    private String getLabelComment() {
        // TODO 1. Solve issue with quotes and spaces
        // TODO 2. Include build information in the comment.
        return "Automatically_applied_by_Jenkins_TFS_plugin";
    }
}
