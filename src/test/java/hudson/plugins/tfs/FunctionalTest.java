package hudson.plugins.tfs;

import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.PendChangesOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.LockLevel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.VersionControlLabel;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.Project;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.XmlHelper;
import hudson.scm.ChangeLogSet;
import hudson.scm.PollingResult;
import hudson.triggers.SCMTrigger;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.JenkinsRecipe;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    @Test public void createLabel() throws ExecutionException, InterruptedException {
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

        // polling should report no changes
        final PollingResult pollingResult = project.poll(taskListener);

        Assert.assertEquals(PollingResult.Change.NONE, pollingResult.change);

        // trigger build
        final AbstractBuild build = runUserTrigger(project);

        // verify new label created against latestChangesetId
        Assert.assertNotNull(build);
        Assert.assertEquals(Result.SUCCESS, build.getResult());
        final ChangeLogSet changeSet = build.getChangeSet();
        Assert.assertEquals(0, changeSet.getItems().length);
        final TFSRevisionState revisionState = build.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, revisionState.changesetVersion);
        final String owner = IntegrationTestHelper.TestUserName;
        final ChangesetVersionSpec spec = new ChangesetVersionSpec(latestChangesetID);
        final VersionControlLabel[] labels = vcc.queryLabels(generatedLabelName, null, owner, false, null, spec);
        Assert.assertEquals(1, labels.length);
        final VersionControlLabel label = labels[0];
        Assert.assertFalse(StringUtils.isEmpty(label.getComment()));
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
    @EndToEndTfs(EndToEndTfs.StubRunner.class)
    @Test public void newJob() throws InterruptedException, ExecutionException, IOException {
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final EndToEndTfs.RunnerImpl tfsRunner = j.getTfsRunner();
        final Workspace workspace = tfsRunner.getWorkspace();
        final Server server = tfsRunner.getServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        int latestChangesetID;

        // first poll should queue a build because we were never built
        latestChangesetID = vcc.getLatestChangesetID();
        final AbstractBuild firstBuild = runScmPollTrigger(project);

        Assert.assertNotNull(firstBuild);
        Assert.assertEquals(Result.SUCCESS, firstBuild.getResult());
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
        final File todoFile = new File(tfsRunner.getLocalBaseFolderFile(), "TODO.txt");
        todoFile.createNewFile();
        workspace.pendAdd(
                new String[]{todoFile.getAbsolutePath()},
                false,
                null,
                LockLevel.UNCHANGED,
                GetOptions.NONE,
                PendChangesOptions.NONE);
        final int changeSet = tfsRunner.checkIn(tfsRunner.getTestCaseName() + " Add a file.");
        Assert.assertTrue(changeSet >= 0);

        // third poll should trigger a build
        latestChangesetID = vcc.getLatestChangesetID();
        final AbstractBuild secondBuild = runScmPollTrigger(project);

        Assert.assertNotNull(secondBuild);
        Assert.assertEquals(Result.SUCCESS, secondBuild.getResult());
        final ChangeLogSet secondChangeSet = secondBuild.getChangeSet();
        Assert.assertEquals(1, secondChangeSet.getItems().length);
        final TFSRevisionState secondRevisionState = secondBuild.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, secondRevisionState.changesetVersion);
        final List<Cause> secondCauses = secondBuild.getCauses();
        Assert.assertEquals(1, secondCauses.size());
        final Cause secondCause = secondCauses.get(0);
        Assert.assertTrue(secondCause instanceof SCMTrigger.SCMTriggerCause);
        final FilePath jenkinsWorkspace = secondBuild.getWorkspace();
        final FilePath[] workspaceFiles = jenkinsWorkspace.list("*.*", "$tf");
        Assert.assertEquals(1, workspaceFiles.length);
        final FilePath workspaceFile = workspaceFiles[0];
        Assert.assertEquals("TODO.txt", workspaceFile.getName());

        // force a build via a manual trigger
        final AbstractBuild thirdBuild = runUserTrigger(project);

        Assert.assertNotNull(thirdBuild);
        Assert.assertEquals(Result.SUCCESS, thirdBuild.getResult());
        final ChangeLogSet thirdChangeSet = thirdBuild.getChangeSet();
        Assert.assertEquals(0, thirdChangeSet.getItems().length);
        final TFSRevisionState thirdRevisionState = thirdBuild.getAction(TFSRevisionState.class);
        Assert.assertEquals(latestChangesetID, thirdRevisionState.changesetVersion);
        final List<Cause> thirdCauses = thirdBuild.getCauses();
        Assert.assertEquals(1, thirdCauses.size());
        final Cause thirdCause = thirdCauses.get(0);
        Assert.assertTrue(thirdCause instanceof Cause.UserIdCause);
        final FilePath thirdBuildWorkspace = thirdBuild.getWorkspace();
        final FilePath[] thirdBuildWorkspaceFiles = thirdBuildWorkspace.list("*.*", "$tf");
        Assert.assertEquals(1, thirdBuildWorkspaceFiles.length);
        final FilePath thirdBuildWorkspaceFile = thirdBuildWorkspaceFiles[0];
        Assert.assertEquals("TODO.txt", thirdBuildWorkspaceFile.getName());
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
    @Test public void oldPollingFallback() throws IOException {
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
            final String lastBuildXmlPath = jobFolder + "builds/2015-07-10_12-11-34/build.xml";
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
    @EndToEndTfs(CurrentChangesetInjector.class)
    @Test public void upgradeEncodedPassword()
            throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        final String encryptedPassword = "pmJe5VYJg6gr2BdipI1sMGJScFwmT+pZbz7B2jISBrw=";
        final Jenkins jenkins = j.jenkins;
        final TaskListener taskListener = j.createTaskListener();
        final List<Project> projects = jenkins.getProjects();
        final Project project = projects.get(0);
        final TeamFoundationServerScm scm = (TeamFoundationServerScm) project.getScm();
        final Secret passwordSecret = scm.getPassword();
        Assert.assertEquals(encryptedPassword, passwordSecret.getEncryptedValue());
        PollingResult actualPollingResult;

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
            final String serverUrl = parent.getServerUrl();
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
