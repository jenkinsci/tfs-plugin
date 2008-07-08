package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class DeleteWorkspaceCommand implements Command {

    private final String workspaceName;

    public DeleteWorkspaceCommand(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspace");
        arguments.add("/delete");
        arguments.add(workspaceName);
        return arguments;
    }
}
