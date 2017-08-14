//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.Workstation;
import com.microsoft.tfs.core.clients.versioncontrol.events.EventSource;
import com.microsoft.tfs.core.clients.versioncontrol.events.VersionControlEventEngine;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ItemNotMappedException;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ServerPathFormatException;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.*;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.workspacecache.WorkspaceInfo;
import com.microsoft.tfs.util.Closable;

import javax.annotation.Nonnull;

/**
 * A non-final wrapper over {@link com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient}
 */
public class MockableVersionControlClient implements Closable {

    private final VersionControlClient vcc;
    private boolean isClosed = false;

    public MockableVersionControlClient(final VersionControlClient vcc) {
        this.vcc = vcc;
    }

    public void close() {
        if (!isClosed) {
            vcc.close();
            isClosed = true;
        }
    }

    private void makeSureNotClosed() {
        if (isClosed) {
            throw new UnsupportedOperationException("Instance has been closed and can no longer be used.");
        }
    }

    /**
     * Create or update a label for items in this workspace.
     *
     * @param label
     *        the label to create or update (must not be <code>null</code>)
     * @param items
     *        the items to be included in the label creation or update (not
     *        null).
     * @param options
     *        options that affect the processing of the label creation or update
     *        (must not be <code>null</code> or empty).
     * @return the label results, null if none were returned. May be empty but
     *         never null.
     */
    public LabelResult[] createLabel(
            final VersionControlLabel label,
            final LabelItemSpec[] items,
            final LabelChildOption options) {
        makeSureNotClosed();
        return vcc.createLabel(label, items, options);
    }

    /**
     * Create a workspace on the server.
     * <p>
     * <!-- Event Origination Info -->
     * <p>
     * This method is an <b>core event origination point</b>. The
     * {@link EventSource} object that accompanies each event fired by this
     * method describes the execution context (current thread, etc.) when and
     * where this method was invoked.
     *
     * @param workingFolders
     *        the initial working folder mappings for this workspace. May be
     *        null, which means no working folders mapped.
     * @param workspaceName
     *        the name of the new workspace (must not be <code>null</code>)
     * @param owner
     *        the name of the workspace owner (if <code>null</code>, empty, or
     *        {@link VersionControlConstants#AUTHENTICATED_USER} the currently
     *        authorized user's name is used)
     * @param ownerDisplayName
     *        the display name of the workspace owner (if <code>null</code>,
     *        empty, or {@link VersionControlConstants#AUTHENTICATED_USER} the
     *        currently authorized user's display name is used)
     * @param comment
     *        an optional comment to be stored with this workspace (may be
     *        null).
     * @param location
     *        where the workspace data is stored (if <code>null</code>, the
     *        server's default is used)
     * @param options
     *        options to use on the newly created workspace (if
     *        <code>null</code>, the default options are used)
     * @return the workspace object created by the server.
     */
    public Workspace createWorkspace(
            final WorkingFolder[] workingFolders,
            final String workspaceName,
            final String owner,
            final String ownerDisplayName,
            final String comment,
            final WorkspaceLocation location,
            final WorkspaceOptions options) {
        makeSureNotClosed();
        return vcc.createWorkspace(
                workingFolders,
                workspaceName,
                owner,
                ownerDisplayName,
                comment,
                location,
                options
        );
    }

    /**
     * Delete a workspace on the server.
     * <p>
     * <!-- Event Origination Info -->
     * <p>
     * This method is an <b>core event origination point</b>. The
     * {@link EventSource} object that accompanies each event fired by this
     * method describes the execution context (current thread, etc.) when and
     * where this method was invoked.
     *
     * @param workspace
     *        the workspace to delete.
     */
    public void deleteWorkspace(final Workspace workspace) {
        makeSureNotClosed();
        vcc.deleteWorkspace(workspace);
    }

    public TFSTeamProjectCollection getConnection() {
        makeSureNotClosed();
        return vcc.getConnection();
    }

