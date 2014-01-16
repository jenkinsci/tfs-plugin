package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.*;
import hudson.plugins.tfs.commands.LabelCommand;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.BuildVariableResolver;
import hudson.scm.SCM;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Used to create a label on TFS after a build is completed.
 * @author Rodrigo Lopes (rodrigolopes)
 */
public class TFSLabeler extends Notifier {

    private String time;
    private String labelName;

    private static final Logger logger = Logger.getLogger(TFSLabeler.class.getName());

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            super(TFSLabeler.class);
        }

        @Override
        public String getDisplayName() {
            return "Create a label on TFS";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new TFSLabeler(
                    req.getParameter("tfsLabeler.time"),
                    req.getParameter("tfsLabeler.labelName")
            );
        }

    }

    @DataBoundConstructor
    public TFSLabeler(String time, String labelName) {
        this.time = time;
        this.labelName = labelName;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        SCM scm = build.getProject().getScm();
        if (!(scm instanceof TeamFoundationServerScm)) {
            listener.getLogger().println("No label for non TFS project");
            return false;
        }

        FilePath workspace = build.getWorkspace();

        TeamFoundationServerScm tfsScm = (TeamFoundationServerScm) scm;

        boolean buildSucess = Result.SUCCESS.equals(build.getResult());

        String whenCreateLabel = getTime();
        if ("always".equals(whenCreateLabel) || ("success".equals(whenCreateLabel) && buildSucess)) {

            final Launcher localLauncher = launcher != null ? launcher : new Launcher.LocalLauncher(listener);
            final TfTool tool = new TfTool(tfsScm.getDescriptor().getTfExecutable(), localLauncher, listener, workspace);
            Server server = new Server(tool, tfsScm.getServerUrl(build), tfsScm.getUserName(), tfsScm.getUserPassword());

            Computer computer = Computer.currentComputer();
            String normalizedLabelName = computeDynamicValue(build, getLabelName());
            String tfsWorkspace = tfsScm.getWorkspaceName(build, computer);

            try {
                logger.info(String.format("Create label '%s' on workspace '%s'", normalizedLabelName, tfsWorkspace));
                LabelCommand labelCommand = new LabelCommand(server, normalizedLabelName, tfsWorkspace, tfsScm.getLocalPath());
                server.execute(labelCommand.getArguments());
            } catch (Exception e) {
                return false;
            } finally {
                server.close();
            }
        }

        return true;
    }

    /**
     * Replace an expression in the form ${name} in the given String
     * by the value of the matching environment variable.<Br/>
     * Util.replaceMacro(parameterizedValue, new BuildVariableResolver(build.getProject())) did not work.
     */
    private String computeDynamicValue(AbstractBuild build, String parameterizedValue)
            throws IllegalStateException, InterruptedException, IOException {
        String value = parameterizedValue;
        while (value != null && value.contains("${")) {
            int start = value.indexOf("${", 0);
            int end = value.indexOf("}", start);
            String parameter = value.substring(start + 2, end);
            String parameterValue = build.getEnvironment(TaskListener.NULL).get(parameter);
            if (parameterValue == null) {
                throw new IllegalStateException(parameter);
            }
            value = value.substring(0, start) + parameterValue +
                    (value.length() > end + 1 ? value.substring(end + 1) : "");
        }
        logger.fine("oldValue = " + parameterizedValue + "; newValue = " + value);
        return value;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }

    public String getTime() {
        return time;
    }

    public String getLabelName() {
        return labelName;
    }
}
