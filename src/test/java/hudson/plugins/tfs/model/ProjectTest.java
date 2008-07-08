package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.List;

import hudson.plugins.tfs.SwedishLocaleTestCase;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class ProjectTest extends SwedishLocaleTestCase {

    @Test
    public void assertGetDetailedHistory() throws Exception {
        Server server = mock(Server.class);
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(
                "-----------------------------------\n" +
                "Changeset: 12472\n" +
                "User:      RNO\\_MCLWEB\n" +
                "Date:      2008-jun-27 11:16:06\n" +
                "\n" +
                "Comment:\n" +
                "Created team project folder $/tfsandbox via the Team Project Creation Wizard\n" +
                "\n" +
                "Items:\n" +
                "  add $/tfsandbox\n"));
        Project project = new Project(server, "$/serverpath");
        List<ChangeSet> list = project.getDetailedHistory(Util.getCalendar(2008, 06, 01), Util.getCalendar(2008, 07, 01));
        assertNotNull("The returned list was null", list);
        assertEquals("The number of change sets in list was incorrect", 1, list.size());
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }

    @Test
    public void assertGetBriefHistory() throws Exception {
        Server server = mock(Server.class);
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n"));
        Project project = new Project(server, "$/serverpath");
        List<ChangeSet> list = project.getBriefHistory(Util.getCalendar(2008, 06, 01), Util.getCalendar(2008, 07, 01));
        assertNotNull("The returned list was null", list);
        assertEquals("The number of change sets in list was incorrect", 1, list.size());
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertGetFiles() throws Exception {
        Server server = mock(Server.class);
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));
        Project project = new Project(server, "$/serverpath");
        project.getFiles(".");
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
}
