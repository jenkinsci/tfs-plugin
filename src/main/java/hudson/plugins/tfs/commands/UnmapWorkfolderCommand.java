package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class UnmapWorkfolderCommand extends AbstractCommand {

    private final String workspaceName;
    private final String localFolder;

    public UnmapWorkfolderCommand(ServerConfigurationProvider provider, String localFolder, String workspaceName) {
        super(provider);
        this.localFolder = localFolder;
        this.workspaceName = workspaceName;
    }

    public UnmapWorkfolderCommand(ServerConfigurationProvider provider, String localFolder) {
        this(provider, localFolder, null);
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");        
        arguments.add("-unmap");
        arguments.add(localFolder);
        if (workspaceName != null) {
            arguments.add(String.format("-workspace:%s", workspaceName));
        }        
        //addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }

}
