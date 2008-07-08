package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.commands.Command;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class UpdateWorkfolderCommand implements Command {
    private final String workFolder;

    public UpdateWorkfolderCommand(String workFolder) {
        this.workFolder = workFolder;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("get");
        arguments.add(workFolder);
        arguments.add("/recursive");
        return arguments;
    }
}
