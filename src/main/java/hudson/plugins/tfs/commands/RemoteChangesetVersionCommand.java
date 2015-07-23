package hudson.plugins.tfs.commands;

import com.google.common.base.Strings;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.util.TextTableParser;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Calendar;

/**
 * TF command for retrieving the latest remote change set version in effect at the specified time.
 * 
 * @author Olivier Dagenais
 *
 */
public class RemoteChangesetVersionCommand extends AbstractCommand implements ParseableCommand<String> {

    private final VersionSpec versionSpec;
    private final String path;

    public RemoteChangesetVersionCommand(
            ServerConfigurationProvider configurationProvider, String remotePath, VersionSpec versionSpec) {
        super(configurationProvider);
        this.path = remotePath;

        this.versionSpec = versionSpec;
    }

    /**
     * Returns arguments for TFS history command:
     *
     *    <i>tf history {path} -recursive -noprompt -stopafter:1 -version:{versionSpecification} -format:brief</i>
     *
     */
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();
        arguments.add("history");
        arguments.add(path);
        arguments.add("-recursive");
        arguments.add("-stopafter:1");
        arguments.add("-noprompt");
        arguments.add(String.format("-version:%s", getVersionSpecification()));
        arguments.add("-format:brief");
        addLoginArgument(arguments);
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

    String getVersionSpecification() {
        final VersionSpec adjustedVersionSpec = adjustVersionSpec(versionSpec);
        return toString(adjustedVersionSpec);
    }

    public static String toString(final VersionSpec versionSpec) {
        // TODO: just call versionSpec.toString() once DateVersionSpec.toString() uses ISO 8601 format
        if (versionSpec instanceof DateVersionSpec){
            final DateVersionSpec dateVersionSpec = (DateVersionSpec) versionSpec;
            return DateUtil.toString(dateVersionSpec);
        }
        else if (versionSpec instanceof LabelVersionSpec) {
            final LabelVersionSpec labelVersionSpec = (LabelVersionSpec) versionSpec;
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
        return versionSpec.toString();
    }

    public String parse(Reader consoleReader) throws ParseException, IOException {
        TextTableParser parser = new TextTableParser(new BufferedReader(consoleReader), 1);

        while (parser.nextRow()) {
            return parser.getColumn(0);
        }

        return StringUtils.EMPTY;
    }
}
