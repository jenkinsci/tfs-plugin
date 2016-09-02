package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.TeamRequestedResult;
import hudson.plugins.tfs.model.TeamResultType;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.DirScanner;
import hudson.util.io.Archiver;
import hudson.util.io.ArchiverFactory;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TeamCollectResultsPostBuildAction extends Recorder implements SimpleBuildStep {

    private static final String TEAM_RESULTS = "team-results";
    static final String TEAM_RESULTS_ZIP = "team-results.zip";

    private List<TeamRequestedResult> requestedResults = new ArrayList<TeamRequestedResult>();

    @DataBoundConstructor
    public TeamCollectResultsPostBuildAction() {

    }

    public List<TeamRequestedResult> getRequestedResults() {
        return requestedResults;
    }

    @DataBoundSetter
    public void setRequestedResults(final List<TeamRequestedResult> requestedResults) {
        this.requestedResults = requestedResults;
    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener) throws InterruptedException, IOException {
        // TODO: do we want to emit an error or warning like the following?
        /*
        if (requestedResults == null || requestedResults.size() == 0) {
            final String template = "No results were requested.  Aborting the '%s' post-build action.";
            final String message = String.format(template, displayName);
            listener.error(message);
            return;
        }
        */

        final PrintStream logger = listener.getLogger();
        logger.print("Recording results...");
        final File rootDir = run.getRootDir();
        final File resultsRoot = new File(rootDir, TEAM_RESULTS);
        for (final TeamRequestedResult requestedResult : requestedResults) {
            final TeamResultType teamResultType = requestedResult.getTeamResultType();
            final String folderName = teamResultType.getFolderName();
            logger.print(" " + teamResultType.getDisplayName());
            final File resultFolder = new File(resultsRoot, folderName);
            //noinspection ResultOfMethodCallIgnored
            resultFolder.mkdirs();
            final String includes = StringUtils.join(requestedResult.getPatternList(), ",");
            final FilePath resultPath = new FilePath(resultFolder);
            final int numCopied = workspace.copyRecursiveTo(includes, resultPath);
            logger.print(" (" + numCopied + " file" + ((numCopied == 1) ? "" : "s") + ")");
        }
        logger.print(". Compressing...");
        final ArchiverFactory zip = ArchiverFactory.ZIP;
        final File resultsZipFile = new File(rootDir, TEAM_RESULTS_ZIP);
        final FileOutputStream outputStream = new FileOutputStream(resultsZipFile);
        try {
            final Archiver archiver = zip.create(outputStream);
            try {
                final DirScanner scanner = new DirScanner.Glob(TEAM_RESULTS + "/**", null, false);
                scanner.scan(rootDir, archiver);
            }
            finally {
                IOUtils.closeQuietly(archiver);
            }
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
        FileUtils.deleteDirectory(resultsRoot);
        TeamResultsAction.addToRun(run);
        logger.println(" Done.");
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Collect results for TFS/Team Services";
        }
    }
}
