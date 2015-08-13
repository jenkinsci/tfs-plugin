package hudson.plugins.tfs.commands;

import com.google.common.base.Strings;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import hudson.model.TaskListener;
import hudson.model.User;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.TfsUserLookup;
import hudson.plugins.tfs.model.UserLookup;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.TextTableParser;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;

/**
 * TF command for retrieving the latest remote change set version in effect at the specified time.
 * 
 * @author Olivier Dagenais
 *
 */
public class RemoteChangesetVersionCommand extends AbstractCallableCommand {

    private static final String QueryingTemplate = "Querying for remote changeset at '%s' as of '%s'...";
    private static final String ResultTemplate = "Query result is: Changeset #%s by '%s' on '%s'.";
    private static final String FailedTemplate = "Query returned no result!";

    private final VersionSpec versionSpec;
    private final String path;
    private UserLookup userLookup;

    public RemoteChangesetVersionCommand(
            final Server server, final String remotePath, final VersionSpec versionSpec) {
        super(server);
        this.path = remotePath;

        this.versionSpec = versionSpec;
    }

    void setUserLookup(final UserLookup userLookup) {
        this.userLookup = userLookup;
    }

    public Callable<ChangeSet> getCallable() {
        return new Callable<ChangeSet>() {
            public ChangeSet call() throws Exception {
                final Server server = getServer();
                final MockableVersionControlClient vcc = server.getVersionControlClient();
                final TaskListener listener = server.getListener();
                final PrintStream logger = listener.getLogger();

                final String specString = getVersionSpecification();
                final String queryingMessage = String.format(QueryingTemplate, path, specString);
                logger.println(queryingMessage);

                if (userLookup == null) {
                    final IIdentityManagementService ims = server.createIdentityManagementService();
                    userLookup = new TfsUserLookup(ims);
                }
                ChangeSet changeSet = null;
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
                if (serverChangeSets != null && serverChangeSets.length >= 1) {
                    final Changeset serverChangeset = serverChangeSets[0];
                    changeSet = Project.convertServerChangeset(serverChangeset, userLookup);
                }

                final String resultMessage;
                if (changeSet != null) {
                    final String version = changeSet.getVersion();
                    final User author = changeSet.getAuthor();
                    final Date changeSetDate = changeSet.getDate();
                    final SimpleDateFormat simpleDateFormat = DateUtil.TFS_DATETIME_FORMATTER.get();
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    final String changeSetDateIso8601 = simpleDateFormat.format(changeSetDate);
                    resultMessage = String.format(ResultTemplate, version, author.getId(), changeSetDateIso8601);
                } else {
                    resultMessage = FailedTemplate;
                }
                logger.println(resultMessage);

                return changeSet;
            }
        };
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
