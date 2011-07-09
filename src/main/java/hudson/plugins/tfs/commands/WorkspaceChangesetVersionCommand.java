package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.util.TextTableParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

/**
 * TF command for retrieving workspace latest change set version.
 *
 * @author Mario Zagar
 */
public class WorkspaceChangesetVersionCommand extends AbstractCommand implements ParseableCommand<String> {
   
    private final String localPath;
    
    /**
     * 
     * @param localPath the local path to get the workspace changeset version for
     */
    public WorkspaceChangesetVersionCommand(ServerConfigurationProvider provider, String localPath) {
        super(provider);

        this.localPath = localPath;
    }

    /**
     * Returns arguments for TFS history command:
     * 
     *    <i>tf history localPath -recursive -noprompt -stopafter:1 -version:T -format:brief</i></p>
     * 
     */
	public MaskedArgumentListBuilder getArguments() {		
		MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
		arguments.add("history");
		arguments.add(localPath);
		arguments.add("-recursive");
		arguments.add("-stopafter:1");
		arguments.add("-noprompt");
		arguments.add("-version:T");
		arguments.add("-format:brief");
		return arguments;
		
	}

	public String parse(Reader consoleReader) throws ParseException, IOException {
		TextTableParser parser = new TextTableParser(new BufferedReader(consoleReader), 1);

		while (parser.nextRow()) {
			return parser.getColumn(0);
		}
		
		return StringUtils.EMPTY;
	}

}
