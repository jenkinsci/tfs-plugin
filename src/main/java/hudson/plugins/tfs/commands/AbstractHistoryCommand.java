package hudson.plugins.tfs.commands;

import java.util.Calendar;
import java.util.List;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

public abstract class AbstractHistoryCommand extends AbstractCommand implements
        ParseableCommand<List<ChangeSet>> {

    protected final String projectPath;
    protected final Calendar fromTimestamp;
    protected final int fromChangeset;
    protected final Calendar toTimestamp;

    protected AbstractHistoryCommand(ServerConfigurationProvider configurationProvider, String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        super(configurationProvider);
        this.projectPath = projectPath;
        this.fromTimestamp = fromTimestamp;
        this.fromChangeset = 0;

        this.toTimestamp = getExclusiveToTimestamp(toTimestamp);
    }

    protected AbstractHistoryCommand(ServerConfigurationProvider configurationProvider, String projectPath, int fromChangeset, Calendar toTimestamp) {
        super(configurationProvider);
        this.projectPath = projectPath;
        this.fromTimestamp = null;
        this.fromChangeset = fromChangeset;

        this.toTimestamp = getExclusiveToTimestamp(toTimestamp);
    }
    
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("history");
        arguments.add(projectPath);
        arguments.add("-noprompt");
        arguments.add(String.format("-version:%s~%s", 
                getRangeSpecification(fromTimestamp, fromChangeset), 
                getRangeSpecification(toTimestamp, 0)));
        arguments.add("-recursive");
        arguments.add(String.format("-format:%s", getFormat()));        
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }

    protected abstract String getFormat();
   
}
