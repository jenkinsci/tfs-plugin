//CHECKSTYLE:OFF
package hudson.plugins.tfs.commands;

import com.google.common.base.Strings;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.TextTableParser;
import hudson.remoting.Callable;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * TF command for retrieving the latest remote change set version in effect at the specified time.
 * 
 * @author Olivier Dagenais
 *
 */
public class RemoteChangesetVersionCommand extends AbstractCallableCommand<Integer, Exception> {

    private static final String QueryingTemplate = "Querying for remote changeset at '%s' as of '%s'...";
    private static final String ResultTemplate = "Query result is: Changeset #%d by '%s' on '%s'.";
    private static final String FailedTemplate = "Query returned no result!";

    private final String versionSpecString;
    private final String path;

    public RemoteChangesetVersionCommand(
            final ServerConfigurationProvider server, final String remotePath, final VersionSpec versionSpec) {
        super(server);
        this.path = remotePath;

        this.versionSpecString = toString(versionSpec);
    }

    public Callable<Integer, Exception> getCallable() {
        return this;
    }

    public Integer call() throws Exception {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();
        final VersionSpec versionSpec = VersionSpec.parseSingleVersionFromSpec(versionSpecString, VersionControlConstants.AUTHENTICATED_USER);

        final String specString = RemoteChangesetVersionCommand.toString(versionSpec);
        final String queryingMessage = String.format(QueryingTemplate, path, specString);
        logger.println(queryingMessage);

        final Changeset[] serverChangeSets = vcc.queryHistory(
                path,
                versionSpec,
                0 /* deletionId */,
                RecursionType.FULL,
                null /* user */,
                null,
                null,
                1     /* maxCount */,
                false /* includeFileDetails */,
                true  /* slotMode */,
                false /* includeDownloadInfo */,
                false /* sortAscending */
        );
        Integer changeSetNumber = null;
        final String resultMessage;
        if (serverChangeSets != null && serverChangeSets.length >= 1) {
            final Changeset serverChangeset = serverChangeSets[0];
            changeSetNumber = serverChangeset.getChangesetID();
            final Date changeSetDate = serverChangeset.getDate().getTime();
            final String author = serverChangeset.getOwner();
            final SimpleDateFormat simpleDateFormat = DateUtil.TFS_DATETIME_FORMATTER.get();
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            final String changeSetDateIso8601 = simpleDateFormat.format(changeSetDate);
            resultMessage = String.format(ResultTemplate, changeSetNumber, author, changeSetDateIso8601);
        } else {
            resultMessage = FailedTemplate;
        }
        logger.println(resultMessage);

        return changeSetNumber;
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
