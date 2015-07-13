package hudson.plugins.tfs;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.PendChangesOptions;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LockLevel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingSet;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.plugins.tfs.model.Server;
import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URISyntaxException;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Setup/teardown of the TFS server configured by the {@code tfs_server_name} property,
 * will create the necessary structure in source control.
 */
@Documented
@JenkinsRecipe(EndToEndTfs.RunnerImpl.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface EndToEndTfs {

    /**
     * Specifies the class that will be given a chance to participate.
     */
    Class<? extends JenkinsRecipe.Runner<EndToEndTfs>> value();

    class RunnerImpl extends JenkinsRecipe.Runner<EndToEndTfs>  {

        private static final String workspaceComment = "Created by the Jenkins tfs-plugin functional tests.";

        private final String serverUrl;

        private JenkinsRecipe.Runner<EndToEndTfs> runner;

        public RunnerImpl() throws URISyntaxException {
            serverUrl = AbstractIntegrationTest.buildTfsServerUrl();
        }

        @Override
        public void setup(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
            final Description testDescription = jenkinsRule.getTestDescription();
            final Class clazz = testDescription.getTestClass();
            final String testClassName = clazz.getSimpleName();
            final String testCaseName = testDescription.getMethodName();
            final File currentFolder = new File("").getAbsoluteFile();
            final File workspaces = new File(currentFolder, "workspaces");
            // TODO: Consider NOT using the Server class
            final Server server = new Server(new TfTool(null, null, null, null), serverUrl, AbstractIntegrationTest.TestUserName, AbstractIntegrationTest.TestUserPassword);
            try {
                final TFSTeamProjectCollection tpc = server.getTeamProjectCollection();
                final VersionControlClient vcc = tpc.getVersionControlClient();

                final String workspaceName = testClassName + "-" + testCaseName;
                final Workspace workspace = createWorkspace(vcc, workspaceName);

                final String pathInTfvc = AbstractIntegrationTest.determinePathInTfvcForTestCase(testDescription);
                final File localTestClassFolder = new File(workspaces, testClassName);
                final File localBaseFolderFile = new File(localTestClassFolder, testCaseName);
                //noinspection ResultOfMethodCallIgnored
                localBaseFolderFile.mkdirs();
                final String localBaseFolder = localBaseFolderFile.getAbsolutePath();
                final WorkingFolder workingFolder = new WorkingFolder(pathInTfvc, localBaseFolder);
                workspace.createWorkingFolder(workingFolder);

                // TODO: Is this necessary if we're about to delete it, anyway?
                workspace.get(GetOptions.NONE);

                // Delete the folder associated with this test in TFVC
                workspace.pendDelete(
                        new String[]{pathInTfvc},
                        RecursionType.FULL,
                        LockLevel.UNCHANGED,
                        GetOptions.NONE,
                        PendChangesOptions.NONE);
                checkIn(workspace, "Cleaning up for the " + workspaceName + " test.");

                // create the folder in TFVC
                workspace.pendAdd(
                        new String[]{localBaseFolder},
                        false,
                        null,
                        LockLevel.UNCHANGED,
                        GetOptions.NONE,
                        PendChangesOptions.NONE);
                checkIn(workspace, "Setting up for the " + workspaceName + " test.");
            }
            finally {
                server.close();
            }

            final Class<? extends JenkinsRecipe.Runner<EndToEndTfs>> runnerClass = recipe.value();
            if (runnerClass != null) {
                runner = runnerClass.newInstance();
                runner.setup(jenkinsRule, recipe);
            }
        }

        static void checkIn(Workspace workspace, String comment) {
            final PendingSet pendingSet = workspace.getPendingChanges();
            if (pendingSet != null) {
                final PendingChange[] pendingChanges = pendingSet.getPendingChanges();
                if (pendingChanges != null) {
                    workspace.checkIn(pendingChanges, comment);
                }
            }
        }

        static Workspace createWorkspace(VersionControlClient vcc, String workspaceName) {
            Workspace workspace = vcc.getLocalWorkspace(workspaceName, ".");
            if (workspace != null) {
                for (WorkingFolder workingFolder : workspace.getFolders()) {
                    final String localItem = workingFolder.getLocalItem();
                    if (localItem != null) {
                        final File file = new File(localItem);
                        FileUtils.deleteQuietly(file);
                    }
                }
                vcc.deleteWorkspace(workspace);
            }
            workspace = vcc.createWorkspace(
                    null,
                    workspaceName,
                    null,
                    null,
                    workspaceComment,
                    WorkspaceLocation.LOCAL,
                    WorkspaceOptions.NONE);
            return workspace;
        }

        @Override
        public void decorateHome(JenkinsRule jenkinsRule, File home) throws Exception {
            if (runner != null) {
                runner.decorateHome(jenkinsRule, home);
            }
        }

        @Override
        public void tearDown(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
            if (runner != null) {
                runner.tearDown(jenkinsRule, recipe);
            }
        }
    }
}
