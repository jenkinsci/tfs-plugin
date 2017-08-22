package hudson.plugins.tfs;

import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.PendChangesOptions;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LockLevel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.VersionControlLabel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.jni.helpers.LocalHost;
import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher;
import hudson.ProxyConfiguration;
import hudson.console.AnnotatedLargeText;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.Computer;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.XmlHelper;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogSet;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.slaves.DumbSlave;
import hudson.slaves.SlaveComputer;
import hudson.triggers.SCMTrigger;
import hudson.util.Scrambler;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.recipes.LocalData;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Tests that exercise real-world functionality, using temporary Jenkins instances.
 * These are so-called functional (L3) tests.
 * Tests may connect to a TFS server identified by the tfs_server_name property.
 */
@Category(IntegrationTests.class)
public class FunctionalTest {

    /**
     * A special version of {@link JenkinsRule} that assumes {@link EndToEndTfs} decorates a test,
     * giving access to the {@link hudson.plugins.tfs.EndToEndTfs.RunnerImpl} and all the cool
     * stuff it has.
     */
    public class TfsJenkinsRule extends JenkinsRule{
        /**
          * https://wiki.jenkins-ci.org/display/JENKINS/Unit+Test+on+Windows#UnitTestonWindows-UnabletodeleteslaveslaveX.log
          *
          */
        private void purgeSlaves() {
            List<Computer> disconnectingComputers = new ArrayList<Computer>();
            List<VirtualChannel> closingChannels = new ArrayList<VirtualChannel>();
            for (Computer computer: jenkins.getComputers()) {
                if (!(computer instanceof SlaveComputer)) {
                    continue;
                }
                // disconnect slaves.
                // retrieve the channel before disconnecting.
                // even a computer gets offline, channel delays to close.
                if (!computer.isOffline()) {
                    VirtualChannel ch = computer.getChannel();
                    computer.disconnect(null);
                    disconnectingComputers.add(computer);
                    closingChannels.add(ch);
                }
            }

            try {
                // Wait for all computers disconnected and all channels closed.
                for (Computer computer: disconnectingComputers) {
                    computer.waitUntilOffline();
                }
                for (VirtualChannel ch: closingChannels) {
                    ch.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void after() throws Exception {
            if (Functions.isWindows()) {
                purgeSlaves();
            }
            super.after();
        }

        public EndToEndTfs.RunnerImpl getTfsRunner() {
            EndToEndTfs.RunnerImpl result = null;
            for (final JenkinsRecipe.Runner recipe : recipes) {
                if (recipe instanceof EndToEndTfs.RunnerImpl) {
                    result = (EndToEndTfs.RunnerImpl) recipe;
                    break;
                }
            }
            return result;
        }
    }

    @Rule public TfsJenkinsRule j = new TfsJenkinsRule();

    /**
     * Runs the project's {@link SCMTrigger} to poll for changes, which may schedule a build.
     *
     * If it does schedule a build, we'll wait for that build to complete and return it;
     * otherwise we return {@code null}.
     *
     * This assumes Jenkins (or the project/job) was configured with a quietPeriod, to give us
     * time to retrieve the item from the queue (especially when execution is paused in the debugger)
     * so we can wait on it.
     *
     * @param project The {@link Project} for which to poll and build.
     * @return The {@link AbstractBuild} that resulted from the build, if applicable; otherwise {@code null}.
     */
    public static AbstractBuild runScmPollTrigger(final Project project)
            throws InterruptedException, ExecutionException {
        final SCMTrigger scmTrigger = (SCMTrigger) project.getTrigger(SCMTrigger.class);
        // This is a roundabout way of calling SCMTrigger#run(),
        // because if we set SCMTrigger#synchronousPolling to true
        // Trigger#checkTriggers() unconditionally runs the trigger,
        // even if we set its schedule (spec) to an empty string
        // (which normally disables the schedule).
        // Having synchronous polling (& building!) in our tests
        // is more important than skipping the usual method call chain.
        // http://docs.oracle.com/javase/tutorial/java/javaOO/nested.html
        final SCMTrigger.Runner runner = scmTrigger.new Runner();
        runner.run();

        final AbstractBuild build = waitForQueuedBuild(project);
        return build;
    }

    public static AbstractBuild runUserTrigger(final Project project)
            throws InterruptedException, ExecutionException {
        final Cause.UserIdCause cause = new Cause.UserIdCause();
        project.scheduleBuild(cause);

        final AbstractBuild build = waitForQueuedBuild(project);
        return build;
    }

    static AbstractBuild waitForQueuedBuild(final Project project)
            throws InterruptedException, ExecutionException {
        final Jenkins jenkins = (Jenkins) project.getParent();
        final Queue queue = jenkins.getQueue();
        final Queue.Item[] items = queue.getItems();
        final boolean buildQueued = items.length == 1;
        final AbstractBuild build;
        if (buildQueued) {
            final Queue.WaitingItem queuedItem = (Queue.WaitingItem) items[0];
            // now that we have the queued item, we can "shorten the quiet period to zero"
            final GregorianCalendar due = new GregorianCalendar();
            due.add(Calendar.SECOND, -1);
            queuedItem.timestamp = due;
            // force re-evaluation of the queue, which should notice the item shouldn't wait anymore
            queue.maintain();
            queue.scheduleMaintenance();

            final Future<? extends AbstractBuild> future = (Future) queuedItem.getFuture();
            build = future.get();
        }
        else {
            build = null;
        }
        return build;
    }

    @LocalData
    @EndToEndTfs(CreateLabel.class)
    @Test public void createLabel() throws ExecutionException, InterruptedException, IOException {
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final CreateLabel innerRunner = tfsRunner.getInnerRunner(CreateLabel.class);
        final String generatedLabelName = innerRunner.getGeneratedLabelName();
        final Server server = tfsRunner.getServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        final int latestChangesetID = vcc.getLatestChangesetID();

        // setup build
        runUserTrigger(project);

        // polling should report no changes
        final PollingResult pollingResult = project.poll(taskListener);

        Assert.assertEquals(PollingResult.Change.NONE, pollingResult.change);

        // trigger build
        final AbstractBuild build = runUserTrigger(project);

        // verify new label created against latestChangesetId
        Assert.assertNotNull(build);
        assertBuildSuccess(build);
        final ChangeLogSet changeSet = build.getChangeSet();
        Assert.assertEquals(0, changeSet.getItems().length);
        final TFSRevisionState revisionState = build.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, revisionState.changesetVersion);
        final String owner = VersionControlConstants.AUTHENTICATED_USER;
        final ChangesetVersionSpec spec = new ChangesetVersionSpec(latestChangesetID);
        final VersionControlLabel[] labels = vcc.queryLabels(generatedLabelName, null, owner, false, null, spec);
        Assert.assertEquals(1, labels.length);
        final VersionControlLabel label = labels[0];
        Assert.assertFalse(StringUtils.isEmpty(label.getComment()));
    }

    public void assertBuildSuccess(final AbstractBuild build) throws IOException {
        final Result result = build.getResult();
        if (!Result.SUCCESS.equals(result)) {
            final AnnotatedLargeText logText = build.getLogText();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            logText.writeLogTo(0, baos);

            final String headerTemplate = "Build result: %s\n---Log Start---\n";
            final String header = String.format(headerTemplate, result);
            final String message = header + baos.toString() + "---Log End---\n\n";
            Assert.fail(message);
        }
    }

    public static class CreateLabel extends CurrentChangesetInjector {

        private final String generatedLabelName;

        public CreateLabel(){
            final Calendar now = Calendar.getInstance();
            final String iso8601DateString = DateUtil.toString(now);
            generatedLabelName = "CreateLabel_" + iso8601DateString.replace(':', '-');
        }

        public String getGeneratedLabelName() {
            return generatedLabelName;
        }

        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home) throws Exception {
            super.decorateHome(jenkinsRule, home);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String jobFolder = parent.getJobFolder();
            final String configXmlPath = jobFolder + "config.xml";
            final File configXmlFile = new File(home, configXmlPath);

            final String labelNameXPath = "/project/publishers/hudson.plugins.tfs.TFSLabeler/labelName";
            XmlHelper.pokeValue(configXmlFile, labelNameXPath, generatedLabelName);
        }
    }

