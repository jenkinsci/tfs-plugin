package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.ChangeSetUtils;
import hudson.plugins.tfs.actions.Versionspec;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @since 19/08/2011
 * @author Vinicius Kopcheski
 */
public class LabelBasedHistoryCommand extends AbstractHistoryCommand implements ParseableCommand<List<ChangeSet>> {

    private final ChangeSetUtils changeSetUtils;
    
    private final String label; 
    
    public LabelBasedHistoryCommand(ServerConfigurationProvider provider, String projectPath,
			String label) {
    	super(provider, projectPath);
        this.changeSetUtils = new ChangeSetUtils(new DateParser());
        this.label = label;
    }
    
    @Override
    public String addVersionspecCommand() {
    	return String.format(Versionspec.L.getCommand(),this.label);
    }

	public List<ChangeSet> parse(Reader reader) throws ParseException,
			IOException {
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

            changeSetString = iterator.readChangeSet();
        }

        Collections.reverse(list);
        return list;
	}
	
	
	
}
