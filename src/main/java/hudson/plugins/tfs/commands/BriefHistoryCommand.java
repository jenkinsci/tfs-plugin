package hudson.plugins.tfs.commands;

import hudson.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.TextTableParser;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * TF command for retrieving a brief history.
 * 
 * @author Erik Ramfelt
 */
public class BriefHistoryCommand extends AbstractCommand implements ParseableCommand<List<ChangeSet>> {
   
    private final String projectPath;
    private final Calendar toTimestamp;
    private final Calendar fromTimestamp;
    
    /**
     * 
     * @param projectPath the project path to get the history for
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     */
    public BriefHistoryCommand(ServerConfigurationProvider provider,
            String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        super(provider);
        this.projectPath = projectPath;
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
    }

    /**
     * Returns the arguments for the command
     * @return arguments for the command.
     */
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("history");
        arguments.add(projectPath);
        arguments.add("-noprompt");
        arguments.add(String.format("-version:D%s~D%s", 
                DateUtil.TFS_DATETIME_FORMATTER.get().format(fromTimestamp.getTime()), 
                DateUtil.TFS_DATETIME_FORMATTER.get().format(toTimestamp.getTime())));
        arguments.add("-recursive");
        arguments.add("-format:brief");
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
   
    /**
     * Parse the data in the reader and return a list of change sets.
     * @param consoleReader console output
     * @return a list of change sets from the console output; empty if none could be found.
     */
    public List<ChangeSet> parse(Reader consoleReader) throws ParseException, IOException {
        List<ChangeSet> list = new ArrayList<ChangeSet>();

        TextTableParser parser = new TextTableParser(new BufferedReader(consoleReader), 1);
        while (parser.nextRow()) {
            ChangeSet changeset = new ChangeSet(parser.getColumn(0),
                DateUtil.parseDate(parser.getColumn(2)),
                parser.getColumn(1),
                Util.fixNull(parser.getColumn(3)));
            if (changeset.getDate().after(fromTimestamp.getTime())) { 
                list.add(changeset);
            }
        }
        return list;
    }
}
