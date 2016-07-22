package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import hudson.model.User;
import hudson.plugins.tfs.IntegrationTestHelper;
import hudson.plugins.tfs.IntegrationTests;
import hudson.plugins.tfs.SwedishLocaleTestCase;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.tasks.Mailer;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;
import org.junit.experimental.categories.Category;

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

        //should put this in a utility/getter function somewhere
        String id;
        String[] split = accountName.split("\\\\");
        if (split.length == 2) {
            id = split[1];
        } else {
            id = accountName;
        }

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
        assertEquals("The author's user ID was incorrect", "ljenkins", author.getId());
        assertEquals("The author's display name was incorrect", userDisplayName, author.getDisplayName());
        final String actualEmailAddress = author.getProperty(Mailer.UserProperty.class).getAddress();
        assertEquals("The author's e-mail address was incorrect", userEmailAddress, actualEmailAddress);
        assertEquals("The date was incorrect", juneTwentySeventh.getTime(), actual.getDate());
        assertEquals("The comment was incorrect", comment, actual.getComment());

        Item item = actual.getItems().get(0);
        assertEquals("The item path was incorrect", "$/tfsandbox", item.getPath());
        assertEquals("The item action was incorrect", "add", item.getAction());

    }

    @Test
    public void findLatestUncloakedChangeset_latestIsUncloaked() {
        final List<String> cloakedPaths = Arrays.asList("$/MyProject/A/2", "$/MyProject/B");
        final ChangeSet changeSet42 = createChangeSet(42, "$/MyProject/A/foo", "$/MyProject/A/bar");
        final ChangeSet changeSet43 = createChangeSet(43, "$/MyProject/A/bar");
        final ChangeSet changeSet44 = createChangeSet(44, "$/MyProject/A/foo");
        final List<ChangeSet> changeSets = Arrays.asList(changeSet44, changeSet43, changeSet42);

        final ChangeSet actual = Project.findLatestUncloakedChangeset(cloakedPaths, changeSets);

        Assert.assertEquals("44", actual.getVersion());
    }

    @Test
    public void findLatestUncloakedChangeset_latestIsCloaked() {
        final List<String> cloakedPaths = Arrays.asList("$/MyProject/A/2", "$/MyProject/B");
        final ChangeSet changeSet42 = createChangeSet(42, "$/MyProject/A/foo", "$/MyProject/A/bar");
        final ChangeSet changeSet43 = createChangeSet(43, "$/MyProject/A/bar");
        final ChangeSet changeSet44 = createChangeSet(44, "$/MyProject/A/2/foo");
        final List<ChangeSet> changeSets = Arrays.asList(changeSet44, changeSet43, changeSet42);

        final ChangeSet actual = Project.findLatestUncloakedChangeset(cloakedPaths, changeSets);

        Assert.assertEquals("43", actual.getVersion());
    }

    @Test
    public void findLatestUncloakedChangeset_everythingIsCloaked() {
        final List<String> cloakedPaths = Arrays.asList("$/MyProject/A/2", "$/MyProject/B");
        final ChangeSet changeSet42 = createChangeSet(42, "$/MyProject/A/2/foo", "$/MyProject/B/bar");
        final ChangeSet changeSet43 = createChangeSet(43, "$/MyProject/B/bar");
        final ChangeSet changeSet44 = createChangeSet(44, "$/MyProject/A/2/foo");
        final List<ChangeSet> changeSets = Arrays.asList(changeSet44, changeSet43, changeSet42);

        final ChangeSet actual = Project.findLatestUncloakedChangeset(cloakedPaths, changeSets);

        Assert.assertEquals(null, actual);
    }

    private static ChangeSet createChangeSet(final int version, final String... itemPaths) {
        final String stringVersion = Integer.toString(version);
        final Calendar calendar = Util.getCalendar(2016, 1, 5, 10, version, 0);
        final Date date = calendar.getTime();
        final ChangeSet result = new ChangeSet(stringVersion, date, "ljenkins", "synthetic for testing");

        for (final String itemPath : itemPaths) {
            final Item item = new Item(itemPath, "edit");
            result.add(item);
        }

        return result;
    }

    @Test
    public void isChangesetFullyCloaked_nullCloakedPaths() {
        final List<String> changesetPaths = Arrays.asList("$/foo", "$/foo/bar.baz");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, null);

        Assert.assertEquals(false, actual);
    }


    @Test
    public void isChangesetFullyCloaked_independentCloakedPaths() {
        final List<String> changesetPaths = Arrays.asList("$/foo", "$/foo/bar.baz");
        final List<String> cloakedPaths = Arrays.asList("$/fizz", "$/fizz/FizzBuzz.java");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(false, actual);
    }

    @Test
    public void isChangesetFullyCloaked_caseInsensitiveCloakedPaths() {
        final List<String> changesetPaths = Arrays.asList("$/foo/bar/test.baz");
        final List<String> cloakedPaths = Arrays.asList("$/fOo/bAr/");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void isChangesetFullyCloaked_cloakingChild() {
        final List<String> changesetPaths = Arrays.asList("$/foo", "$/foo/bar.baz");
        final List<String> cloakedPaths = Collections.singletonList("$/foo/bar.baz");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(false, actual);
    }

    @Test
    public void isChangesetFullyCloaked_partiallyCloakedPaths() {
        final List<String> changesetPaths = Arrays.asList("$/foo", "$/bar");
        final List<String> cloakedPaths = Collections.singletonList("$/foo");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(false, actual);
    }

    @Test
    public void isChangesetFullyCloaked_fullyCloakedPath() {
        final List<String> changesetPaths = Arrays.asList("$/foo", "$/foo/bar.baz");
        final List<String> cloakedPaths = Collections.singletonList("$/foo");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void isChangesetFullyCloaked_fullyCloakedPaths() {
        final List<String> changesetPaths = Collections.singletonList("$/foo/bar.baz");
        final List<String> cloakedPaths = Arrays.asList("$/foo", "$/bar");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(true, actual);
    }

    @Test
    public void isChangesetFullyCloaked_manyToMany() {
        final List<String> changesetPaths = Arrays.asList("$/foo/bar.baz", "$/bar/foo.baz");
        final List<String> cloakedPaths = Arrays.asList("$/foo", "$/bar");

        final boolean actual = Project.isChangesetFullyCloaked(changesetPaths, cloakedPaths);

        Assert.assertEquals(true, actual);
    }

    @Category(IntegrationTests.class)
    @Test public void getDetailedHistory_singleVersionSpec() throws URISyntaxException, IOException {
        final IntegrationTestHelper helper = new IntegrationTestHelper();
        final String serverUrl = helper.getServerUrl();
        final String userName = helper.getUserName();
        final String userPassword = helper.getUserPassword();
        final Server server = new Server(null, null, serverUrl, userName, userPassword);
        try {
            final Project project = new Project(server, "$/FunctionalTests");
            final UserLookup userLookup = mock(UserLookup.class);
            final User fakeUser = mock(User.class);
            when(userLookup.find(isA(String.class))).thenReturn(fakeUser);
            project.setUserLookup(userLookup);
            final MockableVersionControlClient vcc = server.getVersionControlClient();
            final int latestChangesetID = vcc.getLatestChangesetID();
            final ChangesetVersionSpec spec = new ChangesetVersionSpec(latestChangesetID);
            final String singleVersionSpecString = RemoteChangesetVersionCommand.toString(spec);

            final List<ChangeSet> actual = project.getDetailedHistory(singleVersionSpecString);

            Assert.assertEquals(1, actual.size());
            final ChangeSet changeSet = actual.get(0);
            final String latestChangesetString = Integer.toString(latestChangesetID, 10);
            Assert.assertEquals(latestChangesetString, changeSet.getVersion());
        }
        finally {
            server.close();
        }
    }
}
