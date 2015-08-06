package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class GetFilesToWorkFolderCommandTest {

    @Test
    public void assertVersionSpecArgument() {
        final Server config = mock(Server.class);
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new GetFilesToWorkFolderCommand(config, "localPath", "C100").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath -recursive -version:C100 -noprompt -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
