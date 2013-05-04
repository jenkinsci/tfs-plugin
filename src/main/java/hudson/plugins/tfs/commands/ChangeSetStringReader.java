package hudson.plugins.tfs.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Class for extracing one change set segment out of a long list of change sets.
 */
public class ChangeSetStringReader {
	
	private static final Pattern PATTERN_KEYWORD = Pattern.compile("\\w+:");
    private final BufferedReader reader;
	private boolean foundAtLeastOneChangeSet;
	private static final String CHANGESET_SEPERATOR = "------------";

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