package hudson.plugins.tfs;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import hudson.model.FreeStyleProject;
import hudson.plugins.tfs.commands.DetailedHistoryCommand;
import hudson.util.StreamTaskListener;

import org.jvnet.hudson.test.recipes.LocalData;

public class TeamFoundationServerScmIntegrationTest extends HudsonTestCase {

    @Override
    protected void tearDown() throws Exception {
        System.setProperty(DetailedHistoryCommand.IGNORE_DATE_CHECK_ON_CHANGE_SET, "false");
        super.tearDown();
    }
    
    /**
     * Asserts that polling now longer throws an exception.
     * @throws Exception thrown if problem
     */
    @LocalData
    @Bug(4330)
    public void testThatPollingTfsDoesNotThrowNPE() throws Exception {
        System.setProperty(DetailedHistoryCommand.IGNORE_DATE_CHECK_ON_CHANGE_SET, "true");
        FreeStyleProject project = (FreeStyleProject) hudson.getItem("4330");
        assertThat(project.pollSCMChanges(new StreamTaskListener(System.out)), is(true));
    }
}