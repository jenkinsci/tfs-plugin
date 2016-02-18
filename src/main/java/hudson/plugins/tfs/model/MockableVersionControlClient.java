package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.events.VersionControlEventEngine;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ItemNotMappedException;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ServerPathFormatException;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.*;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
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

    public LabelResult[] createLabel(
            final VersionControlLabel label,
            final LabelItemSpec[] items,
            final LabelChildOption options) {
        makeSureNotClosed();
        return vcc.createLabel(label, items, options);
    }

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

    public void deleteWorkspace(final Workspace workspace) {
        makeSureNotClosed();
        vcc.deleteWorkspace(workspace);
    }

    public TFSTeamProjectCollection getConnection() {
        makeSureNotClosed();
        return vcc.getConnection();
    }

    public VersionControlEventEngine getEventEngine() {
        makeSureNotClosed();
        return vcc.getEventEngine();
    }

    public Workspace getLocalWorkspace(final String workspaceName, final String workspaceOwner) {
        makeSureNotClosed();
        return vcc.getLocalWorkspace(workspaceName, workspaceOwner);
    }

    public int getLatestChangesetID() {
        makeSureNotClosed();
        return vcc.getLatestChangesetID();
    }

    public Workspace getWorkspace(final String localPath) throws ItemNotMappedException {
        makeSureNotClosed();
        return vcc.getWorkspace(localPath);
    }

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

    public Workspace queryWorkspace(final String name, final String owner) {
        makeSureNotClosed();
        return vcc.queryWorkspace(name, owner);
    }

    public Workspace[] queryWorkspaces(
            final String workspaceName,
            final String workspaceOwner,
            final String computer,
            @Nonnull final WorkspacePermissions permissionsFilter) {
        makeSureNotClosed();
        return vcc.queryWorkspaces(workspaceName, workspaceOwner, computer, permissionsFilter);
    }
}
