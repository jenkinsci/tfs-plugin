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
public class BriefHistoryCommand implements ParseableCommand<List<ChangeSet>> {
    private static final Pattern PATTERN_BRIEF_CHANGESET = Pattern.compile("^(\\d*)\\s+(\\S+)\\s+(\\S+\\s+\\S+)\\s+(.*)");
    
    private final String projectPath;
    private final Calendar toTimestamp;
    private final Calendar fromTimestamp;
    
    /**
     * 
     * @param projectPath the project path to get the history for
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     */
    public BriefHistoryCommand(String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
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
        return arguments;
    }
   
    /**
     * Parse the data in the reader and return a list of change sets.
     * @param consoleReader console output
     * @return a list of change sets from the console output; empty if none could be found.
     */
    public List<ChangeSet> parse(Reader consoleReader) throws ParseException, IOException {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        
        BufferedReader reader = new BufferedReader(consoleReader);        
        String line = reader.readLine();
        while (line != null) {            
            Matcher matcher = PATTERN_BRIEF_CHANGESET.matcher(line);
            if (matcher.find()) {
                ChangeSet changeset = new ChangeSet(
                        matcher.group(1), 
                        DateUtil.parseDate(matcher.group(3)),
                        matcher.group(2),
                        matcher.group(4));
                if (changeset.getDate().after(fromTimestamp.getTime())) { 
                    list.add(changeset);
                }
            }
            line = reader.readLine();
        }
        return list;
    }
}
