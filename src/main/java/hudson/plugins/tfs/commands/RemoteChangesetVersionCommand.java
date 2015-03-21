package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.util.Calendar;

/**
 * TF command for retrieving the latest remote change set version in effect at the specified time.
 * 
 * @author Olivier Dagenais
 *
 */
public class RemoteChangesetVersionCommand extends AbstractChangesetVersionCommand {

    private final VersionSpec versionSpec;
    
    public RemoteChangesetVersionCommand(
            ServerConfigurationProvider configurationProvider, String remotePath, Calendar toTimestamp) {
        super(configurationProvider, remotePath);

        this.versionSpec = new DateVersionSpec(getExclusiveToTimestamp(toTimestamp));
    }

    public RemoteChangesetVersionCommand(
            ServerConfigurationProvider configurationProvider, String remotePath, VersionSpec versionSpec) {
        super(configurationProvider, remotePath);

        this.versionSpec = versionSpec;
    }

    @Override
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = super.getArguments();
        addServerArgument(arguments);
        return arguments;
    }
    
    @Override
    String getVersionSpecification() {
        // TODO: just call versionSpec.toString() once DateVersionSpec.toString() uses ISO 8601 format
        if (versionSpec instanceof DateVersionSpec){
            final DateVersionSpec dateVersionSpec = (DateVersionSpec) versionSpec;
            return DateUtil.toString(dateVersionSpec);
        }
        return versionSpec.toString();
    }
}
