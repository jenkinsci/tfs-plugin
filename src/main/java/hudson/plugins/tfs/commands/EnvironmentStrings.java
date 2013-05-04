package hudson.plugins.tfs.commands;

public enum EnvironmentStrings {
	
    WORKSPACE("TFS_WORKSPACE"),
    WORKFOLDER("TFS_WORKFOLDER"),
    PROJECTPATH("TFS_PROJECTPATH"),
    SERVERURL("TFS_SERVERURL"),
    USERNAME("TFS_USERNAME"),
    CHECKOUT_INFO("CHECKOUT_INFO"),
    CHANGESET("TFS_CHANGESET");
    
    private String value;
	
	private EnvironmentStrings(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}

}
