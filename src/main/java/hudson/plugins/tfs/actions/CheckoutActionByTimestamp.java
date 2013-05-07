package hudson.plugins.tfs.actions;

import hudson.model.AbstractBuild;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.util.DateUtil;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CheckoutActionByTimestamp extends AbstractCheckoutAction {

    public CheckoutActionByTimestamp(CheckoutInfo checkoutInfo) {
		super(checkoutInfo);
	}

	public List<ChangeSet> checkout() throws IOException, InterruptedException, ParseException {
		@SuppressWarnings("rawtypes")
		AbstractBuild build = this.checkoutInfo.getAbstractBuild();
		
		Calendar lastBuildTimestamp = build.getPreviousBuild() != null ? build.getPreviousBuild().getTimestamp() : null;
        Calendar currentBuildTimestamp = build.getTimestamp();
        Project project = getProject(this.checkoutInfo.getServer(), this.checkoutInfo.getWorkspacePath());
        project.getFiles(this.checkoutInfo.getLocalFolder(), Versionspec.D.name() + DateUtil.TFS_DATETIME_FORMATTER.get().format(currentBuildTimestamp.getTime()));
        
		if (lastBuildTimestamp != null) {
            return project.getDetailedHistory(lastBuildTimestamp, currentBuildTimestamp);
        }
        
        return new ArrayList<ChangeSet>();
    }

}
