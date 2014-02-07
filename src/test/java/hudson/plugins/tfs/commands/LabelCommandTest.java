package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LabelCommandTest {

    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https://tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");

        MaskedArgumentListBuilder arguments = new LabelCommand(config, "int_build.10", "Jenkins-JOB-MASTER", ".").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("label int_build.10 . -version:WJenkins-JOB-MASTER -comment:Automatically_applied_by_Jenkins_TFS_plugin -noprompt -recursive -server:https://tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
}
