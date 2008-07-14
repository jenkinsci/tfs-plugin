package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class MapWorkfolderCommand extends AbstractCommand {

    private final String projectPath;
    private final String workFolder;
    private final String workspaceName;

    public MapWorkfolderCommand(ServerConfigurationProvider provider, 
            String projectPath, String workFolder) {
        this(provider, projectPath, workFolder, null);
    }

    public MapWorkfolderCommand(ServerConfigurationProvider provider, 
            String projectPath, String workFolder, String workspaceName) {
        super(provider);
        this.projectPath = projectPath;
        this.workFolder = workFolder;
        this.workspaceName = workspaceName;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");        
        arguments.add("/map");
        arguments.add(projectPath);
        arguments.add(workFolder);
        if (workspaceName != null) {
            arguments.add(String.format("/workspace:%s", workspaceName));
        }        
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
}
