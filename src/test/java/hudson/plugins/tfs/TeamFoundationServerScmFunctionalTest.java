package hudson.plugins.tfs;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import hudson.model.Project;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.XmlHelper;
import hudson.scm.PollingResult;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Tests that exercise real-world functionality, using temporary Jenkins instances.
 * These are so-called functional (L3) tests.
 * Tests may connect to a TFS server identified by the tfs_server_name property.
 */
@Category(IntegrationTests.class)
public class TeamFoundationServerScmFunctionalTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    /*
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

            final String jobFolder = getJobFolder();
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

            final String jobFolder = getJobFolder();
            final String lastBuildXmlPath = jobFolder + "builds/2015-07-15_20-37-42/build.xml";
            final File lastBuildXmlFile = new File(home, lastBuildXmlPath);

            final EndToEndTfs.RunnerImpl parent = getParent();
            final String projectPath = parent.getPathInTfvc();
            final String serverUrl = parent.getServerUrl();
            final Server server = parent.getServer();
            final TFSTeamProjectCollection tpc = server.getTeamProjectCollection();
            final VersionControlClient vcc = tpc.getVersionControlClient();
            final int latestChangesetID = vcc.getLatestChangesetID();
            final String changesetVersion = String.valueOf(latestChangesetID);

            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.model.WorkspaceConfiguration/projectPath", projectPath);
            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.model.WorkspaceConfiguration/serverUrl", serverUrl);

            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.TFSRevisionState/changesetVersion", changesetVersion);
            XmlHelper.pokeValue(lastBuildXmlFile, "/build/actions/hudson.plugins.tfs.TFSRevisionState/projectPath", projectPath);

        }
    }
}
