package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.Reader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

import hudson.model.User;
import hudson.plugins.tfs.SwedishLocaleTestCase;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.tasks.Mailer;

import org.junit.Test;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;

public class ProjectTest extends SwedishLocaleTestCase {

    private com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change createServerChange() {
        final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item serverItem
            = new com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item();
        serverItem.setItemType(ItemType.FILE);
        serverItem.setServerItem("$/tfsandbox");
        final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange
            = new Change(serverItem, ChangeType.ADD, null);
        return serverChange;
    }

    @Test
    public void assertConvertServerChange() throws Exception {
        final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange = createServerChange();
        
        final Item actual = Project.convertServerChange(serverChange);
        
        assertEquals("$/tfsandbox", actual.getPath());
        assertEquals("add", actual.getAction());
    }
    
    private UserLookup createMockUserLookup(String accountName, String displayName, String emailAddress) {
        UserLookup userLookup = mock(UserLookup.class);
        User user = mock(User.class);
        // this portion stolen from User.get()
        final String id = accountName.replace('\\', '_').replace('/', '_').replace('<','_')
                .replace('>','_');  // 4 replace() still faster than regex
        // end stolen portion
        when(user.getId()).thenReturn(id);
        when(user.getDisplayName()).thenReturn(displayName);
        when(user.getProperty(Mailer.UserProperty.class)).thenReturn(new Mailer.UserProperty(emailAddress));

        when(userLookup.find(accountName)).thenReturn(user);
        return userLookup;
    }
    
    @Test
    public void assertConvertServerChangeset() throws Exception {
        final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change serverChange = createServerChange();
        final String comment = "Created team project folder $/tfsandbox via the Team Project Creation Wizard";
        final Calendar juneTwentySeventh = Util.getCalendar(2008, 06, 27, 11, 16, 06);
        final String userString = "EXAMPLE\\ljenkins";
        Changeset serverChangeset = new Changeset(userString, comment, null, null);
        serverChangeset.setChangesetID(12472);
        serverChangeset.setCommitter(userString);
        serverChangeset.setDate(juneTwentySeventh);
        final Change[] changes = new Change[] { serverChange };
        serverChangeset.setChanges(changes);
        final String userDisplayName = "Leeroy Jenkins";
        final String userEmailAddress = "leeroy.jenkins@example.com";
        final UserLookup userLookup = createMockUserLookup(userString, userDisplayName, userEmailAddress);

        hudson.plugins.tfs.model.ChangeSet actual = Project.convertServerChangeset(serverChangeset, userLookup);

        final User author = actual.getAuthor();
        assertEquals("The version was incorrect", "12472", actual.getVersion());
        assertEquals("The author's user ID was incorrect", "EXAMPLE_ljenkins", author.getId());
        assertEquals("The author's display name was incorrect", userDisplayName, author.getDisplayName());
        final String actualEmailAddress = author.getProperty(Mailer.UserProperty.class).getAddress();
        assertEquals("The author's e-mail address was incorrect", userEmailAddress, actualEmailAddress);
        assertEquals("The date was incorrect", juneTwentySeventh.getTime(), actual.getDate());
        assertEquals("The comment was incorrect", comment, actual.getComment());

        Item item = actual.getItems().get(0);
        assertEquals("The item path was incorrect", "$/tfsandbox", item.getPath());
        assertEquals("The item action was incorrect", "add", item.getAction());

    }
}
