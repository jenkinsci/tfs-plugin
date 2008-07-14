package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.util.ArgumentListBuilder;

import org.junit.Test;

public class AbstractCommandTest {

    @Test
    public void assertAddingServerArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https://tfs02.codeplex.com");
        
        AbstractCommand command = new AbstractCommandImpl(config);
        ArgumentListBuilder builder = new ArgumentListBuilder();
        command.addServerArgument(builder);
        assertEquals("The server URL was incorrect", "/server:https://tfs02.codeplex.com", builder.toCommandArray()[0]);
    }
    
    @Test
    public void assertAddingUserCredentials() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("user");
        stub(config.getUserPassword()).toReturn("password");
        
        AbstractCommand command = new AbstractCommandImpl(config);
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        command.addLoginArgument(builder);
        assertEquals("The login argument was incorrect", "/login:user,password", builder.toCommandArray()[0]);
        assertTrue("The login argument was not masked", builder.toMaskArray()[0]);
    }
    
    @Test
    public void assertNotAddingInvalidUserCredentials() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("user");
        stub(config.getUserPassword()).toReturn(null);
        
        AbstractCommand command = new AbstractCommandImpl(config);
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        command.addLoginArgument(builder);
        assertTrue("The login argument was added", builder.toCommandArray().length == 0);
        assertTrue("The login argument was added", builder.toMaskArray().length == 0);
    }
    
    @Test
    public void assertNotAddingUserCredentialsForEmptyName() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("");
        stub(config.getUserPassword()).toReturn("");
        
        AbstractCommand command = new AbstractCommandImpl(config);
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        command.addLoginArgument(builder);
        assertTrue("The login argument was added", builder.toCommandArray().length == 0);
        assertTrue("The login argument was added", builder.toMaskArray().length == 0);
    }
    
    @Test
    public void assertAddingUserCredentialsForEmptyPassword() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUserName()).toReturn("aname");
        stub(config.getUserPassword()).toReturn("");
        
        AbstractCommand command = new AbstractCommandImpl(config);
        MaskedArgumentListBuilder builder = new MaskedArgumentListBuilder();
        command.addLoginArgument(builder);
        assertEquals("The login argument was incorrect", "/login:aname,", builder.toCommandArray()[0]);
        assertTrue("The login argument was not masked", builder.toMaskArray()[0]);
    }
    
    private static class AbstractCommandImpl extends AbstractCommand {
        
        public AbstractCommandImpl(ServerConfigurationProvider provider) {
            super(provider);
        }

        public MaskedArgumentListBuilder getArguments() {
            throw new IllegalStateException();
        }
        
    }
}
