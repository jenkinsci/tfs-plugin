package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;


public class NewWorkspaceCommand implements Command {
    private final String workspaceName;

    public NewWorkspaceCommand(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspace");
        arguments.add("/new");
        arguments.add(workspaceName);        
        return arguments;
    }
}
