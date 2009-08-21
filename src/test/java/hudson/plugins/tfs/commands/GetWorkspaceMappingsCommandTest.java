package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.InputStreamReader;
import java.util.List;

import hudson.plugins.tfs.model.WorkspaceMapping;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class GetWorkspaceMappingsCommandTest {

    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new GetWorkspaceMappingsCommand(config, "workspace").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workfold -workspace:workspace -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertProjectPathWasParsed() throws Exception {
        InputStreamReader reader = new InputStreamReader(GetWorkspaceMappingsCommandTest.class.getResourceAsStream("tf-workfold-list.txt"));
        GetWorkspaceMappingsCommand command = new GetWorkspaceMappingsCommand(mock(ServerConfigurationProvider.class), "workspace");
        List<WorkspaceMapping> mappings = command.parse(reader);
        assertEquals("Project path was incorrect", "$/tfshudsonplugin", mappings.get(0).getProjectPath());
        assertEquals("Project path was incorrect", "$/tfshudsonplugin/folder", mappings.get(1).getProjectPath());
    }
    
    @Test
    public void assertLocalPathWasParsed() throws Exception {
        InputStreamReader reader = new InputStreamReader(GetWorkspaceMappingsCommandTest.class.getResourceAsStream("tf-workfold-list.txt"));
        GetWorkspaceMappingsCommand command = new GetWorkspaceMappingsCommand(mock(ServerConfigurationProvider.class), "workspace");
        List<WorkspaceMapping> mappings = command.parse(reader);
        assertEquals("Local path was incorrect", "C:\\tfshudsonplugin_2\\workfolder1", mappings.get(0).getLocalPath());
        assertEquals("Local path was incorrect", "C:\\tfshudsonplugin_3\\workfolder1", mappings.get(1).getLocalPath());
    }
}