    @LocalData
    @EndToEndTfs(CurrentChangesetInjector.class)
    @Test public void agent() throws Exception {
        final Jenkins jenkins = j.jenkins;
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        checkInEmptyFile(tfsRunner);
        final LabelAtom label = new LabelAtom("agent");
        final DumbSlave agent = j.createOnlineSlave(label);
        project.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
                    throws InterruptedException, IOException {
                final FilePath workspace = build.getWorkspace();
                final FilePath child = workspace.child("TODO.txt");
                final boolean result = child.exists();
                return result;
            }
        });

        final AbstractBuild firstBuild = runScmPollTrigger(project);

        Assert.assertNotNull(firstBuild);
        assertBuildSuccess(firstBuild);

        final FilePath workspace = firstBuild.getWorkspace();
        final FilePath workspaceParent = workspace.getParent();
        final FilePath assumedRootPath = workspaceParent.getParent();
        final FilePath agentRootPath = agent.getRootPath();
        Assert.assertEquals(agentRootPath, assumedRootPath);

        assertEmptyFileIsInWorkspace(workspace);
    }

    @LocalData
    @EndToEndTfs(EndToEndTfs.StubRunner.class)
    @Test public void newJob() throws InterruptedException, ExecutionException, IOException {
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final Server server = tfsRunner.getServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        int latestChangesetID;

        // first poll should queue a build because we were never built
        latestChangesetID = vcc.getLatestChangesetID();
        final AbstractBuild firstBuild = runScmPollTrigger(project);

        Assert.assertNotNull(firstBuild);
        assertBuildSuccess(firstBuild);
        final ChangeLogSet firstChangeSet = firstBuild.getChangeSet();
        Assert.assertEquals(true, firstChangeSet.isEmptySet());
        final TFSRevisionState firstRevisionState = firstBuild.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, firstRevisionState.changesetVersion);
        final List<Cause> firstCauses = firstBuild.getCauses();
        Assert.assertEquals(1, firstCauses.size());
        final Cause firstCause = firstCauses.get(0);
        Assert.assertTrue(firstCause instanceof SCMTrigger.SCMTriggerCause);

        // second poll should report no changes since last build
        final PollingResult secondPoll = project.poll(taskListener);

        Assert.assertEquals(PollingResult.Change.NONE, secondPoll.change);

        // make a change in source control
        final int changeSet = checkInEmptyFile(tfsRunner);
        Assert.assertTrue(changeSet >= 0);

        // third poll should trigger a build
        latestChangesetID = vcc.getLatestChangesetID();
        final AbstractBuild secondBuild = runScmPollTrigger(project);

        Assert.assertNotNull(secondBuild);
        assertBuildSuccess(secondBuild);
        final ChangeLogSet secondChangeSet = secondBuild.getChangeSet();
        Assert.assertEquals(1, secondChangeSet.getItems().length);
        final TFSRevisionState secondRevisionState = secondBuild.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, secondRevisionState.changesetVersion);
        final List<Cause> secondCauses = secondBuild.getCauses();
        Assert.assertEquals(1, secondCauses.size());
        final Cause secondCause = secondCauses.get(0);
        Assert.assertTrue(secondCause instanceof SCMTrigger.SCMTriggerCause);
        final FilePath jenkinsWorkspace = secondBuild.getWorkspace();
        assertEmptyFileIsInWorkspace(jenkinsWorkspace);

        // force a build via a manual trigger
        final AbstractBuild thirdBuild = runUserTrigger(project);

        Assert.assertNotNull(thirdBuild);
        assertBuildSuccess(thirdBuild);
        final ChangeLogSet thirdChangeSet = thirdBuild.getChangeSet();
        Assert.assertEquals(0, thirdChangeSet.getItems().length);
        final TFSRevisionState thirdRevisionState = thirdBuild.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, thirdRevisionState.changesetVersion);
        final List<Cause> thirdCauses = thirdBuild.getCauses();
        Assert.assertEquals(1, thirdCauses.size());
        final Cause thirdCause = thirdCauses.get(0);
        Assert.assertTrue(thirdCause instanceof Cause.UserIdCause);
        final FilePath thirdBuildWorkspace = thirdBuild.getWorkspace();
        assertEmptyFileIsInWorkspace(thirdBuildWorkspace);

        // finally, delete the project, which should first remove the workspace
        final TeamFoundationServerScm scm = (TeamFoundationServerScm) project.getScm();
        final Computer computer = Computer.currentComputer();
        final String hostName = LocalHost.getShortName();
        final String workspaceName = scm.getWorkspaceName(thirdBuild, computer).replace("${COMPUTERNAME}", hostName);
        Assert.assertTrue(jenkinsWorkspace.exists());
        final Workspace[] workspacesBeforeDeletion = vcc.queryWorkspaces(workspaceName, VersionControlConstants.AUTHENTICATED_USER, hostName, WorkspacePermissions.NONE_OR_NOT_SUPPORTED);
        Assert.assertEquals(1, workspacesBeforeDeletion.length);

        project.delete();

        Assert.assertFalse(jenkinsWorkspace.exists());
        final Workspace[] workspacesAfterDeletion = vcc.queryWorkspaces(workspaceName, VersionControlConstants.AUTHENTICATED_USER, hostName, WorkspacePermissions.NONE_OR_NOT_SUPPORTED);
        Assert.assertEquals(0, workspacesAfterDeletion.length);
    }

    public void assertEmptyFileIsInWorkspace(final FilePath workspace) throws IOException, InterruptedException {
        final FilePath[] workspaceFiles = workspace.list("*.*", "$tf");
        Assert.assertEquals(1, workspaceFiles.length);
        final FilePath workspaceFile = workspaceFiles[0];
        Assert.assertEquals("TODO.txt", workspaceFile.getName());
    }

    public static int checkInEmptyFile(final EndToEndTfs.RunnerImpl tfsRunner) throws IOException {
        return checkInFile(tfsRunner, "Add a file.", null);
    }

    public static int checkInFile(final EndToEndTfs.RunnerImpl tfsRunner, final String changeMessage, final String fileContents) throws IOException {
        final Workspace workspace = tfsRunner.getWorkspace();
        final File todoFile = new File(tfsRunner.getLocalBaseFolderFile(), "TODO.txt");
        final boolean alreadyExisted = todoFile.isFile();
        FileUtils.writeStringToFile(todoFile, fileContents, "UTF-8");
        final String[] paths = {todoFile.getAbsolutePath()};
        if (alreadyExisted) {
            workspace.pendEdit(
                    paths,
                    RecursionType.NONE,
                    LockLevel.UNCHANGED,
                    null,
                    GetOptions.NONE,
                    PendChangesOptions.NONE);
        }
        else {
            workspace.pendAdd(
                    paths,
                    false,
                    null,
                    LockLevel.UNCHANGED,
                    GetOptions.NONE,
                    PendChangesOptions.NONE);
        }
        return tfsRunner.checkIn(tfsRunner.getTestCaseName() + " " + changeMessage);
    }

    @LocalData
    @EndToEndTfs(CloakedPaths.class)
    @Test public void cloakedPaths() throws Exception {
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final Server server = tfsRunner.getServer();
        final String testCaseName = tfsRunner.getTestCaseName();
        final Workspace workspace = tfsRunner.getWorkspace();
        final List<Project> projects = jenkins.getProjects();
        final Project jenkinsProject = projects.get(0);
        final TeamFoundationServerScm tfsScm = (TeamFoundationServerScm) jenkinsProject.getScm();
        Assert.assertNotEquals(StringUtils.EMPTY, tfsScm.getCloakedPaths());
        int latestChangesetID;

        // arrange: create structure
        final File root = tfsRunner.getLocalBaseFolderFile();
        final String[] paths = {
                createWorkspaceFile(root, "root.txt"),
                createWorkspaceFile(root, "A/A.txt"),
                createWorkspaceFile(root, "A/1/A1.txt"),
                createWorkspaceFile(root, "A/2/A2.txt"),
                createWorkspaceFile(root, "B/B.txt"),
                createWorkspaceFile(root, "C/C.txt"),
        };
        workspace.pendAdd(
                paths,
                false,
                null,
                LockLevel.UNCHANGED,
                GetOptions.NONE,
                PendChangesOptions.NONE);
        final int structureChangeSet = tfsRunner.checkIn(testCaseName + " Create structure.");
        Assert.assertTrue(structureChangeSet >= 0);

        // act: poll trigger
        final AbstractBuild firstBuild = runScmPollTrigger(jenkinsProject);

        // assert
        Assert.assertNotNull("First poll should queue a build", firstBuild);
        assertBuildSuccess(firstBuild);
        assertCloakedPathsWorkspaceContents(firstBuild.getWorkspace());

        // arrange: make a change in a non-cloaked path (fully uncloaked)
        final File aOne = new File(root, "A/1/A1.txt");
        FileUtils.writeStringToFile(aOne, "Now with content!", "UTF-8");
        workspace.pendEdit(
                new String[]{aOne.getAbsolutePath()},
                RecursionType.NONE,
                LockLevel.UNCHANGED,
                null,
                GetOptions.NONE,
                PendChangesOptions.NONE);
        latestChangesetID = tfsRunner.checkIn(testCaseName + " Add content to A1.txt");

        // act: poll trigger
        final AbstractBuild secondBuild = runScmPollTrigger(jenkinsProject);

        // assert
        Assert.assertNotNull("Second poll should queue a build", secondBuild);
        assertCloakedPathsWorkspaceContents(secondBuild.getWorkspace());

        // arrange: make a changeset that has an item in a cloaked path
        final File aTwo = new File(root, "A/2/A2.txt");
        FileUtils.writeStringToFile(aOne, "Now with content!", "UTF-8");
        workspace.pendEdit(
                new String[]{aTwo.getAbsolutePath()},
                RecursionType.NONE,
                LockLevel.UNCHANGED,
                null,
                GetOptions.NONE,
                PendChangesOptions.NONE);
        latestChangesetID = tfsRunner.checkIn(testCaseName + " Add content to A2.txt");

        // act: poll (no need to poll trigger)
        final PollingResult thirdPoll = jenkinsProject.poll(taskListener);

        // assert
        Assert.assertEquals("Third poll should NOT find any significant changes", PollingResult.Change.NONE, thirdPoll.change);

        // arrange: create a changeset that has changes in both cloaked and uncloaked paths
        final File a = new File(root, "A/A.txt");
        FileUtils.writeStringToFile(a, "Now with content!", "UTF-8");
        final File b = new File(root, "B/B.txt");
        FileUtils.writeStringToFile(b, "Now with content!", "UTF-8");
        workspace.pendEdit(
                new String[]{
                        a.getAbsolutePath(),
                        b.getAbsolutePath(),
                },
                RecursionType.NONE,
                LockLevel.UNCHANGED,
                null,
                GetOptions.NONE,
                PendChangesOptions.NONE);
        latestChangesetID = tfsRunner.checkIn(testCaseName + " Add content to A.txt and B.txt");

        // act: poll trigger
        final AbstractBuild thirdBuild = runScmPollTrigger(jenkinsProject);

        // assert
        Assert.assertNotNull("Fourth poll should queue a build", thirdBuild);
        assertCloakedPathsWorkspaceContents(thirdBuild.getWorkspace());
    }

    /** workspace should only contain:
root.txt
A/A.txt
A/1/A1.txt
C/C.txt
    */
    private static void assertCloakedPathsWorkspaceContents(final FilePath workspace) throws Exception {
        final FilePath[] workspaceFiles = workspace.list("**", "$tf");
        final HashSet<String> expectedFileNames = new HashSet<String>(Arrays.asList("root.txt", "A.txt", "A1.txt", "C.txt"));
        for (final FilePath workspaceFile : workspaceFiles) {
            final String actualFileName = workspaceFile.getName();
            if (expectedFileNames.contains(actualFileName)) {
                expectedFileNames.remove(actualFileName);
            }
            else {
                final String message = "Did not expect to find " + actualFileName + " in the workspace.";
                Assert.fail(message);
            }
        }
        Assert.assertEquals("All expected files should have been found in the workspace", 0, expectedFileNames.size());
    }

    static String createWorkspaceFile(final File root, final String relFilePath) throws IOException {
        final File file = new File(root, relFilePath);
        final File folder = file.getParentFile();
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        //noinspection ResultOfMethodCallIgnored
        file.createNewFile();
        return file.getAbsolutePath();
    }

    public static class CloakedPaths extends CurrentChangesetInjector {

        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home)
                throws Exception {
            super.decorateHome(jenkinsRule, home);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String jobFolder = parent.getJobFolder();
            final String configXmlPath = jobFolder + "config.xml";
            final File configXmlFile = new File(home, configXmlPath);

            final String projectPath = parent.getPathInTfvc();
            XmlHelper.pokeValue(configXmlFile, "/project/scm/cloakedPaths/string[1]", projectPath + "/A/2");
            XmlHelper.pokeValue(configXmlFile, "/project/scm/cloakedPaths/string[2]", projectPath + "/B");
        }
    }

    /**
     * If there's no SCMRevisionState present, we revert to old-school polling,
     * using the timestamp of the last build to see if there have been any changes in TFVC,
     * at the project's path, since the specified time.
     *
     * Even though the @EndToEndTfs annotation caused some commits,
     * the OldPollingFallback runner poked the current time (after said commits)
     * in the build.xml, which should cause polling to not find any changes.
     */
    @LocalData
    @EndToEndTfs(OldPollingFallback.class)
    @Ignore @Test public void oldPollingFallback() throws IOException {
        final Jenkins jenkins = j.jenkins;
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        final TaskListener taskListener = j.createTaskListener();

        final PollingResult actual = project.poll(taskListener);

        Assert.assertEquals(PollingResult.NO_CHANGES, actual);
    }

    /**
     * Injects the current time in milliseconds into the <code>/build/timestamp</code> element
     * of the last <code>build.xml</code>.
     */
    public static class OldPollingFallback extends EndToEndTfs.StubRunner {

        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home)
                throws Exception {
            super.decorateHome(jenkinsRule, home);

            // Add a small pause to make sure we record the timestamp one second later
            // than the last check-in, otherwise we have the polling occurring on the same
            // second as the check-in and finding an SCM change where there should be none.
            Thread.sleep(1000 /* ms */);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String jobFolder = parent.getJobFolder();
            final String lastBuildXmlPath = jobFolder + "builds/2015-07-15_20-37-42/build.xml";
            final File lastBuildXmlFile = new File(home, lastBuildXmlPath);
            final long rightNowMilliseconds = System.currentTimeMillis();
            final String value = String.valueOf(rightNowMilliseconds);
            XmlHelper.pokeValue(lastBuildXmlFile, "/build/timestamp", value);
        }
    }

    /**
     * As of version 3.2.0, passwords are no longer encoded but encrypted.
     * Such a job should have its encoded password upgraded to encrypted
     * and still be able to poll and build.
     */
    @LocalData
    @EndToEndTfs(UpgradeEncodedPassword.class)
    @Test public void upgradeEncodedPassword()
            throws IOException, XPathExpressionException, ExecutionException, InterruptedException,
		SAXException, ParserConfigurationException {
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final EndToEndTfs.StubRunner stubRunner = tfsRunner.getInnerRunner(EndToEndTfs.StubRunner.class);
        final String encryptedPassword = stubRunner.getEncryptedPassword();
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        final TeamFoundationServerScm scm = (TeamFoundationServerScm) project.getScm();
        final Secret passwordSecret = scm.getPassword();
        Assert.assertEquals(encryptedPassword, passwordSecret.getEncryptedValue());
        PollingResult actualPollingResult;

        // setup build
        runUserTrigger(project);

        actualPollingResult = project.poll(taskListener);
        Assert.assertEquals(PollingResult.Change.NONE, actualPollingResult.change);

        project.save(/* force the project to be written to disk, which should encrypt the password */);

        actualPollingResult = project.poll(taskListener);
        Assert.assertEquals(PollingResult.Change.NONE, actualPollingResult.change);
        final File home = j.jenkins.getRootDir();
        final String configXmlPath = "jobs/upgradeEncodedPassword/config.xml";
        final File configXmlFile = new File(home, configXmlPath);
        final String userPassword = XmlHelper.peekValue(configXmlFile, "/project/scm/userPassword");
        Assert.assertEquals("Encoded password should no longer be there", null, userPassword);
        final String password = XmlHelper.peekValue(configXmlFile, "/project/scm/password");
        Assert.assertEquals("Encrypted password should be there", encryptedPassword, password);

        // TODO: Check in & record changeset, poll & assert SIGNIFICANT
        // TODO: build & assert new last build recorded changeset from above
        // TODO: poll & assert NONE
    }

    public static class UpgradeEncodedPassword extends CurrentChangesetInjector {
        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home) throws Exception {
            super.decorateHome(jenkinsRule, home);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String jobFolder = parent.getJobFolder();
            final IntegrationTestHelper helper = getHelper();
            final String userPassword = helper.getUserPassword();
            final String scrambledPassword = Scrambler.scramble(userPassword);
            final String configXmlPath = jobFolder + "config.xml";
            final File configXmlFile = new File(home, configXmlPath);
            XmlHelper.pokeValue(configXmlFile, "/project/scm/userPassword", scrambledPassword);
        }
    }

    /**
     * Verifies that we can still poll and GET from a server when going through a proxy server.
     */
    @LocalData
    @EndToEndTfs(UseWebProxyServer.class)
    @Test public void useWebProxyServer() throws Exception {
        final Jenkins jenkins = j.jenkins;

        // double-check proxy configuration was loaded and is available
        final ProxyConfiguration proxyConfiguration = jenkins.proxy;
        Assert.assertNotNull(proxyConfiguration);
        final String proxyServerSetting = hudson.Util.fixEmpty(proxyConfiguration.name);
        Assert.assertNotNull(proxyServerSetting);

        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final UseWebProxyServer innerRunner = tfsRunner.getInnerRunner(UseWebProxyServer.class);
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);

        final HttpProxyServer proxyServer = innerRunner.getServer();
        final LoggingFiltersSourceAdapter adapter = innerRunner.getAdapter();
        final int previousChangeSet;
        try {
            Assert.assertFalse(adapter.proxyWasUsed());

            // setup build
            runUserTrigger(project);

            // first poll should report no changes since last build
            final PollingResult firstPoll = project.poll(taskListener);

            Assert.assertEquals(PollingResult.Change.NONE, firstPoll.change);
            Assert.assertTrue(adapter.proxyWasUsed());

            adapter.reset();
            // make a change in source control
            previousChangeSet = checkInEmptyFile(tfsRunner);
            Assert.assertTrue(previousChangeSet >= 0);
            Assert.assertFalse(adapter.proxyWasUsed());

            // second poll should queue a build
            final AbstractBuild firstBuild = runScmPollTrigger(project);

            Assert.assertNotNull(firstBuild);
            assertBuildSuccess(firstBuild);
            Assert.assertTrue(adapter.proxyWasUsed());
        }
        finally {
            proxyServer.stop();
        }

        // make a change in source control
        final String fileContents = "1. Pick up vegetables.\nPrevious changeset:" + previousChangeSet;
        final int changeSet = checkInFile(tfsRunner, "Now with content.", fileContents);
        Assert.assertTrue(changeSet >= 0);
        adapter.reset();

        // third poll should claim "no changes" due to being unable to reach the proxy server
        final InterceptingTaskListener itl = new InterceptingTaskListener(taskListener);
        // TODO: this takes over 70 seconds to execute, because there's a retry with backoff
        // We might be able to inject an interception that turns off the retry for this operation
        final PollingResult thirdPoll = project.poll(itl);
        Assert.assertEquals("Error during polling => NO_CHANGES", PollingResult.NO_CHANGES, thirdPoll);
        final List<String> fatalErrors = itl.getFatalErrors();
        Assert.assertEquals(1, fatalErrors.size());
        Assert.assertFalse(adapter.proxyWasUsed());
    }

    public static class UseWebProxyServer extends CurrentChangesetInjector {

        private final LoggingFiltersSourceAdapter adapter;
        private final HttpProxyServer server;

        public UseWebProxyServer() {
            adapter = new LoggingFiltersSourceAdapter();
            server =
                DefaultHttpProxyServer
                    .bootstrap()
                    .withPort(0 /* "...let the system pick up an ephemeral port in a bind operation." */)
                    .withFiltersSource(adapter)
                    .start();

        }

        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home) throws Exception {
            super.decorateHome(jenkinsRule, home);
            final InetSocketAddress proxyAddress = server.getListenAddress();

            final File proxyXmlFile = new File(home, "proxy.xml");
            XmlHelper.pokeValue(proxyXmlFile, "/proxy/name", proxyAddress.getHostName());
            XmlHelper.pokeValue(proxyXmlFile, "/proxy/port", Integer.toString(proxyAddress.getPort(), 10));
        }

        public HttpProxyServer getServer() {
            return server;
        }

        public LoggingFiltersSourceAdapter getAdapter() {
            return adapter;
        }
    }

    /**
     * Injects some values into the last <code>build.xml</code> to pretend we're up-to-date with TFS.
     */
    public static class CurrentChangesetInjector extends EndToEndTfs.StubRunner {

        @Override public void decorateHome(final JenkinsRule jenkinsRule, final File home)
                throws Exception {
            super.decorateHome(jenkinsRule, home);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String jobFolder = parent.getJobFolder();
            final String lastBuildXmlPath = jobFolder + "builds/2015-07-15_20-37-42/build.xml";
            final File lastBuildXmlFile = new File(home, lastBuildXmlPath);

            final String projectPath = parent.getPathInTfvc();
            final String serverUrl = getHelper().getServerUrl();
            final Server server = parent.getServer();
            final MockableVersionControlClient vcc = server.getVersionControlClient();
            final int latestChangesetID = vcc.getLatestChangesetID();
            final String changesetVersion = String.valueOf(latestChangesetID);

            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.model.WorkspaceConfiguration/projectPath", projectPath);
            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.model.WorkspaceConfiguration/serverUrl", serverUrl);

            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.TFSRevisionState/changesetVersion", changesetVersion);
            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.TFSRevisionState/projectPath", projectPath);

        }
    }
}
