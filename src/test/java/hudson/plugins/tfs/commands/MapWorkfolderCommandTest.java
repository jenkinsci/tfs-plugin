package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.plugins.tfs.commands.MapWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class MapWorkfolderCommandTest {
    
    @Test
    public void assertArguments() {
        MaskedArgumentListBuilder arguments = new MapWorkfolderCommand("$/serverPath", "localFolder").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /map $/serverPath localFolder", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertArgumentsWithWorkspace() {
        MaskedArgumentListBuilder arguments = new MapWorkfolderCommand("$/serverPath", "localFolder", "workspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /map $/serverPath localFolder /workspace:workspaceName", arguments.toStringWithQuote());
    }
}
