package hudson.plugins.tfs;

import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.PendChangesOptions;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LockLevel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingSet;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.XmlHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
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
    Class<? extends StubRunner> value();

    /**
     * The {@link EndToEndTfs} annotation requires a value of type {@link Class}.
     * This class provides an implementation that does almost nothing.
     */
    class StubRunner extends JenkinsRecipe.Runner<EndToEndTfs> {
        private RunnerImpl parent;

        protected RunnerImpl getParent() {
            return parent;
        }

        private void setParent(final RunnerImpl parent) {
            this.parent = parent;
        }

        @Override
        public void decorateHome(final JenkinsRule jenkinsRule, final File home) throws Exception {
            final String jobFolder = parent.getJobFolder();
            final String configXmlPath = jobFolder + "config.xml";
            final File configXmlFile = new File(home, configXmlPath);

            final String tfsServerUrl = AbstractIntegrationTest.buildTfsServerUrl();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/serverUrl", tfsServerUrl);

            final String projectPath = parent.getPathInTfvc();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/projectPath", projectPath);

            final String workspaceName = "Hudson-${JOB_NAME}-${COMPUTERNAME}";
            XmlHelper.pokeValue(configXmlFile, "/project/scm/workspaceName", workspaceName);

            final String userName = AbstractIntegrationTest.TestUserName;
            XmlHelper.pokeValue(configXmlFile, "/project/scm/userName", userName);
        }
    }

    class RunnerImpl extends JenkinsRecipe.Runner<EndToEndTfs>  {

        private static final String workspaceComment = "Created by the Jenkins tfs-plugin functional tests.";

        private final String serverUrl;

        private File localBaseFolderFile;
        private StubRunner runner;
        private Server server = null;
        private String testClassName;
        private String testCaseName;
        private String workspaceName;
        private String pathInTfvc;
        private Workspace workspace;

        public RunnerImpl() throws URISyntaxException {
            serverUrl = AbstractIntegrationTest.buildTfsServerUrl();
        }

        @Override
        public void setup(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
            final Description testDescription = jenkinsRule.getTestDescription();
            final Class clazz = testDescription.getTestClass();
            testClassName = clazz.getSimpleName();
            testCaseName = testDescription.getMethodName();
            final String hostName = AbstractIntegrationTest.tryToDetermineHostName();
            final File currentFolder = new File("").getAbsoluteFile();
            final File workspaces = new File(currentFolder, "workspaces");
            // TODO: Consider NOT using the Server class
            server = new Server(new TfTool(null, null, null, null), serverUrl, AbstractIntegrationTest.TestUserName, AbstractIntegrationTest.TestUserPassword);

            final MockableVersionControlClient vcc = server.getVersionControlClient();

            // workspaceName MUST be unique across computers hitting the same server
            workspaceName = hostName + "-" + testCaseName;
            workspace = createWorkspace(vcc, workspaceName);

            pathInTfvc = AbstractIntegrationTest.determinePathInTfvcForTestCase(testDescription);
            final File localTestClassFolder = new File(workspaces, testClassName);
            localBaseFolderFile = new File(localTestClassFolder, testCaseName);
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
            checkIn("Cleaning up for the " + testCaseName + " test.");
            // we don't need to verify this check-in, because a first run on a server will be a no-op

            // create the folder in TFVC
            workspace.pendAdd(
                    new String[]{localBaseFolder},
                    false,
                    null,
                    LockLevel.UNCHANGED,
                    GetOptions.NONE,
                    PendChangesOptions.NONE);
            final int changeSet = checkIn("Setting up for the " + testCaseName + " test.");
            Assert.assertTrue(changeSet >= 0);

            final Class<? extends StubRunner> runnerClass = recipe.value();
            if (runnerClass != null) {
                runner = runnerClass.newInstance();
                runner.setParent(this);
                runner.setup(jenkinsRule, recipe);
            }
        }

        public File getLocalBaseFolderFile() {
            return localBaseFolderFile;
        }

        public String getPathInTfvc() {
            return pathInTfvc;
        }

        public String getWorkspaceName() {
            return workspaceName;
        }

        public String getTestCaseName() {
            return testCaseName;
        }

        public String getTestClassName() {
            return testClassName;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public Server getServer() {
            return server;
        }

        public String getJobFolder() {
            return "jobs/" + testCaseName + "/";
        }

        public <T extends StubRunner> T getInnerRunner(final Class<T> type) {
            return type.cast(runner);
        }

        public Workspace getWorkspace() {
            return workspace;
        }

        public int checkIn(final String comment) {
            return checkIn(workspace, comment);
        }

        static int checkIn(Workspace workspace, String comment) {
            final PendingSet pendingSet = workspace.getPendingChanges();
            int result = -1;
            if (pendingSet != null) {
                final PendingChange[] pendingChanges = pendingSet.getPendingChanges();
                if (pendingChanges != null) {
                    result = workspace.checkIn(pendingChanges, comment);
                }
            }
            return result;
        }

        static Workspace createWorkspace(final MockableVersionControlClient vcc, final String workspaceName) {
            deleteWorkspace(vcc, workspaceName);

            final Workspace workspace = vcc.createWorkspace(
                    null,
                    workspaceName,
                    VersionControlConstants.AUTHENTICATED_USER,
                    VersionControlConstants.AUTHENTICATED_USER,
                    workspaceComment,
                    WorkspaceLocation.LOCAL,
                    WorkspaceOptions.NONE);
            return workspace;
        }

        static void deleteWorkspace(final MockableVersionControlClient vcc, final String workspaceName) {
            final Workspace workspace = vcc.getLocalWorkspace(workspaceName, VersionControlConstants.AUTHENTICATED_USER);
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
            final MockableVersionControlClient vcc = server.getVersionControlClient();
            deleteWorkspace(vcc, workspaceName);
            if (server != null) {
                server.close();
            }
        }
    }
}
