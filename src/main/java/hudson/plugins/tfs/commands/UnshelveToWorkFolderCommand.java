package hudson.plugins.tfs.commands;

import java.io.PrintStream;
import java.util.Collection;

import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedListener;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationStartedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationStartedListener;
import com.microsoft.tfs.core.clients.versioncontrol.events.VersionControlEventEngine;
import com.microsoft.tfs.core.clients.versioncontrol.events.UnshelveShelvesetCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.UnshelveShelvesetStartedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Shelveset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.ProcessType;

import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;


public class UnshelveToWorkFolderCommand extends AbstractCallableCommand implements Callable<Void, Exception>, OperationCompletedListener, OperationStartedListener 
{
    private static final String PendingChangeTemplate = "Unshelving '%s'";
    private static final String StartTemplate         = "Unshelving shelveset '%s:%s' to workspace '%s'...";
    private static final String StopTemplate          = "Completed unshelving '%s:%s'.";


    private final String              workFolder;
    private final Collection<String>  shelveSets;
    private       PrintStream         logger;


    public UnshelveToWorkFolderCommand(final ServerConfigurationProvider server, final String workFolder, final Collection<String> shelveSets) 
    {
        super(server);
        this.workFolder = workFolder;
        this.shelveSets = shelveSets;
    }


    @Override
    public Callable<Void, Exception> getCallable()
    {
        return this;
    }


    void setLogger(final PrintStream logger)
    {
        this.logger = logger;
    }


    public Void call() throws Exception
    {
        final Server                       server      = createServer();

        final MockableVersionControlClient vcc         = server.getVersionControlClient();
        final TaskListener                 listener    = server.getListener();

        final Workspace                    workspace   = vcc.getWorkspace(workFolder);
        final VersionControlEventEngine    eventEngine = vcc.getEventEngine();

        logger = listener.getLogger();

        eventEngine.addOperationStartedListener(this);
        eventEngine.addOperationCompletedListener(this);
        for (String shelveSet : shelveSets)
        {
            String part[] = shelveSet.split(":");
            workspace.unshelve (part[0], part[1], null);
        }
        eventEngine.removeOperationStartedListener(this);
        eventEngine.removeOperationCompletedListener(this);

        return null;
    }


    public void onOperationStarted(final OperationStartedEvent e)
    {
        UnshelveShelvesetStartedEvent event = (UnshelveShelvesetStartedEvent) e;
        
        if (event.getProcessType() == ProcessType.UNSHELVE)
        {
            logger.println (String.format (StartTemplate, event.getShelveset().getName(), event.getShelveset().getOwnerDisplayName(), event.getWorkspace().getName()));
            for (PendingChange change : event.getChanges())
            {
                logger.println (String.format (PendingChangeTemplate, change.getServerItem(), change.getLocalItem()));
            }
       }
    }


    public void onOperationCompleted(final OperationCompletedEvent e)
    {
        UnshelveShelvesetCompletedEvent event = (UnshelveShelvesetCompletedEvent) e;

        if (event.getProcessType() == ProcessType.UNSHELVE)
        {
            logger.println (String.format (StopTemplate, event.getShelveset().getName(), event.getShelveset().getOwnerDisplayName())); 
        }
    }

}
