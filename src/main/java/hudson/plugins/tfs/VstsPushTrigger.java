package hudson.plugins.tfs;

import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.queue.QueueTaskFuture;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.StreamTaskListener;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Triggers a build when we receive a VSTS post-push web hook.
 */
public class VstsPushTrigger extends Trigger<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(VstsPushTrigger.class.getName());

    @DataBoundConstructor
    public VstsPushTrigger() {
    }

    public void execute(final String pushedBy) {
        // TODO: Consider executing the poll + queue asynchronously
        final Runner runner = new Runner(pushedBy);
        runner.run();
    }

    public File getLogFile() {
        return new File(job.getRootDir(), "vsts-polling.log");
    }

    // TODO: This was inspired by SCMTrigger.Runner; it would be worth extracting something for re-use
    public class Runner implements Runnable {

        private final String pushedBy;

        public Runner(final String pushedBy) {
            this.pushedBy = pushedBy;
        }

        private SCMTriggerItem job() {
            return SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(job);
        }

        private boolean runPolling() {
            final String failedToRecord = "Failed to record SCM polling for " + job;
            try {
                final StreamTaskListener listener = new StreamTaskListener(getLogFile());

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
                VstsPushCause cause;
                try {
                    cause = new VstsPushCause(getLogFile(), pushedBy);
                }
                catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Failed to parse the polling log", e);
                    cause = new VstsPushCause(pushedBy);
                }
                final CauseAction causeAction = new CauseAction(cause);
                final int quietPeriod = p.getQuietPeriod();
                final QueueTaskFuture<?> queueTaskFuture = p.scheduleBuild2(quietPeriod, causeAction);
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
            return "Build when a change is pushed to VSTS";
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        if (job == null) {
            return Collections.emptyList();
        }

        return Collections.singleton(new VstsPollingAction());
    }

    public final class VstsPollingAction implements Action {

        @Override
        public String getIconFileName() {
            return "clipboard.png";
        }

        @Override
        public String getDisplayName() {
            return "VSTS hook log";
        }

        @Override
        public String getUrlName() {
            return "VstsPollLog";
        }

        // TODO: what else do we need?
    }
}
