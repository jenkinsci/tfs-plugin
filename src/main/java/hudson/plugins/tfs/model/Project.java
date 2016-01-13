package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import hudson.model.User;
import hudson.plugins.tfs.commands.GetFilesToWorkFolderCommand;
import hudson.plugins.tfs.commands.UnshelveToWorkFolderCommand;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
import hudson.plugins.tfs.model.ChangeSet.Item;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;

public class Project {

    private final String projectPath;
    private final Collection<String> cloakPaths;
    private final Collection<String> shelveSets;
    private final Server server;
    private UserLookup userLookup;

    public Project(Server server, String projectPath, Collection<String> cloakPaths, Collection<String> shelveSets) {
        this.server = server;
        this.projectPath = projectPath;
        this.cloakPaths = cloakPaths;
        this.shelveSets = shelveSets;
    }

    public String getProjectPath() {
        return projectPath;
    }
    
    public Collection<String> getCloakPaths() {
    	return cloakPaths;
    }

    public Collection<String> getShelveSets () {
	return shelveSets;
    }

    static hudson.plugins.tfs.model.ChangeSet.Item convertServerChange
        (com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange) {
        final String path = serverChange.getItem().getServerItem();
        final String action = serverChange.getChangeType().toUIString(true);
        final Item result = new Item(path, action);
        return result;
    }

    public static hudson.plugins.tfs.model.ChangeSet convertServerChangeset
        (com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset serverChangeset, UserLookup userLookup) {
        final String version = Integer.toString(serverChangeset.getChangesetID(), 10);
        final Date date = serverChangeset.getDate().getTime();
        final String author = serverChangeset.getCommitter();
        final User authorUser = userLookup.find(author);
        final String comment = serverChangeset.getComment();

        final ChangeSet result = new ChangeSet(version, date, authorUser, comment);
        final Change[] serverChanges = serverChangeset.getChanges();
        for (final Change serverChange : serverChanges) {
            final Item item = convertServerChange(serverChange);
            result.add(item);
        }
        return result;
    }

    /**
     * Returns a list of changes using TFS Java SDK
     * @param fromVersion the version to get the history from
     * @param toVersion the version to get the history to
     * @param includeFileDetails whether or not to include details of modified items
     * @param maxCount the maximum number of changes to return (pass Integer.MAX_VALUE for all available values).
     *                 {@literal Must be > 0.}
     * @return a list of change sets
     */
    public List<ChangeSet> getVCCHistory(VersionSpec fromVersion, VersionSpec toVersion, boolean includeFileDetails, int maxCount) {
        final UserLookup userLookup = getOrCreateUserLookup();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final Changeset[] serverChangesets = vcc.queryHistory(
                projectPath,
                fromVersion != null ? fromVersion : toVersion,
                0 /* deletionId */,
                RecursionType.FULL,
                null /* user */,
                fromVersion,
                toVersion,
                maxCount,
                includeFileDetails /* includeFileDetails */,
                true /* slotMode */,
                false /* includeDownloadInfo */,
                false /* sortAscending */
        );
        final List<ChangeSet> result = new ArrayList<ChangeSet>();
        if (serverChangesets != null) {
            for (final Changeset serverChangeset : serverChangesets) {
                final ChangeSet changeSet = convertServerChangeset(serverChangeset, userLookup);
                result.add(changeSet);
            }
        }
        return result;
    }

    public UserLookup getOrCreateUserLookup() {
        if (userLookup == null) {
            synchronized (this) {
                if (userLookup == null) {
                    final IIdentityManagementService ims = server.createIdentityManagementService();
                    userLookup = new TfsUserLookup(ims);
                }
            }
        }
        return userLookup;
    }

    /**
     * Returns a list of change sets containing modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getDetailedHistory(Calendar fromTimestamp, Calendar toTimestamp) {
        final DateVersionSpec fromVersion = new DateVersionSpec(fromTimestamp);
        final DateVersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, true, Integer.MAX_VALUE);
    }
    
    public List<ChangeSet> getDetailedHistory(final String singleVersionSpec) {
        final VersionSpec toVersion = VersionSpec.parseSingleVersionFromSpec(singleVersionSpec, null);
        return getVCCHistory(toVersion, toVersion, true, 1);
    }

    /**
     * Returns a list of change sets not containing the modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getBriefHistory(Calendar fromTimestamp, Calendar toTimestamp) {
        final DateVersionSpec fromVersion = new DateVersionSpec(fromTimestamp);
        final DateVersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, false, Integer.MAX_VALUE);
    }

    /**
     * Returns a list of change sets not containing the modified items.
     * @param fromChangeset the changeset number to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getBriefHistory(int fromChangeset, Calendar toTimestamp) {
        final ChangesetVersionSpec fromVersion = new ChangesetVersionSpec(fromChangeset);
        final VersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, false, Integer.MAX_VALUE);
    }

    /**
     * Returns the latest changeset at the project's path.
     * @return the {@link ChangeSet} instance representing the last entry in the history for the path
     */
    public ChangeSet getLatestChangeset() {
        final List<ChangeSet> changeSets = getVCCHistory(LatestVersionSpec.INSTANCE, null, false, 1);
        final ChangeSet result = changeSets.size() > 0 ? changeSets.get(0) : null;
        return result;
    }

    /**
     * Gets all files from server.
     * @param localPath the local path to get all files into
     * @param versionSpec the version spec to use when getting the files
     */
    public void getFiles(String localPath, String versionSpec) {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(server, localPath, versionSpec);
        server.execute(command.getCallable());
    }

    /**
     * Unshelve the defined ShelveSets into Workspace
     * @param localPath the local path to unshelve into
     * @param shelveSets the Shelvesets to unshelve
     */
    public void unshelveShelveSets (String localPath, Collection<String> shelveSets)
    {
        UnshelveToWorkFolderCommand command = new UnshelveToWorkFolderCommand (server, localPath, shelveSets);
	server.execute (command.getCallable());
    }

    /**
     * Gets remote changeset version for specified remote path, as of versionSpec.
     *
     * @param remotePath for which to get latest changeset version
     * @param versionSpec a version specification to convert to a changeset number
     * @return changeset version for specified remote path
     */
    public int getRemoteChangesetVersion(final String remotePath, final VersionSpec versionSpec) {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(server, remotePath, versionSpec);
        return extractChangesetNumber(command);
    }

    int extractChangesetNumber(final RemoteChangesetVersionCommand command) {
        final Integer changeSet = server.execute(command.getCallable());
        final int result = changeSet;
        return result;
    }

    /**
     * Gets remote changeset version for the project's remote path, as of versionSpec.
     *
     * @param versionSpec a version specification to convert to a changeset number
     * @return changeset version for the project's remote path
     */
    public int getRemoteChangesetVersion(final VersionSpec versionSpec) {
        return getRemoteChangesetVersion(projectPath, versionSpec);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27).append(projectPath).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final Project other = (Project) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.projectPath, other.projectPath);
        return builder.isEquals();
    }

    protected UserLookup getUserLookup() {
        return userLookup;
    }

    protected void setUserLookup(final UserLookup userLookup) {
        this.userLookup = userLookup;
    }
}
