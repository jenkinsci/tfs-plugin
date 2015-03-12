package hudson.plugins.tfs.model;

import hudson.model.User;
import hudson.plugins.tfs.commands.GetFilesToWorkFolderCommand;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
import hudson.plugins.tfs.commands.WorkspaceChangesetVersionCommand;
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

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementException;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementService;

public class Project {

    private final String projectPath;
    private final Collection<String> cloakPaths;
    private final Server server;

    public Project(Server server, String projectPath, Collection<String> cloakPaths) {
        this.server = server;
        this.projectPath = projectPath;
        this.cloakPaths = cloakPaths;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public Collection<String> getCloakPaths() {
        return cloakPaths;
    }

    static hudson.plugins.tfs.model.ChangeSet.Item convertServerChange
        (com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange) {
        final String path = serverChange.getItem().getServerItem();
        final String action = serverChange.getChangeType().toUIString(true);
        final Item result = new Item(path, action);
        return result;
    }

    static hudson.plugins.tfs.model.ChangeSet convertServerChangeset
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
     * @return a list of change sets
     */
    private List<ChangeSet> getVCCHistory(VersionSpec fromVersion, VersionSpec toVersion, boolean includeFileDetails) {
        final TFSTeamProjectCollection tpc = server.getTeamProjectCollection();
        IIdentityManagementService ims;
        try {
            ims = new IdentityManagementService(tpc);
        } catch (IdentityManagementException e) {
            ims = new LegacyIdentityManagementService();
        }
        final UserLookup userLookup = new TfsUserLookup(ims);
        final VersionControlClient vcc = tpc.getVersionControlClient();
        try {
            final Changeset[] serverChangesets = vcc.queryHistory(
                    projectPath,
                    fromVersion,
                    0 /* deletionId */,
                    RecursionType.FULL,
                    null /* user */,
                    fromVersion,
                    toVersion,
                    Integer.MAX_VALUE,
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
        finally {
            vcc.close();
        }
    }

    /**
     * Returns a list of change sets containing modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getDetailedHistory(Calendar fromTimestamp, Calendar toTimestamp) throws IOException, InterruptedException, ParseException {
        final DateVersionSpec fromVersion = new DateVersionSpec(fromTimestamp);
        final DateVersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, true);
    }
    
	public List<ChangeSet> getDetailedHistory(String label) {
		LabelVersionSpec fromVersion = new LabelVersionSpec(new LabelSpec(label, null));
		return getVCCHistory(fromVersion, null, true);
	}

    /**
     * Returns a list of change sets not containing the modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getBriefHistory(Calendar fromTimestamp, Calendar toTimestamp) throws IOException, InterruptedException, ParseException {
        final DateVersionSpec fromVersion = new DateVersionSpec(fromTimestamp);
        final DateVersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, false);
    }

    /**
     * Returns a list of change sets not containing the modified items.
     * @param fromChangeset the changeset number to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getBriefHistory(int fromChangeset, Calendar toTimestamp) throws IOException, InterruptedException, ParseException {
        final ChangesetVersionSpec fromVersion = new ChangesetVersionSpec(fromChangeset);
        final VersionSpec toVersion = new DateVersionSpec(toTimestamp);
        return getVCCHistory(fromVersion, toVersion, false);
    }

    /**
     * Gets all files from server.
     * @param localPath the local path to get all files into
     */
    public void getFiles(String localPath) throws IOException, InterruptedException {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(server, localPath);
        server.execute(command.getArguments()).close();
    }

    /**
     * Gets all files from server.
     * @param localPath the local path to get all files into
     * @param versionSpec the version spec to use when getting the files
     */
    public void getFiles(String localPath, String versionSpec) throws IOException, InterruptedException {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(server, localPath, versionSpec);
        server.execute(command.getArguments()).close();
    }

    /**
     * Gets workspace changeset version for specified local path.
     * 
     * @param localPath for which to get latest workspace changeset version
     * @param workspaceName name of workspace for which to get latest changeset version
     * @return workspace changeset version for specified local path
     */
    public String getWorkspaceChangesetVersion(String localPath, String workspaceName, String workspaceOwner) 
                                                                                       throws IOException, 
                                                                                              InterruptedException, 
                                                                                              ParseException {
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(server,localPath,workspaceName, workspaceOwner);
        Reader reader = null;
        try {
            reader = server.execute(command.getArguments());
            return command.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Gets remote changeset version for specified remote path, as of toTimestamp.
     * 
     * @param remotePath for which to get latest changeset version
     * @param toTimestamp the date/time of the last build
     * @return changeset version for specified remote path
     */
    public int getRemoteChangesetVersion(String remotePath, Calendar toTimestamp)
            throws IOException, InterruptedException, ParseException {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(server, remotePath, toTimestamp);
        Reader reader = null;
        try {
            reader = server.execute(command.getArguments());
            final String changesetString = command.parse(reader);
            return Integer.parseInt(changesetString, 10);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Gets remote changeset version for the project's remote path, as of toTimestamp.
     * 
     * @param toTimestamp the date/time of the last build
     * @return changeset version for the project's remote path
     */
    public int getRemoteChangesetVersion(Calendar toTimestamp)
            throws IOException, InterruptedException, ParseException {
        return getRemoteChangesetVersion(projectPath, toTimestamp);
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
}
