package hudson.plugins.tfs;

import hudson.model.Run;
import hudson.plugins.tfs.util.EndpointHelper;
import hudson.plugins.tfs.util.MediaType;
import hudson.plugins.tfs.util.QueryString;
import hudson.plugins.tfs.util.UriHelper;
import jenkins.model.RunAction2;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

/**
 * Captures the details of the TFS/Team Services build which triggered us.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamBuildDetailsAction implements RunAction2, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(TeamBuildDetailsAction.class.getName());

    public Map<String, String> buildVariables = new HashMap<String, String>();
    public String buildUrl;
    public transient Run<?, ?> run;

    public TeamBuildDetailsAction() {

    }

    public TeamBuildDetailsAction(final Map<String, String> buildVariables) {
        this.buildVariables = new HashMap<String, String>(buildVariables);
        this.buildUrl = determineBuildUrl(buildVariables).toString();
    }

    static URI determineBuildUrl(final Map<String, String> buildVariables) {
        // TODO: eventually call the build REST API to obtain the proper web URL
        final String collectionUri = buildVariables.get("System.TeamFoundationCollectionUri");
        final String projectName = buildVariables.get("System.TeamProject");
        final String buildId = buildVariables.get("Build.BuildId");
        final QueryString query = new QueryString("buildId", buildId);
        final URI result = UriHelper.join(
                collectionUri,
                projectName,
                "_build",
                "index",
                query);
        return result;

    }

    public void doResultsZip(final StaplerRequest req, final StaplerResponse rsp) throws IOException {
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

    @Override
    public String getIconFileName() {
        return "/plugin/tfs/48x48/logo.png";
    }

    @Override
    public String getDisplayName() {
        return "TFS/Team Services build";
    }

    @Override
    public String getUrlName() {
        return "team-build";
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        this.run = r;
    }

    // the following methods are called from this/summary.jelly and this/index.jelly

    @Exported
    public String getBuildNumber() {
        return buildVariables.get("Build.BuildNumber");
    }

    @Exported
    public String getBuildDefinitionName() {
        return buildVariables.get("Build.DefinitionName");
    }

    @Exported
    public String getBuildUrl() {
        return buildUrl;
    }
}
