package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.util.Calendar;

/**
 * TF command for retrieving the latest remote change set version in effect at the specified time.
 * 
 * @author Olivier Dagenais
 *
 */
public class RemoteChangesetVersionCommand extends AbstractChangesetVersionCommand {

    private final Calendar toTimestamp;
    
    public RemoteChangesetVersionCommand(
            ServerConfigurationProvider configurationProvider, String remotePath, Calendar toTimestamp) {
        super(configurationProvider, remotePath);

        this.toTimestamp = getExclusiveToTimestamp(toTimestamp);
    }

    @Override
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = super.getArguments();
        addServerArgument(arguments);
        return arguments;
    }
    
    @Override
    String getVersionSpecification() {
        return String.format("~%s", AbstractCommand.getRangeSpecification(toTimestamp, 0));
    }
}
