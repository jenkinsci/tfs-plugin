package hudson.plugins.tfs;

import hudson.Extension;
import hudson.Util;
import hudson.console.AnnotatedLargeText;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.util.MediaType;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.StreamTaskListener;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Triggers a build when we receive a VSTS post-push web hook.
 */
public class TeamPushTrigger extends Trigger<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(TeamPushTrigger.class.getName());

    @DataBoundConstructor
    public TeamPushTrigger() {
    }

    public void execute(final GitCodePushedEventArgs gitCodePushedEventArgs, final CommitParameterAction commitParameterAction) {
        // TODO: Consider executing the poll + queue asynchronously
        final Runner runner = new Runner(gitCodePushedEventArgs, commitParameterAction);
        runner.run();
    }

    public File getLogFile() {
        return new File(job.getRootDir(), "team-polling.log");
    }

    // TODO: This was inspired by SCMTrigger.Runner; it would be worth extracting something for re-use
    public class Runner implements Runnable {

        private final GitCodePushedEventArgs gitCodePushedEventArgs;
        private final CommitParameterAction commitParameterAction;

        public Runner(final GitCodePushedEventArgs gitCodePushedEventArgs, final CommitParameterAction commitParameterAction) {
            this.gitCodePushedEventArgs = gitCodePushedEventArgs;
            this.commitParameterAction = commitParameterAction;
        }

        private SCMTriggerItem job() {
            return SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
        }

        private boolean runPolling() {
            final String failedToRecord = "Failed to record SCM polling for " + job;
            try {
                final StreamTaskListener listener = new StreamTaskListener(getLogFile(), MediaType.UTF_8);

                try {
                    final PrintStream logger = listener.getLogger();
                    final long startTimeMillis = System.currentTimeMillis();
                    final Date date = new Date(startTimeMillis);
                    logger.println("Started on "+ DateFormat.getDateTimeInstance().format(date));
                    final boolean result = job().poll(listener).hasChanges();
                    final long endTimeMillis = System.currentTimeMillis();
                    logger.println("Done. Took "+ Util.getTimeSpanString(endTimeMillis - startTimeMillis));
                    if (result) {
                        logger.println("Changes found");
                    }
                    else {
                        logger.println("No changes");
                    }
                    return result;
                }
                catch (final Error e) {
                    e.printStackTrace(listener.error(failedToRecord));
                    LOGGER.log(Level.SEVERE, failedToRecord,e);
                    throw e;
                }
                catch (final RuntimeException e) {
                    e.printStackTrace(listener.error(failedToRecord));
                    LOGGER.log(Level.SEVERE, failedToRecord,e);
                    throw e;
                }
                finally {
                    listener.close();
                }
            }
            catch (final IOException e) {
                LOGGER.log(Level.SEVERE, failedToRecord, e);
                return false;
            }
        }

        @Override
        public void run() {
            if (runPolling()) {
                final String changesDetected = "SCM changes detected in " + job.getFullDisplayName() + ".";
                final SCMTriggerItem p = job();
                final String name = " #" + p.getNextBuildNumber();
                final String pushedBy = gitCodePushedEventArgs.pushedBy;
                TeamPushCause cause;
                try {
                    cause = new TeamPushCause(getLogFile(), pushedBy);
                }
                catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to parse the polling log", e);
                    cause = new TeamPushCause(pushedBy);
                }
                final int quietPeriod = p.getQuietPeriod();
                final CauseAction causeAction = new CauseAction(cause);
                final QueueTaskFuture<?> queueTaskFuture = p.scheduleBuild2(quietPeriod, causeAction, commitParameterAction);
                if (queueTaskFuture != null) {
                    LOGGER.info(changesDetected + " Triggering " + name);
                }
                else {
                    LOGGER.info(changesDetected + " Job is already in the queue");
                }
            }
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(final Item item) {
            return item instanceof Job
                    && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to TFS/Team Services";
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        if (job == null) {
            return Collections.emptyList();
        }

        return Collections.singleton(new TeamPollingAction());
    }

    public final class TeamPollingAction implements Action {

        @Override
        public String getIconFileName() {
            return "clipboard.png";
        }

        @Override
        public String getDisplayName() {
            return "TFS/Team Services hook log";
        }

        @Override
        public String getUrlName() {
            return "TeamPollLog";
        }

        // the following methods are called from TeamPushTrigger/TeamPollingAction/index.jelly

        @SuppressWarnings("unused")
        public Job<?, ?> getOwner() {
            return job;
        }

        @SuppressWarnings("unused")
        public String getLog() throws IOException {
            return Util.loadFile(getLogFile());
        }

        @SuppressWarnings("unused")
        public void writeLogTo(XMLOutput out) throws IOException {
            final File logFile = getLogFile();
            final AnnotatedLargeText<TeamPollingAction> text =
                    new AnnotatedLargeText<TeamPollingAction>(logFile, MediaType.UTF_8, true, this);
            final Writer writer = out.asWriter();
            text.writeHtmlTo(0, writer);
        }

    }
}
