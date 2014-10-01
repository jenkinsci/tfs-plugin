package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class CloakCommand extends AbstractCommand {

    private final String projectPath;
    private final String workspaceName;
    
    public CloakCommand(ServerConfigurationProvider provider, 
            String projectPath) {
        this(provider, projectPath, null);
    }

    public CloakCommand(ServerConfigurationProvider provider, 
            String projectPath, String workspaceName) {
        super(provider);
        this.projectPath = projectPath;
        this.workspaceName = workspaceName;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");        
        arguments.add("-cloak");
        arguments.add(projectPath);
        if (workspaceName != null) {
            arguments.add(String.format("-workspace:%s", workspaceName));
        }
        addLoginArgument(arguments);
        return arguments;
    }
}
