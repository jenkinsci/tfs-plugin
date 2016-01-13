package hudson.plugins.tfs.commands;


import java.util.List;
import java.util.ArrayList;

import hudson.remoting.Callable;

import org.junit.Ignore;
import org.junit.Test;


public class UnshelveToWorkFolderCommandTest extends AbstractCallableCommandTest
{
    private List<String> shelveSets;


    public UnshelveToWorkFolderCommandTest ()
    {
        List<String> shelveSets = new ArrayList<String>();
        shelveSets.add ("shelveset:user");
    } 


    @Ignore("Finish test when we have MockableWorkspaces")
    @Test 
    public void assertLogging() throws Exception
    {
        final UnshelveToWorkFolderCommand command = new UnshelveToWorkFolderCommand(server, "c:/jenkins/jobs/newJob/workspace", shelveSets);
        final Callable<Void, Exception> callable = command.getCallable();

        callable.call();

        assertLog("Unshelving shelveset 'shelveset:user' to workspace",
                  "Completed unshelving 'shelveset:user'");
    }


    @Override 
    protected AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig)
    {
        return new UnshelveToWorkFolderCommand(serverConfig, "workFolder", shelveSets);
    }
}