    /**
     * @return a reference to the EventEngine used by this client. Add (and
     *         remove) listeners to this event engine instance in order to be
     *         notified of events. All client events are dispatched through this
     *         event engine.
     */
    public VersionControlEventEngine getEventEngine() {
        makeSureNotClosed();
        return vcc.getEventEngine();
    }

    /**
     * Look up the local workspace for the specified repository, workspaceName
     * and workspaceOwner combo. This will only ever return anything if the
     * workspaceOwner matches the current user. This returns the actual
     * instance, not a copy!
     */
    public Workspace getLocalWorkspace(final String workspaceName, final String workspaceOwner) {
        makeSureNotClosed();
        return vcc.getLocalWorkspace(workspaceName, workspaceOwner);
    }

    /**
     * Gets the latest changeset ID from the server.
     *
     * @return the changeset ID number of the latest changeset.
     */
    public int getLatestChangesetID() {
        makeSureNotClosed();
        return vcc.getLatestChangesetID();
    }

    /**
     * Retrieve the workspace that is mapped to the provided local path. This
     * method searches all known workspaces on the current computer to identify
     * a workspace that has explicitly or implicitly mapped the provided local
     * path. If no workspace is found, this method throws a
     * ItemNotMappedException.
     *
     * @param localPath
     *        A local path for which a workspace is desired (must not be
     *        <code>null</code>)
     * @return A reference to the workspace object that has mapped the specified
     *         local path
     * @throws ItemNotMappedException
     *         if the path is not mapped to any local workspace
     */
    public Workspace getWorkspace(final String localPath) throws ItemNotMappedException {
        makeSureNotClosed();
        return vcc.getWorkspace(localPath);
    }

    /**
     * Queries the server for history about an item. History items are returned
     * as an array of changesets.
     *
     * @param serverOrLocalPath
     *        the server or local path to the server item being queried for its
     *        history (must not be <code>null</code> or empty).
     * @param version
     *        the version of the item to query history for (history older than
     *        this version will be returned) (must not be <code>null</code>)
     * @param deletionID
     *        the deletion ID for the item, if it is a deleted item (pass 0 if
     *        the item is not deleted).
     * @param recursion
     *        whether to query recursively (must not be <code>null</code>)
     * @param user
     *        only include historical changes made by this user (pass null to
     *        retrieve changes made by all users).
     * @param versionFrom
     *        the beginning version to query historical changes from (pass null
     *        to start at the first version).
     * @param versionTo
     *        the ending version to query historical changes to (pass null to
     *        end at the most recent version).
     * @param maxCount
     *        the maximum number of changes to return (pass Integer.MAX_VALUE
     *        for all available values). Must be &gt; 0.
     * @param includeFileDetails
     *        true to include individual file change details with the returned
     *        results, false to return only general changeset information.
     * @param slotMode
     *        if true, all items that have occupied the given serverPath (during
     *        different times) will have their changes returned. If false, only
     *        the item that matches that path at the given version will have its
     *        changes returned.
     * @param sortAscending
     *        when <code>true</code> gets the top maxCount changes in ascending
     *        order, when <code>false</code> gets them in descending order
     * @return the changesets that matched the history query, null if the server
     *         did not return a changeset array.
     */
    public Changeset[] queryHistory(
            final String serverOrLocalPath,
            final VersionSpec version,
            final int deletionID,
            final RecursionType recursion,
            final String user,
            final VersionSpec versionFrom,
            final VersionSpec versionTo,
            final int maxCount,
            final boolean includeFileDetails,
            final boolean slotMode,
            final boolean includeDownloadInfo,
            final boolean sortAscending) throws ServerPathFormatException {
        makeSureNotClosed();
        return vcc.queryHistory(
                serverOrLocalPath,
                version,
                deletionID,
                recursion,
                user,
                versionFrom,
                versionTo,
                maxCount,
                includeFileDetails,
                slotMode,
                includeDownloadInfo,
                sortAscending
        );
    }

