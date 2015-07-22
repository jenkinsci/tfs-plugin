package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.ServerPathFormatException;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.*;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.util.Closable;

import javax.annotation.Nonnull;

/**
 * A non-final wrapper over {@link com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient}
 */
public class MockableVersionControlClient implements Closable {

    private final VersionControlClient vcc;

    public MockableVersionControlClient(final VersionControlClient vcc) {
        this.vcc = vcc;
    }

    public void close() {
        vcc.close();
    }

    public Workspace createWorkspace(
            final WorkingFolder[] workingFolders,
            final String workspaceName,
            final String owner,
            final String ownerDisplayName,
            final String comment,
            final WorkspaceLocation location,
            final WorkspaceOptions options) {
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
        vcc.deleteWorkspace(workspace);
    }

    public Workspace getLocalWorkspace(final String workspaceName, final String workspaceOwner) {
        return vcc.getLocalWorkspace(workspaceName, workspaceOwner);
    }

    public int getLatestChangesetID() {
        return vcc.getLatestChangesetID();
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

    public Workspace queryWorkspace(final String name, final String owner) {
        return vcc.queryWorkspace(name, owner);
    }

    public Workspace[] queryWorkspaces(
            final String workspaceName,
            final String workspaceOwner,
            final String computer,
            @Nonnull final WorkspacePermissions permissionsFilter) {
        return vcc.queryWorkspaces(workspaceName, workspaceOwner, computer, permissionsFilter);
    }
}
