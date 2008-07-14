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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailedHistoryCommand extends AbstractCommand implements ParseableCommand<List<ChangeSet>> {

    private static final String CHANGESET_SEPERATOR = "------------";
    
    /**
     * The magic regex to identify the key data elements within the
     * changeset
     */
    private static final Pattern PATTERN_CHANGESET = Pattern.compile("^[^:]*:[ \t]([0-9]*)\n"
            + "[^:]*:[ \t](.*)\n[^:]*:[ \t](.*)\n"
            + "[^:]*:((?:\n.*)*)\n\n[^\n :]*:(?=\n  )((?:\n[ \t]+.*)*)");

    /**
     * An additional regex to split the items into their parts (change type
     * and filename)
     */
    private static final Pattern PATTERN_ITEM = Pattern.compile("\n  ([^$]+) (\\$/.*)");

    private final String projectPath;

    private final Calendar fromTimestamp;

    private final Calendar toTimestamp;

    
    public DetailedHistoryCommand(ServerConfigurationProvider provider,  
            String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        super(provider);
        this.projectPath = projectPath;
        this.fromTimestamp = fromTimestamp;
        this.toTimestamp = toTimestamp;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("history");
        arguments.add(projectPath);
        arguments.add("/noprompt");
        arguments.add(String.format("/version:D%s~D%s", 
                Util.XS_DATETIME_FORMATTER.format(fromTimestamp.getTime()), 
                Util.XS_DATETIME_FORMATTER.format(toTimestamp.getTime())));
        arguments.add("/recursive");
        arguments.add("/format:detailed");        
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
    
    public List<ChangeSet> parse(Reader reader) throws IOException, ParseException {
        return parseDetailedHistoryOutput(new BufferedReader(reader), fromTimestamp.getTime());
    }
    
    private List<ChangeSet> parseDetailedHistoryOutput(BufferedReader consoleReader, Date lastBuildDate) throws IOException, ParseException {
        ArrayList<ChangeSet> list = new ArrayList<ChangeSet>();
        
        StringBuilder builder = new StringBuilder();
        String line;
        int linecount = 0;
        boolean foundAtLeastOneChangeSet = false;
        
        while ((line = consoleReader.readLine()) != null) {
            linecount++;
            if (line.startsWith(CHANGESET_SEPERATOR)) {
                foundAtLeastOneChangeSet = true;
                if (linecount > 1) {
                    // We are starting a new changeset.
                    ChangeSet changeSet = parseChangeSetOutput(builder.toString(), lastBuildDate);
                    if (changeSet != null) {
                        list.add(changeSet);
                    }
                    builder.setLength(0);
                }
            } else {
                builder.append(line).append('\n');
            }
        }
        
        if (foundAtLeastOneChangeSet) {
            ChangeSet changeSet = parseChangeSetOutput(builder.toString().trim(), lastBuildDate);
            if (changeSet != null) {
                list.add(changeSet);
            }
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * Returns a change set from the string containing ONE change set
     * @param changeSetString string containing ONE change set output
     * @param lastBuildDate the last build date
     * @return a change set; null if the change set was too old or invalid.
     */
    private ChangeSet parseChangeSetOutput(String changeSetString, Date lastBuildDate) throws ParseException {
        ChangeSet changeset = null;
        Matcher m = PATTERN_CHANGESET.matcher(changeSetString);
        if (m.find()) {
            String revision = m.group(1);
            String userName = m.group(2).trim();

            Date modifiedTime = DateUtil.parseDate(m.group(3));
            
            // CC-735.  Ignore changesets that occured before the specified lastBuild.
            if (modifiedTime.compareTo(lastBuildDate) < 0) {
                return null;
            }

            // Remove the indentation from the comment
            String comment = m.group(4).replaceAll("\n  ", "\n");
            if (comment.length() > 0) {
                // remove leading "\n"
                comment = comment.trim();
            }

            // Parse the items.
            Matcher itemMatcher = PATTERN_ITEM.matcher(m.group(5));
            while (itemMatcher.find()) {
                if (changeset == null) {
                    changeset = new ChangeSet(revision, modifiedTime, userName, comment);
                }

                // In a similar way to Subversion, TFS will record additions
                // of folders etc
                // Therefore we have to report all modifictaion by the file
                // and not split
                // into file and folder as there is no easy way to
                // distinguish
                // $/path/filename
                // from
                // $/path/foldername
                //
                String path = itemMatcher.group(2);
                String action = itemMatcher.group(1).trim();
                if (!path.startsWith("$/")) {
                    // If this happens then we have a bug, output some data
                    // to make it easy to figure out what the problem was so
                    // that we can fix it.
                    throw new ParseException("Parse error. Mistakenly identified \"" + path
                            + "\" as an item, but it does not appear to "
                            + "be a valid TFS path.  Please report this as a bug.  Changeset" + "data = \"\n"
                            + changeSetString + "\n\".", itemMatcher.start());
                }
                changeset.getItems().add(new ChangeSet.Item(path, action));
            }
        }
        if (changeset == null) {
            // We should always find at least one item. If we don't
            // then this will be because we have not parsed correctly.
            throw new ParseException("Parse error. Unable to find an item within "
                    + "a changeset.  Please report this as a bug.  Changeset" 
                    + "data = \"\n" + changeSetString + "\n\".",
                    0);
        }
        return changeset;
    }
}
