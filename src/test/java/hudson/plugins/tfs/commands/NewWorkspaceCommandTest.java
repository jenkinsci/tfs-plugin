package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import hudson.plugins.tfs.commands.NewWorkspaceCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class NewWorkspaceCommandTest {
    
    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https//tfs02.codeplex.com");
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new NewWorkspaceCommand(config, "TheWorkspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspace /new TheWorkspaceName /server:https//tfs02.codeplex.com /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
