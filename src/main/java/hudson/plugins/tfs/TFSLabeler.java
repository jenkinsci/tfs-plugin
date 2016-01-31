package hudson.plugins.tfs;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.tfs.commands.LabelCommand;
import hudson.plugins.tfs.model.Server;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Used to create a label in TFS after a build is completed.
 * @author Rodrigo Lopes (rodrigolopes)
 */
public class TFSLabeler extends Notifier {

    private String whenToLabel;
    private String labelName;

    private static final Logger logger = Logger.getLogger(TFSLabeler.class.getName());

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(TFSLabeler.class);
        }

        @Override
        public String getDisplayName() {
            return "Create a label in TFS";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    @DataBoundConstructor
    public TFSLabeler(String whenToLabel, String labelName) {
        this.whenToLabel = whenToLabel;
        this.labelName = labelName;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        SCM scm = build.getProject().getScm();
        if (!(scm instanceof TeamFoundationServerScm)) {
            listener.getLogger().println("Labels are only supported for projects using TFS SCM");
            return false;
        }

        FilePath workspace = build.getWorkspace();

        TeamFoundationServerScm tfsScm = (TeamFoundationServerScm) scm;

        boolean buildSuccess = Result.SUCCESS.equals(build.getResult());

        String whenCreateLabel = getWhenToLabel();
        if ("always".equals(whenCreateLabel) || ("success".equals(whenCreateLabel) && buildSuccess)) {

            final Launcher localLauncher = launcher != null ? launcher : new Launcher.LocalLauncher(listener);
            EnvVars env = build.getEnvironment(listener);
            Server server = new Server(localLauncher, listener, tfsScm.getServerUrl(env), tfsScm.getUserName(), tfsScm.getUserPassword());

            String normalizedLabelName = computeDynamicValue(env, getLabelName());
            String tfsWorkspace = tfsScm.getWorkspaceName(env);

            try {
                logger.info(String.format("Create label '%s' on workspace '%s'", normalizedLabelName, tfsWorkspace));
                LabelCommand labelCommand = new LabelCommand(server, normalizedLabelName, tfsWorkspace, tfsScm.getProjectPath());
                server.execute(labelCommand.getCallable());
            } finally {
                server.close();
            }
        }

        return true;
    }

    /**
     * Replace an expression in the form ${name} in the given String
     * by the value of the matching environment variable or build parameter.<Br/>
     */
    private String computeDynamicValue(EnvVars env, String parameterizedValue) {

        String value = env.expand(parameterizedValue);

        logger.fine("oldValue = " + parameterizedValue + "; newValue = " + value);
        return value;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    public String getWhenToLabel() {
        return whenToLabel;
    }

    public String getLabelName() {
        return labelName;
    }
}
