package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class UnmapWorkfolderCommand implements Command {

    private final String workspaceName;
    private final String localFolder;

    public UnmapWorkfolderCommand(String localFolder, String workspaceName) {
        this.localFolder = localFolder;
        this.workspaceName = workspaceName;
    }

    public UnmapWorkfolderCommand(String localFolder) {
        this(localFolder, null);
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");        
        arguments.add("/unmap");
        arguments.add(localFolder);
        if (workspaceName != null) {
            arguments.add(String.format("/workspace:%s", workspaceName));
        }        
        return arguments;
    }

}
