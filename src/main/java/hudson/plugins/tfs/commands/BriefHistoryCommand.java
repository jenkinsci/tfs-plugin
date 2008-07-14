package hudson.plugins.tfs.commands;

import hudson.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TF command for retrieving a brief history.
 * 
 * @author Erik Ramfelt
 */
public class BriefHistoryCommand extends AbstractCommand implements ParseableCommand<List<ChangeSet>> {
    static final Pattern CHANGESET_PATTERN = Pattern.compile("^\\d*\\s+\\S+\\s+.*");
    static final Pattern SEPARATOR_PATTERN = Pattern.compile("(-+)(\\s+)(-+)(\\s+)(-+)(\\s+)(-+)");
    
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
        arguments.add("/noprompt");
        arguments.add(String.format("/version:D%s~D%s", 
                Util.XS_DATETIME_FORMATTER.format(fromTimestamp.getTime()), 
                Util.XS_DATETIME_FORMATTER.format(toTimestamp.getTime())));
        arguments.add("/recursive");
        arguments.add("/format:brief");
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

        int[] colStart = new int[4];
        int[] colLength = new int[4];
        
        BufferedReader reader = new BufferedReader(consoleReader);        
        String line = reader.readLine();
        
        // Need to find out how long the columns are, as the date column is variable
        // and it does not have a common separator so it can be extracted using a 
        // regex. This is not pretty, and if anyone have any idea to improve this
        // let me know!!!
        while (line != null) {
            Matcher matcher = SEPARATOR_PATTERN.matcher(line);
            if (matcher.matches()) {
                for (int i = 0; i < (colLength.length-1); i++) {
                    colLength[i] = colStart[i] + matcher.group(i*2 + 1).length();
                    colStart[i+1] = colLength[i] + matcher.group(i*2+2).length();
                }
                break;
            }
            line = reader.readLine();
        }
        
        line = reader.readLine();
        while (line != null) {
            if (CHANGESET_PATTERN.matcher(line).matches()) {
                ChangeSet changeset = new ChangeSet(
                    line.substring(colStart[0], colLength[0]).trim(),
                    DateUtil.parseDate(line.substring(colStart[2], colLength[2]).trim()),
                    line.substring(colStart[1], colLength[1]).trim(),
                    line.substring(colStart[3]));
                if (changeset.getDate().after(fromTimestamp.getTime())) { 
                    list.add(changeset);
                }
            }
            line = reader.readLine();
        }
        return list;
    }
}
