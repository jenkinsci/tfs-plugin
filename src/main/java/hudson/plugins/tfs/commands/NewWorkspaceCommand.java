package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;


public class NewWorkspaceCommand extends AbstractCommand {
    private final String workspaceName;

    public NewWorkspaceCommand(ServerConfigurationProvider provider, String workspaceName) {
        super(provider);
        this.workspaceName = workspaceName;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspace");
        arguments.add("/new");
        arguments.add(workspaceName);
        arguments.add("/noprompt");
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
}
