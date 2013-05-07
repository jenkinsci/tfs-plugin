package hudson.plugins.tfs;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateParser;
import hudson.plugins.tfs.util.KeyValueTextReader;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeSetUtils {
	
	// Setting this system property will skip the date chek in parsing that makes
    // sure that a change set is within the date range. See CC-735 reference.
    public static final String IGNORE_DATE_CHECK_ON_CHANGE_SET = "tfs.history.skipdatecheck";
    
    
    /** Names of field in the output from the TFS tool. This was added because the tool 
     * will return locale dependent field names.
     */
    private static final int FIELD_USER = 0;
    private static final int FIELD_CHANGESET = 1;
    private static final int FIELD_DATE = 2;
    private static final int FIELD_ITEMS = 3;
    private static final int FIELD_COMMENT = 4;
    private static final int FIELD_CHECKED_IN_BY = 5;
    //private static final int FIELD_CHECKEIN_NOTES = 6;
    
    private static final String[][] LANG_FIELD_NAMES = {
                {"User", "Changeset", "Date", "Items", "Comment", "Checked in by", "Check-in Notes"}, // EN
                {"Benutzer", "Changeset", "Datum", "Elemente", "Kommentar", "Checked in by", "Eincheckhinweise"} // DE
            };
    
    /**
     * The magic regex to identify the key data elements within the
     * changeset
     */
    private static final Pattern PATTERN_CHANGESET = Pattern.compile("^[^:]*:[ \t]([0-9]*)\n"
            + "[^:]*:[ \t](.*)\n[^:]*:[ \t](.*)\n"
            + "[^:]*:(?s)(.*)\n\n[^\n :]*:(?=\n  )(.*)\n\n");

    /**
     * An additional regex to split the items into their parts (change type
     * and filename)
     */
    private static final Pattern PATTERN_ITEM = Pattern.compile("\\s*([^$]+) (\\$/.*)");

	private final DateParser dateParser;
    

	public ChangeSetUtils(DateParser dateParser) {
		this.dateParser = dateParser;
	}

	
    /**
     * Returns a change set from the string containing one change set.
     * This will do some intelligent parsing as it will read all key and value from the log.
     * This will only work if we know the exact words in the key column, and as of now we only
     * know of english. If it can not find the keys it will return null.
     * @param changeSetString string containing one change set
     * @return a change set if it could read the different key/value pairs; null otherwise
     */
    public ChangeSet parseChangeSetString(String changeSetString) throws ParseException, IOException {
        KeyValueTextReader reader = new KeyValueTextReader();
        Map<String, String> map = reader.parse(changeSetString);
        for (String[] fieldNames : LANG_FIELD_NAMES) {
            if (map.containsKey(fieldNames[FIELD_USER]) 
                    && map.containsKey(fieldNames[FIELD_CHANGESET]) 
                    && map.containsKey(fieldNames[FIELD_DATE])
                    && map.containsKey(fieldNames[FIELD_ITEMS])) {
                ChangeSet changeSet = createChangeSet(map.get(fieldNames[FIELD_ITEMS]), 
                		map.get(fieldNames[FIELD_CHANGESET]), 
                		map.get(fieldNames[FIELD_USER]), 
                		map.get(fieldNames[FIELD_DATE]), 
                		map.get(fieldNames[FIELD_COMMENT]));
                if (changeSet != null) {
                    changeSet.setCheckedInBy(map.get(fieldNames[FIELD_CHECKED_IN_BY]));
                }
                return changeSet;
            }
        }
        return null;
    }

    /**
     * Returns a change set from the string containing ONE change set using a regex
     * @param changeSetString string containing ONE change set output
     * @return a change set; null if the change set was too old or invalid.
     */
    public ChangeSet parseChangeSetStringWithRegex(String changeSetString) throws ParseException {
        Matcher m = PATTERN_CHANGESET.matcher(changeSetString);
        if (m.find()) {
            String revision = m.group(1);
            String userName = m.group(2).trim();
            
            // Remove the indentation from the comment
            String comment = m.group(4).replaceAll("\n  ", "\n");
            if (comment.length() > 0) {
                // remove leading "\n"
                comment = comment.trim();
            }

            return createChangeSet(m.group(5), revision, userName, m.group(3), comment);
        }
        return null;
    }

	private ChangeSet createChangeSet(String items, String revision, String userName, String modifiedTime, String comment) throws ParseException {
		// Parse the items.
		Matcher itemMatcher = PATTERN_ITEM.matcher(items);
		ChangeSet changeset = null;
		while (itemMatcher.find()) {
		    if (changeset == null) {
		        changeset = new ChangeSet(revision, dateParser.parseDate(modifiedTime), userName, comment);
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
		                + items + "\n\".", itemMatcher.start());
		    }
		    changeset.getItems().add(new ChangeSet.Item(path, action));
		}
		return changeset;
	}

}
