package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;

public class LabelBasedHistoryCommandTest {
	
    @Test
    public void assertBriefHistoryArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        String label = "myLabel";
        
        MaskedArgumentListBuilder arguments = new LabelBasedHistoryCommand(config, "$/tfsandbox", label).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox -noprompt -version:L" + label + " -recursive -format:detailed -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

}
