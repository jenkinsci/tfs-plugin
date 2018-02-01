package hudson.plugins.tfs.rm;

import hudson.model.InvisibleAction;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Show release logs link in build page.
 */

@ExportedBean
public class LogSummaryAction extends InvisibleAction {
    private String projectName;
    private int buildNo;
    private String logLink;

    public LogSummaryAction() {

    }

    public LogSummaryAction(final String projectName, final int buildNo, final String logLink) {
        this.projectName = projectName;
        this.buildNo = buildNo;
        this.logLink = logLink;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getBuildNo() {
        return buildNo;
    }

    public String getLogLink() {
        return logLink;
    }
}
