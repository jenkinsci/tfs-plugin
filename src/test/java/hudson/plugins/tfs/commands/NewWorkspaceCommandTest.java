package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.commands.NewWorkspaceCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Before;
import org.junit.Test;

public class NewWorkspaceCommandTest {

    private ServerConfigurationProvider config;
    private MaskedArgumentListBuilder arguments;

    private void init() {
        init(false);
    }

    private void init(boolean localWorkspace) {
        config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        when(config.isLocalWorkspace()).thenReturn(localWorkspace);
        arguments = new NewWorkspaceCommand(config, "TheWorkspaceName").getArguments();
    }

    @Test
    public void assertArgumentsNotNull() {
        init();
        assertNotNull("Arguments were null", arguments);
    }

    @Test
    public void assertArgumentsDefaultValues() {
        init();
        assertEquals("workspace -new TheWorkspaceName;snd\\user_cp -noprompt -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertArgumentsLocalWorkspace() {
        init(true);
        assertEquals("workspace -new TheWorkspaceName;snd\\user_cp -noprompt -location:local -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
