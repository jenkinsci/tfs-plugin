package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import hudson.plugins.tfs.commands.MapWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class MapWorkfolderCommandTest {
    
    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https//tfs02.codeplex.com");
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new MapWorkfolderCommand(config, "$/serverPath", "localFolder").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /map $/serverPath localFolder /server:https//tfs02.codeplex.com /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertArgumentsWithWorkspace() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https//tfs02.codeplex.com");
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new MapWorkfolderCommand(config, "$/serverPath", "localFolder", "workspaceName").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold /map $/serverPath localFolder /workspace:workspaceName /server:https//tfs02.codeplex.com /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
