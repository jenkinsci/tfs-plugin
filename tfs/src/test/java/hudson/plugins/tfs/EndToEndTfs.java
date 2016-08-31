package hudson.plugins.tfs;

import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.PendChangesOptions;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LockLevel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingSet;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.plugins.tfs.model.ExtraSettings;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.XmlHelper;
import hudson.util.Secret;
import hudson.util.SecretOverride;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.runner.Description;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
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
        private IntegrationTestHelper helper;
        private String encryptedPassword;

        public String getEncryptedPassword() {
            return encryptedPassword;
        }

        protected RunnerImpl getParent() {
            return parent;
        }

        private void setParent(final RunnerImpl parent) {
            this.parent = parent;
        }

        protected IntegrationTestHelper getHelper() {
            return helper;
        }

        private void setHelper(final IntegrationTestHelper helper) {
            this.helper = helper;
        }

        @Override
        public void decorateHome(final JenkinsRule jenkinsRule, final File home) throws Exception {
            final String jobFolder = parent.getJobFolder();
            final String configXmlPath = jobFolder + "config.xml";
            final File configXmlFile = new File(home, configXmlPath);

            final String tfsServerUrl = helper.getServerUrl();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/serverUrl", tfsServerUrl);

            final String projectPath = parent.getPathInTfvc();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/projectPath", projectPath);

            final String workspaceName = "Hudson-${JOB_NAME}-${COMPUTERNAME}";
            XmlHelper.pokeValue(configXmlFile, "/project/scm/workspaceName", workspaceName);

            final String userName = helper.getUserName();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/userName", userName);

            final String userPassword = helper.getUserPassword();
            final SecretOverride secretOverride = new SecretOverride();
            try {
                final Secret secret = Secret.fromString(userPassword);
                encryptedPassword = secret.getEncryptedValue();
            }
            finally {
                try {
                    secretOverride.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            final String projectScmPassword = "/project/scm/password";
            final String currentPassword = XmlHelper.peekValue(configXmlFile, projectScmPassword);
            if (currentPassword != null) {
                XmlHelper.pokeValue(configXmlFile, projectScmPassword, encryptedPassword);
            }
        }
    }

    class RunnerImpl extends JenkinsRecipe.Runner<EndToEndTfs>  {

        private static final String workspaceComment = "Created by the TFS plugin for Jenkins functional tests.";

        private final IntegrationTestHelper helper;
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
            helper = new IntegrationTestHelper();
            serverUrl = helper.getServerUrl();
        }

        @Override
        public void setup(JenkinsRule jenkinsRule, EndToEndTfs recipe) throws Exception {
            final Description testDescription = jenkinsRule.getTestDescription();
            final Class clazz = testDescription.getTestClass();
            testClassName = clazz.getSimpleName();
            testCaseName = testDescription.getMethodName();
            final String hostName = IntegrationTestHelper.tryToDetermineHostName();
            final File currentFolder = new File("").getAbsoluteFile();
            final File workspaces = new File(currentFolder, "workspaces");
            // TODO: Consider NOT using the Server class
            server = new Server(null, null, serverUrl, helper.getUserName(), helper.getUserPassword(), null, ExtraSettings.DEFAULT);

            final MockableVersionControlClient vcc = server.getVersionControlClient();

            // workspaceName MUST be unique across computers hitting the same server
            workspaceName = hostName + "-" + testCaseName;
            workspace = createWorkspace(vcc, workspaceName);

            pathInTfvc = IntegrationTestHelper.determinePathInTfvcForTestCase(testDescription);
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
                runner.setHelper(this.helper);
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
            final Workspace[] workspaces = vcc.queryWorkspaces(workspaceName, null, /* TODO: computer */null, WorkspacePermissions.NONE_OR_NOT_SUPPORTED);
            for (final Workspace workspace : workspaces) {
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
