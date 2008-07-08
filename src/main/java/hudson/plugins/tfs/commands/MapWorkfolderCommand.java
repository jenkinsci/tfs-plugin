package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class MapWorkfolderCommand implements Command {

    private final String projectPath;
    private final String workFolder;
    private final String workspaceName;

    public MapWorkfolderCommand(String projectPath, String workFolder) {
        this(projectPath, workFolder, null);
    }

    public MapWorkfolderCommand(String projectPath, String workFolder, String workspaceName) {
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
        return arguments;
    }
}
