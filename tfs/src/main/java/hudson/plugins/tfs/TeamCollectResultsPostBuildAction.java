package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.TeamResult;
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

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class TeamCollectResultsPostBuildAction extends Recorder implements SimpleBuildStep {

    private static final String TEAM_RESULTS = "team-results";

    @DataBoundConstructor
    public TeamCollectResultsPostBuildAction() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener) throws InterruptedException, IOException {
        final TeamBuildDetailsAction action = run.getAction(TeamBuildDetailsAction.class);
        final DescriptorImpl descriptor = getDescriptor();
        final String displayName = descriptor.getDisplayName();
        if (action == null) {
            final String template = "No TeamBuildDetailsAction found; the '%s' post-build action is designed to be used along with the Jenkins Queue Job build task.  Aborting.";
            final String message = String.format(template, displayName);
            listener.error(message);
            return;
        }
        final List<TeamResult> requestedResults = action.requestedResults;
        if (requestedResults == null || requestedResults.size() == 0) {
            final String template = "No results were requested.  Aborting the '%s' post-build action.";
            final String message = String.format(template, displayName);
            listener.error(message);
            return;
        }

        final PrintStream logger = listener.getLogger();
        logger.print("Recording results...");
        final File rootDir = run.getRootDir();
        final File resultsRoot = new File(rootDir, TEAM_RESULTS);
        for (final TeamResult requestedResult : requestedResults) {
            final String name = requestedResult.name;
            logger.print(" " + name);
            final File resultFolder = new File(resultsRoot, name);
            //noinspection ResultOfMethodCallIgnored
            resultFolder.mkdirs();
            final String includes = StringUtils.join(requestedResult.patterns, ",");
            final FilePath resultPath = new FilePath(resultFolder);
            final int numCopied = workspace.copyRecursiveTo(includes, resultPath);
            logger.print(" (" + numCopied + " file" + ((numCopied == 1) ? "" : "s") + ")");
        }
        logger.print(". Compressing...");
        final ArchiverFactory zip = ArchiverFactory.ZIP;
        final File resultsZipFile = new File(rootDir, "team-results.zip");
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
