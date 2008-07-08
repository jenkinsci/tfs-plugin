package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;

import hudson.plugins.tfs.commands.NewWorkspaceCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class NewWorkspaceCommandTest {
    
    @Test
    public void assertArguments() {
        MaskedArgumentListBuilder arguments = new NewWorkspaceCommand("TheWorkspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspace /new TheWorkspaceName", arguments.toStringWithQuote());
    }
}
