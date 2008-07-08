package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.plugins.tfs.commands.UnmapWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class UnmapWorkfolderCommandTest {
    
    @Test
    public void assertArguments() {
        MaskedArgumentListBuilder arguments = new UnmapWorkfolderCommand("localFolder").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /unmap localFolder", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertArgumentsWithWorkspace() {
        MaskedArgumentListBuilder arguments = new UnmapWorkfolderCommand("localFolder", "workspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /unmap localFolder /workspace:workspaceName", arguments.toStringWithQuote());
    }
}
