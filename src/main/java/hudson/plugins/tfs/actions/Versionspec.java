package hudson.plugins.tfs.actions;

public enum Versionspec {

	D("D%s~D%s"), //datetime 
	L("L%s"); // label
	//CHANGESET("C"), LATEST("T"), WORKSPACE("W")
	
	private String command;

	private Versionspec(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return this.command;
	}
	
}
