package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.List;

import hudson.plugins.tfs.commands.GetFilesToWorkFolderCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;


public class GetFilesToWorkFolderCommandTest {

    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new GetFilesToWorkFolderCommand(config, "localPath").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath -recursive -noprompt -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertPreviewArgument() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new GetFilesToWorkFolderCommand(config, "localPath", true).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath -recursive -preview -noprompt -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertVersionSpecArgument() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new GetFilesToWorkFolderCommand(config, "localPath", false, "C100").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("get localPath -recursive -version:C100 -noprompt -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertEmptyListWithEmptyOutput() throws Exception {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(mock(ServerConfigurationProvider.class), ".");
        List<String> list = command.parse(new StringReader(""));
        assertEquals("Number of files was incorrect", 0, list.size());
    }

    @Test
    public void assertEmptyListWithNoChangesOutput() throws Exception {
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(mock(ServerConfigurationProvider.class), ".");
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
        GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(mock(ServerConfigurationProvider.class), ".");
        List<String> list = command.parse(reader);
        assertEquals("Number of files was incorrect", 4, list.size());
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path1", list.get(0));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path with space", list.get(1));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\readme.txt", list.get(2));
        assertEquals("File name was incorrect", "C:\\Projects\\tfs\\tfsandbox_ms\\path1\\pom2.xml", list.get(3));
    }
}
