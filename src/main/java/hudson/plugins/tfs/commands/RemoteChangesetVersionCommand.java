package hudson.plugins.tfs.commands;

import com.google.common.base.Strings;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
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
    
    static VersionSpec adjustVersionSpec(final VersionSpec versionSpec) {
        final VersionSpec adjustedVersionSpec;
        if (versionSpec instanceof DateVersionSpec) {
            // The to timestamp is exclusive, ie it will only show history before the to timestamp.
            // This command should be inclusive.
            final DateVersionSpec dateVersionSpec = (DateVersionSpec) versionSpec;
            final Calendar calendar = dateVersionSpec.getDate();
            final Calendar adjustedCalendar = (Calendar) calendar.clone();
            adjustedCalendar.add(Calendar.SECOND, 1);
            adjustedVersionSpec = new DateVersionSpec(adjustedCalendar);
        }
        else {
            adjustedVersionSpec = versionSpec;
        }
        return adjustedVersionSpec;
    }

    @Override
    String getVersionSpecification() {
        final VersionSpec adjustedVersionSpec = adjustVersionSpec(versionSpec);
        // TODO: just call adjustedVersionSpec.toString() once DateVersionSpec.toString() uses ISO 8601 format
        if (adjustedVersionSpec instanceof DateVersionSpec){
            final DateVersionSpec dateVersionSpec = (DateVersionSpec) adjustedVersionSpec;
            return DateUtil.toString(dateVersionSpec);
        }
        else if (adjustedVersionSpec instanceof LabelVersionSpec) {
            final LabelVersionSpec labelVersionSpec = (LabelVersionSpec) adjustedVersionSpec;
            // TODO: It seems to me LabelVersionSpec.toString() should emit "Lfoo" when its scope is null
            final String label = labelVersionSpec.getLabel();
            final String scope = labelVersionSpec.getScope();
            final StringBuilder sb = new StringBuilder(1 + label.length() + Strings.nullToEmpty(scope).length());
            sb.append('L');
            sb.append(label);
            if (!Strings.isNullOrEmpty(scope)) {
                sb.append('@');
                sb.append(scope);
            }
            return sb.toString();
        }
        return adjustedVersionSpec.toString();
    }
}
