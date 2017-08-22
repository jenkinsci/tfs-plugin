//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import hudson.model.Run;
import hudson.plugins.tfs.util.EndpointHelper;
import hudson.plugins.tfs.util.MediaType;
import jenkins.model.RunAction2;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Added by {@link TeamCollectResultsPostBuildAction} to enable the download of the ZIP
 * file containing the collected results from the build.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamResultsAction implements RunAction2, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(TeamResultsAction.class.getName());

    public transient Run<?, ?> run;

    @Override
    public void onAttached(final Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "team-results";
    }

    public static void addToRun(final Run<?, ?> run) {
        final TeamResultsAction action = new TeamResultsAction();
        run.addAction(action);
    }

    @SuppressWarnings("unused" /* API method */)
    public void doZip(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
        try {
            if (run == null) {
                throw new IllegalArgumentException("There is no associated Run<?,?>");
            }
            final File rootDir = run.getRootDir();
            final File resultsZipFile = new File(rootDir, TeamCollectResultsPostBuildAction.TEAM_RESULTS_ZIP);
            if (!resultsZipFile.isFile()) {
                throw new IllegalArgumentException("There is no results file in this build");
            }
            final FileInputStream resultsZipStream = new FileInputStream(resultsZipFile);
            rsp.setContentType(MediaType.APPLICATION_ZIP);
            final long lastModified = resultsZipFile.lastModified();
            final long contentLength = resultsZipFile.length();
            final String fileName = resultsZipFile.getName();
            try {
                rsp.serveFile(req, resultsZipStream, lastModified, contentLength, fileName);
            }
            finally {
                IOUtils.closeQuietly(resultsZipStream);
            }
        }
        catch (final IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "IllegalArgumentException", e);
            EndpointHelper.error(SC_BAD_REQUEST, e);
        }
        catch (final ForwardToView e) {
            throw e;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Unknown error", e);
            EndpointHelper.error(SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
