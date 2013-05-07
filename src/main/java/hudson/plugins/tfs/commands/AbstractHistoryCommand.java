package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public abstract class AbstractHistoryCommand extends AbstractCommand {
	
	String projectPath;
	
	public AbstractHistoryCommand(
			ServerConfigurationProvider configurationProvider) {
		super(configurationProvider);
	}

	public AbstractHistoryCommand(ServerConfigurationProvider provider,
			String projectPath2) {
		this(provider);
		this.projectPath = projectPath2;
	}

	/**
	 * Builds a history command
	 */
	public MaskedArgumentListBuilder getArguments() {
		MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("history");
        arguments.add(projectPath);
        arguments.add("-noprompt");
        arguments.add("-version:" + addVersionspecCommand());
        arguments.add("-recursive");
        arguments.add("-format:detailed");        
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
	}
	
	/**
	 * Append a versionspec parameter followed by its value<br/>
	 * Ex: "C1256" to a changeset or "Lmylabel" to a label. 
	 * @param arguments 
	 * 
	 * @return
	 */
	public abstract String addVersionspecCommand();

}
