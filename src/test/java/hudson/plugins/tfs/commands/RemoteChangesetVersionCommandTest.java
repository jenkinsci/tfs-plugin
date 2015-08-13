package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ChangeType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Item;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.ItemType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.LabelSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LabelVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.model.User;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Server;

import java.io.StringReader;
import java.util.concurrent.Callable;

import hudson.plugins.tfs.model.UserLookup;
import org.junit.Assert;
import org.junit.Test;

public class RemoteChangesetVersionCommandTest extends AbstractCallableCommandTest {

    private static final DateVersionSpec fixedPointInTime = new DateVersionSpec(Util.getCalendar(2013, 07, 02, 15, 40, 50, "UTC"));

    @Test public void assertLoggingWhenChangeset() throws Exception {
        final User user = mock(User.class);
        when(user.getId()).thenReturn("piedefer");
        final UserLookup userLookup = mock(UserLookup.class);
        when(userLookup.find("piedefer")).thenReturn(user);
        final Item item = new Item();
        item.setServerItem("Arithmetica.iTeX");
        item.setItemType(ItemType.FILE);
        final Change serverChange = new Change(item, ChangeType.EDIT, null);
        final Changeset serverChangeset = new Changeset(
                new Change[]{serverChange},
                "I have discovered a truly marvellous proof of this, which this margin is too narrow to contain.",
                null,
                null,
                "piedefer",
                "Pierre de Fermat",
                fixedPointInTime.getDate(),
                1637,
                "piedefer",
                "Pierre de Fermat",
                null
        );
        final Changeset[] serverChangesets = new Changeset[]{serverChangeset};
        when(vcc.queryHistory(
                isA(String.class),
                isA(VersionSpec.class),
                anyInt(),
                isA(RecursionType.class),
                (String) isNull(),
                (VersionSpec) isNull(),
                (VersionSpec) isNull(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean())).thenReturn(serverChangesets);
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(server, "$/RemotePath", LatestVersionSpec.INSTANCE);
        command.setUserLookup(userLookup);
        final Callable<ChangeSet> callable = command.getCallable();

        final ChangeSet actual = callable.call();

        Assert.assertNotNull(actual);
        assertLog(
                "Querying for remote changeset at '$/RemotePath' as of 'T'...",
                "Query result is: Changeset #1637 by 'piedefer' on '2013-07-02T15:40:50Z'."
        );
        Assert.assertEquals(user, actual.getAuthor());
    }

    @Test public void assertLoggingWhenNoResult() throws Exception {
        when(vcc.queryHistory(
                isA(String.class),
                isA(VersionSpec.class),
                anyInt(),
                isA(RecursionType.class),
                (String) isNull(),
                (VersionSpec) isNull(),
                (VersionSpec) isNull(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean())).thenReturn(null);
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(server, "$/RemotePath", LatestVersionSpec.INSTANCE);
        final Callable<ChangeSet> callable = command.getCallable();

        final ChangeSet result = callable.call();

        Assert.assertNull(result);
        assertLog(
                "Querying for remote changeset at '$/RemotePath' as of 'T'...",
                "Query returned no result!"
        );
    }

    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(Server.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(new StringReader(""));
        assertEquals("Change set number was incorrect", "", changesetNumber);
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(Server.class), "$/tfsandbox", fixedPointInTime);
        StringReader reader = new StringReader("No history entries were found for the item and version combination specified.\n\n");
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "", changesetNumber);
    }    
    
    @Test
    public void assertChangesWithChangeOutput() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n");
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(Server.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12495", changesetNumber);
    }    
    
    @Test
    public void assertChangesWithNoComment() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(Server.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12495", changesetNumber);
    }    

    @Test
    public void assertChangesNoEmptyLine() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "12497     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(Server.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12497", changesetNumber);
    }

    @Test public void getVersionSpecificationWhenDateVersionSpec() {
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(null, null, fixedPointInTime);

        final String actual = command.getVersionSpecification();

        assertEquals("D2013-07-02T15:40:51Z", actual);
    }

    @Test public void getVersionSpecificationWhenChangesetVersionSpec() {
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(null, null, new ChangesetVersionSpec(42));

        final String actual = command.getVersionSpecification();

        assertEquals("C42", actual);
    }

    @Test public void getVersionSpecificationWhenLabelVersionSpecWithoutScope() {
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(null, null, new LabelVersionSpec(new LabelSpec("Foo", null)));

        final String actual = command.getVersionSpecification();

        assertEquals("LFoo", actual);
    }

    @Test public void getVersionSpecificationWhenLabelVersionSpecWithScope() {
        final RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(null, null, new LabelVersionSpec(new LabelSpec("Foo", "Bar")));

        final String actual = command.getVersionSpecification();

        assertEquals("LFoo@Bar", actual);
    }
}
