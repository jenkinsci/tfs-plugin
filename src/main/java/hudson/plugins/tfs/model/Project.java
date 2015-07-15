package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.clients.webservices.IIdentityManagementService;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementException;
import com.microsoft.tfs.core.clients.webservices.IdentityManagementService;
import com.microsoft.tfs.core.clients.workitem.WorkItem;
import com.microsoft.tfs.core.clients.workitem.WorkItemClient;
import com.microsoft.tfs.core.clients.workitem.query.Query;
import com.microsoft.tfs.core.clients.workitem.query.WorkItemLinkInfo;
import hudson.model.User;
import hudson.plugins.tfs.commands.AbstractChangesetVersionCommand;
import hudson.plugins.tfs.commands.GetFilesToWorkFolderCommand;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
import hudson.plugins.tfs.commands.WorkspaceChangesetVersionCommand;
import hudson.plugins.tfs.model.ChangeSet.Item;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Project {

    private final String projectPath;
    private final Server server;

    public Project(Server server, String projectPath) {
        this.server = server;
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    static hudson.plugins.tfs.model.ChangeSet.Item convertServerChange
        (com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange) {
        final String path = serverChange.getItem().getServerItem();
        final String action = serverChange.getChangeType().toUIString(true);
        return new Item(path, action);
    }

    static ChangeSet.WorkItem convertWorkItem(WorkItem workItem) {
        ChangeSet.WorkItem item = new ChangeSet.WorkItem(workItem.getID(), workItem.getTitle(), workItem.getType().getName());
        WorkItem parentWorkItem = getParentWorkItem(workItem);

        if (parentWorkItem != null) {
            item.setParent(convertWorkItem(parentWorkItem));
        }
        return item;
    }

    static WorkItem getParentWorkItem(WorkItem workItem)
    {
        WorkItem parent = null;
        WorkItemClient wic = workItem.getClient();

        Query wiQuery =  workItem.getClient().createQuery("SELECT [System.Id]" +
                " FROM WorkItemLinks " +
                " WHERE [Source].[System.Id] = " + workItem.getID());

        WorkItemLinkInfo[] wiTrees = wiQuery.runLinkQuery();
        int parentLinkId = workItem.getClient().getLinkTypes().getLinkTypeEnds().get("Parent").getID();
        for (WorkItemLinkInfo wiTree : wiTrees) {
            if (wiTree.getLinkTypeID() == parentLinkId) {
                parent = wic.getWorkItemByID(wiTree.getTargetID());
                break;
            }
        }

        return parent;
    }

    static hudson.plugins.tfs.model.ChangeSet convertServerChangeset
        (com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset serverChangeset, UserLookup userLookup, WorkItemClient wic) {
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

        if (wic != null) {
            WorkItem[] workItems = serverChangeset.getWorkItems(wic);
            for (final WorkItem workItem : workItems) {
                final ChangeSet.WorkItem item = convertWorkItem(workItem);
                result.add(item);
            }
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
                    fromVersion != null ? fromVersion : toVersion,
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
                WorkItemClient wic = tpc.getWorkItemClient();
                for (final Changeset serverChangeset : serverChangesets) {
                    final ChangeSet changeSet = convertServerChangeset(serverChangeset, userLookup, wic);
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

    public List<ChangeSet> getDetailedHistory(String singleVersionSpec) {
        final VersionSpec toVersion = VersionSpec.parseSingleVersionFromSpec(singleVersionSpec, null);
        return getVCCHistory(null, toVersion, true);
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
     * Gets remote changeset version for specified remote path, as of versionSpec.
     *
     * @param remotePath for which to get latest changeset version
     * @param versionSpec a version specification to convert to a changeset number
     * @return changeset version for specified remote path
     */
    public int getRemoteChangesetVersion(final String remotePath, final VersionSpec versionSpec)
            throws IOException, InterruptedException, ParseException {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(server, remotePath, versionSpec);
        return extractChangesetNumber(command);
    }

    int extractChangesetNumber(final AbstractChangesetVersionCommand command)
            throws IOException, InterruptedException, ParseException {
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
     * Gets remote changeset version for the project's remote path, as of versionSpec.
     *
     * @param versionSpec a version specification to convert to a changeset number
     * @return changeset version for the project's remote path
     */
    public int getRemoteChangesetVersion(final VersionSpec versionSpec)
            throws IOException, InterruptedException, ParseException {
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
}
