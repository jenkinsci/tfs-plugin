package hudson.plugins.tfs;

import hudson.model.Action;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Captures the details of the TFS/Team Services build which triggered us.
 */
@ExportedBean(defaultVisibility = 999)
public class TeamBuildDetailsAction implements Action, Serializable {
    private static final long serialVersionUID = 1L;

    public Map<String, String> buildVariables = new HashMap<String, String>();

    public TeamBuildDetailsAction() {

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
        return "team-build";
    }
}
