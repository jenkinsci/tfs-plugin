package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.ChangeSetUtils;
import hudson.plugins.tfs.actions.Versionspec;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateParser;
import hudson.plugins.tfs.util.DateUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class DetailedHistoryCommand extends AbstractHistoryCommand implements ParseableCommand<List<ChangeSet>> {

    // Setting this system property will skip the date chek in parsing that makes
    // sure that a change set is within the date range. See CC-735 reference.
    public static final String IGNORE_DATE_CHECK_ON_CHANGE_SET = "tfs.history.skipdatecheck";
    
    private static final String CHANGESET_SEPERATOR = "------------";
    
    private final Calendar fromTimestamp;

    private final Calendar toTimestamp;

    private final boolean skipDateCheckInParsing;
    
    private final ChangeSetUtils changeSetUtils;
    
    public DetailedHistoryCommand(ServerConfigurationProvider configurationProvider, String projectPath, Calendar fromTimestamp, Calendar toTimestamp,
            DateParser dateParser) {
        super(configurationProvider);
        this.projectPath = projectPath;
        this.fromTimestamp = fromTimestamp;
        this.skipDateCheckInParsing = Boolean.valueOf(System.getProperty(ChangeSetUtils.IGNORE_DATE_CHECK_ON_CHANGE_SET));
        this.changeSetUtils = new ChangeSetUtils(dateParser);

        // The to timestamp is exclusive, ie it will only show history before the to timestamp.
        // This command should be inclusive.
        this.toTimestamp = (Calendar) toTimestamp.clone();
        this.toTimestamp.add(Calendar.SECOND, 1);
    }

    public DetailedHistoryCommand(ServerConfigurationProvider provider,  
            String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        this(provider, projectPath, fromTimestamp, toTimestamp, new DateParser());
    }

    @Override
    public String addVersionspecCommand() {
    	return String.format(Versionspec.D.getCommand(), 
                DateUtil.TFS_DATETIME_FORMATTER.get().format(fromTimestamp.getTime()), 
                DateUtil.TFS_DATETIME_FORMATTER.get().format(toTimestamp.getTime()));
    }
    
    public List<ChangeSet> parse(Reader reader) throws IOException, ParseException {
        Date lastBuildDate = fromTimestamp.getTime();
        ArrayList<ChangeSet> list = new ArrayList<ChangeSet>();
        
        ChangeSetStringReader iterator = new ChangeSetStringReader(new BufferedReader(reader));
        String changeSetString = iterator.readChangeSet(); 
        while (changeSetString != null) {
        	
        	ChangeSet changeSet = this.changeSetUtils.parseChangeSetString(changeSetString);
        	// If some tf tool outputs the key words in non english we will use the old fashion way
        	// using the complicated regex
        	if (changeSet == null) {
        		changeSet = this.changeSetUtils.parseChangeSetStringWithRegex(changeSetString);
        	}

            if (changeSet == null) {
                // We should always find at least one item. If we don't
                // then this will be because we have not parsed correctly.
                throw new ParseException("Parse error. Unable to find an item within "
                        + "a changeset.  Please report this as a bug.  Changeset" 
                        + "data = \"\n" + changeSetString + "\n\".",
                        0);
            }

            // CC-735.  Ignore changesets that occured before the specified lastBuild.
            if (skipDateCheckInParsing || changeSet.getDate().compareTo(lastBuildDate) > 0) {
                list.add(changeSet);
            }
            changeSetString = iterator.readChangeSet();
        }

        Collections.reverse(list);
        return list;
    }
	
	/**
	 * Class for extracing one change set segment out of a long list of change sets.
	 */
	private static class ChangeSetStringReader {
		
		private static final Pattern PATTERN_KEYWORD = Pattern.compile("\\w+:");
        private final BufferedReader reader;
		private boolean foundAtLeastOneChangeSet;

		public ChangeSetStringReader(BufferedReader reader) {
			super();
			this.reader = reader;
		}
		
        public String readChangeSet() throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;
            int linecount = 0;

            while ((line = reader.readLine()) != null) {
                if (line.length() > 0) {
                    if ((!foundAtLeastOneChangeSet) && PATTERN_KEYWORD.matcher(line).matches()) {
                        foundAtLeastOneChangeSet = true;
                    }
                    if (line.startsWith(CHANGESET_SEPERATOR) && (linecount > 0)) {
                        // We are starting a new changeset.
                        return builder.toString();
                    }
                    linecount++;
                    builder.append(line).append('\n');
                }
            }
            if (foundAtLeastOneChangeSet && linecount > 0) {
                return builder.toString();
            }
            return null;
        }
    }
}
