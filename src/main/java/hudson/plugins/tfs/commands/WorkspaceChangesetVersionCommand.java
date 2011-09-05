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
    private final String workspaceName;
    private String workspaceOwner;
    
    /**
     * 
     * @param localPath the local path to get the workspace changeset version for
     * @param workspaceName name of workspace for which to get changeset version
     * @param workspaceOwner owner of TFS workspace
     */
    public WorkspaceChangesetVersionCommand(ServerConfigurationProvider provider, String localPath, String workspaceName, String workspaceOwner) {
        super(provider);

        this.localPath = localPath;
        this.workspaceName = workspaceName;
        this.workspaceOwner = workspaceOwner;
    }

    /**
     * Returns arguments for TFS history command:
     * 
     *    <i>tf history localPath -recursive -noprompt -stopafter:1 -version:WworkspaceName;workspaceOwner -format:brief</i></p>
     *    
     */
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();
        arguments.add("history");
        arguments.add(localPath);
        arguments.add("-recursive");
        arguments.add("-stopafter:1");
        arguments.add("-noprompt");
        arguments.add("-version:W" + workspaceName + ";" + workspaceOwner);
        arguments.add("-format:brief");
        addLoginArgument(arguments);
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
