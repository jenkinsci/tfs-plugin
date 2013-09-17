package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.util.Calendar;

public abstract class AbstractHistoryCommand extends AbstractCommand {

    protected final String projectPath;
    protected Calendar fromTimestamp;
    protected int fromChangeset;
    protected Calendar toTimestamp;
	
	public AbstractHistoryCommand(ServerConfigurationProvider provider,
			String projectPath2) {
		super(provider);
		this.projectPath = projectPath2;
	}

    public AbstractHistoryCommand(ServerConfigurationProvider configurationProvider, String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        super(configurationProvider);
        this.projectPath = projectPath;
        this.fromTimestamp = fromTimestamp;
        this.fromChangeset = 0;

        this.toTimestamp = getExclusiveToTimestamp(toTimestamp);
    }

    public AbstractHistoryCommand(ServerConfigurationProvider configurationProvider, String projectPath, int fromChangeset, Calendar toTimestamp) {
        super(configurationProvider);
        this.projectPath = projectPath;
        this.fromTimestamp = null;
        this.fromChangeset = fromChangeset;

        this.toTimestamp = getExclusiveToTimestamp(toTimestamp);
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
        arguments.add(String.format("-format:%s", getFormat()));
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
	}
	
	/**
	 * Append a versionspec parameter followed by its value<br/>
	 * Ex: "C1256" to a changeset or "Lmylabel" to a label.
	 * 
	 */
    protected String addVersionspecCommand() {
        return new StringBuilder()
                .append(getRangeSpecification(fromTimestamp, fromChangeset))
                .append("~")
                .append(getRangeSpecification(toTimestamp, 0))
                .toString();
    }

    protected abstract String getFormat();

}
