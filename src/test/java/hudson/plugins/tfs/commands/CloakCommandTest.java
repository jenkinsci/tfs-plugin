package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import hudson.plugins.tfs.commands.MapWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class CloakCommandTest {
    
    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new CloakCommand(config, "$/serverPath/hide").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold -cloak $/serverPath/hide -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertArgumentsWithWorkspace() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new CloakCommand(config, "$/serverPath/hide", "workspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold -cloak $/serverPath/hide -workspace:workspaceName -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
