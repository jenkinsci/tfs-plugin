package hudson.plugins.tfs.model;

import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.recipes.LocalData;

import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ChangeLogSetIntegrationTest extends HudsonTestCase {

    /**
     * Asserts that polling now longer throws an exception.
     * @throws Exception thrown if problem
     */
    @LocalData
    @Bug(4943)
    public void testThatLogSetContainsCheckedInByUserReference() throws Exception {
    	HtmlPage page = new WebClient().getPage(hudson.getItem("4943"), "2/changes");
    	assertXPath(page, "//a[@href=\"/user/dude/\"]");
    }
}