    /**
     * Query the collection of labels that match the given specifications.
     *
     * @param label
     *        the label name to match (may be null?).
     * @param scope
     *        the scope of the label to match (may be null?).
     * @param owner
     *        the owner of the label to match (may be null?).
     * @param includeItemDetails
     *        if true, details about the labeled items are included in the
     *        results, otherwise only general label information is included.
     * @param filterItem
     *        if not <code>null</code>, only labels containing this item are
     *        returned.
     * @param filterItemVersion
     *        if filterItem was supplied, only labels that include this version
     *        of the filterItem are returned, otherwise may be null.
     * @return the label items that matched the query. May be empty but never
     *         null.
     */
    public VersionControlLabel[] queryLabels(
            final String label,
            final String scope,
            final String owner,
            final boolean includeItemDetails,
            final String filterItem,
            final VersionSpec filterItemVersion) {
        makeSureNotClosed();
        return vcc.queryLabels(
                label,
                scope,
                owner,
                includeItemDetails,
                filterItem,
                filterItemVersion
        );
    }

    /**
     * Returns the workspace on the server that matches the given parameters.
     * Always queries the server immediately; does not check the local workspace
     * cache.
     * <p>
     * Unlike {@link VersionControlClient#queryWorkspaces(String, String, String)}, this method does
     * not update the local workspace cache when workspaces are queried, because
     * the workspace's computer is unknown (and the computer must be know to
     * update the cache).
     *
     * @param name
     *        the workspace name to match, null to match all.
     * @param owner
     *        the owner name to match, null to match all. Use
     *        {@link VersionControlConstants#AUTHENTICATED_USER} to retrieve
     *        workspaces owned by the currently logged in user.
     * @return the matching workspace or null if no matching workspace was
     *         found.
     */
    public Workspace queryWorkspace(final String name, final String owner) {
        makeSureNotClosed();
        return vcc.queryWorkspace(name, owner);
    }

    /**
     * Returns all workspaces on the server that match the given parameters.
     * Always queries the server immediately; does not check the local workspace
     * cache.
     *
     * @param workspaceName
     *        the workspace name to match, null to match all.
     * @param workspaceOwner
     *        the owner name to match, null to match all. Use
     *        {@link VersionControlConstants#AUTHENTICATED_USER} to retrieve
     *        workspaces owned by the currently logged in user.
     * @param computer
     *        the computer name to match, null to match all. Use
     *        LocalHost.getShortName() to match workspaces for this computer.
     * @param permissionsFilter
     *        find only workspaces matching the given permissions (must not be
     *        <code>null</code>) Use
     *        {@link WorkspacePermissions#NONE_OR_NOT_SUPPORTED} to find all
     *        workspaces.
     * @return an array of matching workspaces. May be empty but never null.
     */
    public Workspace[] queryWorkspaces(
            final String workspaceName,
            final String workspaceOwner,
            final String computer,
            @Nonnull final WorkspacePermissions permissionsFilter) {
        makeSureNotClosed();
        return vcc.queryWorkspaces(workspaceName, workspaceOwner, computer, permissionsFilter);
    }

    /**
     * Removes a cached workspace that matches the given name and owner and this
     * client's server's GUID from the {@link Workstation}'s cache. The caller
     * is responsible for saving the {@link Workstation} cache.
     */
    public WorkspaceInfo removeCachedWorkspace(final String workspaceName, String workspaceOwner) {
        makeSureNotClosed();
        return vcc.removeCachedWorkspace(workspaceName, workspaceOwner);
    }

    /**
     * This is the same as GetWorkspace() except that it returns null rather
     * than throwing ItemNotMappedException if the path is not in any known
     * local workspace.
     *
     * @param localPath
     *        A local path for which a workspace is desired (must not be
     *        <code>null</code>)
     * @return A reference to the workspace object that has mapped the specified
     *         local path or null if the local path is not in a local workspace
     */
    public Workspace tryGetWorkspace(final String localPath) {
        makeSureNotClosed();
        return vcc.tryGetWorkspace(localPath);
    }

}
