package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.StringReader;
import java.util.List;

import hudson.plugins.tfs.commands.UpdateWorkfolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class UpdateWorkfolderCommandTest {

    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new UpdateWorkfolderCommand(config, "localPath").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath /recursive /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertPreviewArgument() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new UpdateWorkfolderCommand(config, "localPath", true).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath /recursive /preview /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertEmptyListWithEmptyOutput() throws Exception {
        UpdateWorkfolderCommand command = new UpdateWorkfolderCommand(mock(ServerConfigurationProvider.class), ".");
        List<String> list = command.parse(new StringReader(""));
        assertEquals("Number of files was incorrect", 0, list.size());
    }

    @Test
    public void assertEmptyListWithNoChangesOutput() throws Exception {
        UpdateWorkfolderCommand command = new UpdateWorkfolderCommand(mock(ServerConfigurationProvider.class), ".");
        List<String> list = command.parse(new StringReader("All files are up to date.\n"));
        assertEquals("Number of files was incorrect", 0, list.size());
    }    
    
    @Test
    public void assertChangesWithChangeOutput() throws Exception {
        StringReader reader = new StringReader(
                "Getting tfsandbox_ms\" +" +
                "\n" +
                "C:\\Projects\\tfs\\tfsandbox_ms:\n" +
                "Getting path1\n" +
                "Getting path with space\n" +
                "Getting readme.txt\n" +
                "\n" +
                "C:\\Projects\\tfs\\tfsandbox_ms\\path1:\n" +
                "Getting pom2.xml\n");
        UpdateWorkfolderCommand command = new UpdateWorkfolderCommand(mock(ServerConfigurationProvider.class), ".");
        List<String> list = command.parse(reader);
        assertEquals("Number of files was incorrect", 4, list.size());
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path1", list.get(0));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path with space", list.get(1));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\readme.txt", list.get(2));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path1\\pom2.xml", list.get(3));
    }
}
