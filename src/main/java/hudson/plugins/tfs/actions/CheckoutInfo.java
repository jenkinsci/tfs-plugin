package hudson.plugins.tfs.actions;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.plugins.tfs.model.Server;

public class CheckoutInfo {
	
    private final String workspaceName;
    private final String projectPath;
    private final String localFolder;
    private final boolean useUpdate;
    private final Server server;
    private final FilePath workspacePath;
    @SuppressWarnings("rawtypes") 
    private final AbstractBuild abstractBuild;
    
    private String checkoutStrategyValue;
    
	public CheckoutInfo(String workspaceName, String projectPath,
			String localFolder, boolean useUpdate, Server server,
			FilePath workspacePath, @SuppressWarnings("rawtypes") AbstractBuild abstractBuild) {
		super();
		this.workspaceName = workspaceName;
		this.projectPath = projectPath;
		this.localFolder = localFolder;
		this.useUpdate = useUpdate;
		this.server = server;
		this.workspacePath = workspacePath;
		this.abstractBuild = abstractBuild;
	}

	public String getWorkspaceName() {
		return workspaceName;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public String getLocalFolder() {
		return localFolder;
	}

	public boolean isUseUpdate() {
		return useUpdate;
	}

	public Server getServer() {
		return server;
	}

	public FilePath getWorkspacePath() {
		return workspacePath;
	}

	public String getCheckoutStrategyValue() {
		return checkoutStrategyValue;
	}

	public void setCheckoutStrategyValue(String checkoutStrategyValue) {
		this.checkoutStrategyValue = checkoutStrategyValue;
	}

	@SuppressWarnings("rawtypes")
	public AbstractBuild getAbstractBuild() {
		return abstractBuild;
	}
    

}
