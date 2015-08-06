package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public class GetFilesToWorkFolderCommand extends AbstractCommand {

    private final String workFolder;
    private final String versionSpec;

    public GetFilesToWorkFolderCommand(final Server server, final String workFolder, final String versionSpec) {
        super(server);
        this.workFolder = workFolder;
        this.versionSpec = versionSpec;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("get");
        arguments.add(workFolder);
        arguments.add("-recursive");
        if (versionSpec != null) {
            arguments.add("-version:" + versionSpec);
        }
        arguments.add("-noprompt");
        addLoginArgument(arguments);
        return arguments;
    }
}
