package hudson.plugins.tfs.commands;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintStream;

import com.microsoft.tfs.core.clients.versioncontrol.events.GetEvent;
import hudson.remoting.Callable;
import org.junit.Ignore;
import org.junit.Test;


public class GetFilesToWorkFolderCommandTest extends AbstractCallableCommandTest {

    @Ignore("Finish test when we have MockableWorkspace and MockableVersionControlEventEngine")
    @Test public void assertLogging() throws Exception {
        when(vcc.queryWorkspace(
                isA(String.class),
                isA(String.class))).thenReturn(null);
        final GetFilesToWorkFolderCommand command = new GetFilesToWorkFolderCommand(server, "c:/jenkins/jobs/newJob/workspace", "C618", false);
        final Callable<Void, Exception> callable = command.getCallable();

        callable.call();

        assertLog(
                "Getting version 'C618' to 'c:/jenkins/jobs/newJob/workspace'...",
                "Finished getting version 'C618'."
        );
    }

    @Test public void onGet_typical() throws IOException {
        final GetEvent getEvent = mock(GetEvent.class);
        final String pathToFile = "C:\\.jenkins\\jobs\\typical\\workspace\\TODO.txt";
        when(getEvent.getTargetLocalItem()).thenReturn(pathToFile);
        final GetFilesToWorkFolderCommand cut = new GetFilesToWorkFolderCommand(server, null, null, false, true);
        cut.setLogger(new PrintStream(this.outputStream));

        cut.onGet(getEvent);

        assertLog(
                pathToFile
        );
    }

    @Override protected AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig) {
        return new GetFilesToWorkFolderCommand(serverConfig, "workFolder", "versionSpec", false);
    }
}
