package hudson.plugins.tfs.commands;

import java.io.PrintStream;
import java.lang.IllegalArgumentException;
import java.util.Collection;

import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedListener;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationStartedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationStartedListener;
import com.microsoft.tfs.core.clients.versioncontrol.events.VersionControlEventEngine;
import com.microsoft.tfs.core.clients.versioncontrol.events.UnshelveShelvesetCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.UnshelveShelvesetStartedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.UnshelveException;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Conflict;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Failure;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.PendingChange;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Shelveset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.ProcessType;
import com.microsoft.tfs.core.clients.versioncontrol.UnshelveResult;

import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;


public class UnshelveToWorkFolderCommand extends AbstractCallableCommand implements Callable<Void, Exception>, OperationCompletedListener, OperationStartedListener 
{
    private static final String ConflictTemplate                    = "Conflict found with '%s'.";
    private static final String FailureTemplate                     = "Failure with '%s' : '%s'.";
    private static final String FormatTemplate                      = "Wrong format for shelveset '%s'. The correct format is <name>:<user>.";
    private static final String NoChangesUnshelved                  = "No changes unshelved.";
    private static final String NoChangesUnshelvedExceptionTemplate = "Unshelve Exception reported for shelveset '%s:%s' : '%s'";
    private static final String PendingChangeTemplate               = "Unshelved '%s'";
    private static final String StartTemplate                       = "Unshelving shelveset '%s:%s' to workspace '%s'...";
    private static final String StopTemplate                        = "Completed unshelving '%s:%s'.";

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
            if (part.length != 2)
                throw new IllegalArgumentException (String.format (FormatTemplate, shelveSet));

            try
            {
                UnshelveResult result = workspace.unshelve (part[0], part[1], null, null, null, true, true);

                for (PendingChange change : result.changes()) {
                    logger.println (String.format (PendingChangeTemplate, change.getServerItem(), change.getLocalItem()));
                }
                for (Conflict conflict : result.getConflicts ()) {
                    logger.println (String.format (ConflictTemplate, conflict.getServerPath()));
                }
                for (Failure failure : result.getStatus().getFailures ()) {
                    logger.println (String.format (FailureTemplate, failure.getLocalItem(), failure.getFormattedMessage()));
                }
            }
            catch (UnshelveException e)
            {
                if (e.getMessage().equals (NoChangesUnshelved)) {
                    logger.println (String.format (NoChangesUnshelvedExceptionTemplate, part[0], part[1], workspace.getName()));
                } else {
                    throw new UnshelveException (e);
                }
            }
        }
        eventEngine.removeOperationStartedListener(this);
        eventEngine.removeOperationCompletedListener(this);

        return null;
    }


    public void onOperationStarted(final OperationStartedEvent e)
    {
        UnshelveShelvesetStartedEvent event = (UnshelveShelvesetStartedEvent) e;
        
        if (event.getProcessType() == ProcessType.UNSHELVE) {
            logger.println (String.format (StartTemplate, event.getShelveset().getName(), event.getShelveset().getOwnerName(), event.getWorkspace().getName()));
        }
    }


    public void onOperationCompleted(final OperationCompletedEvent e)
    {
        UnshelveShelvesetCompletedEvent event = (UnshelveShelvesetCompletedEvent) e;

        if (event.getProcessType() == ProcessType.UNSHELVE) {
            logger.println (String.format (StopTemplate, event.getShelveset().getName(), event.getShelveset().getOwnerName())); 
        }
    }

}
