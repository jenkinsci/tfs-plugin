package hudson.plugins.tfs;

import hudson.model.Action;
import hudson.plugins.tfs.util.QueryString;
import hudson.plugins.tfs.util.UriHelper;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures the details of the TFS/Team Services build which triggered us.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamBuildDetailsAction implements Action, Serializable {
    private static final long serialVersionUID = 1L;

    public Map<String, String> buildVariables = new HashMap<String, String>();
    public String buildUrl;

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

    @Override
    public String getIconFileName() {
        // TODO: find an appropriate icon
        return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
        return "TFS/Team Services build";
    }

    @Override
    public String getUrlName() {
        // TODO: set this once we add an index.jelly with useful information
        return null;
    }

    // the following methods are called from this/summary.jelly

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
