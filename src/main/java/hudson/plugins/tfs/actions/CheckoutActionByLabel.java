package hudson.plugins.tfs.actions;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class CheckoutActionByLabel extends AbstractCheckoutAction {
    
	public CheckoutActionByLabel(CheckoutInfo checkoutInfo) {
		super(checkoutInfo);
	}

	public List<ChangeSet> checkout() throws IOException, InterruptedException, ParseException {
		Project project = getProject(this.checkoutInfo.getServer(), this.checkoutInfo.getWorkspacePath());
        
        String label = this.checkoutInfo.getCheckoutStrategyValue();
        
        project.getFiles(this.checkoutInfo.getLocalFolder(), Versionspec.L.name() + label);
        
        if (label != null) {
        	return project.getDetailedHistory(label);
        }
        
        return new ArrayList<ChangeSet>();
    }
	
	
	

}
