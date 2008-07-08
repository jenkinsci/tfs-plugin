package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.plugins.tfs.commands.DeleteWorkspaceCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class DeleteWorkspaceCommandTest {

    @Test
    public void assertArguments() {
        MaskedArgumentListBuilder arguments = new DeleteWorkspaceCommand("workspacename").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspace /delete workspacename", arguments.toStringWithQuote());
    }
}
