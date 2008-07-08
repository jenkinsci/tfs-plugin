package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.plugins.tfs.commands.UpdateWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class UpdateWorkfolderCommandTest {

    @Test
    public void assertArguments() {
        MaskedArgumentListBuilder arguments = new UpdateWorkfolderCommand("localPath").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath /recursive", arguments.toStringWithQuote());
    } 
}
