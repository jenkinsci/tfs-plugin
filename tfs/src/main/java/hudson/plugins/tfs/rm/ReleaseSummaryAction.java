package hudson.plugins.tfs.rm;

import hudson.model.InvisibleAction;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Show release summary link in build page.
 */

@ExportedBean
public class ReleaseSummaryAction extends InvisibleAction {
    private String projectName;
    private int buildNo;
    private String releaseLink;

    public ReleaseSummaryAction() {

    }

    public ReleaseSummaryAction(final String projectName, final int buildNo, final String releaseLink) {
        this.projectName = projectName;
        this.buildNo = buildNo;
        this.releaseLink = releaseLink;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getBuildNo() {
        return buildNo;
    }

    public String getReleaseLink() {
        return releaseLink;
    }
}
