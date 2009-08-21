package hudson.plugins.tfs.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hudson.plugins.tfs.model.WorkspaceMapping;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

/**
 * Retrieves the workspace mappings.
 * 
 * @author redsolo
 */
public class GetWorkspaceMappingsCommand extends AbstractCommand implements ParseableCommand<List<WorkspaceMapping>> {

    private static final Pattern PATTERN_WORKFOLDERMAPPING = Pattern.compile("\\s*(.*):\\s(.*)");
    
    private final String workspace;

    public GetWorkspaceMappingsCommand(ServerConfigurationProvider config, String workspace) {
        super(config);
        this.workspace = workspace;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");        
        arguments.add("-workspace:" + workspace);
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }

    public List<WorkspaceMapping> parse(Reader consoleReader) throws ParseException, IOException {
        List<WorkspaceMapping> mappings = new ArrayList<WorkspaceMapping>();
        BufferedReader reader = new BufferedReader(consoleReader);
        reader.readLine(); // ====
        reader.readLine(); // workspace
        reader.readLine(); // server
        String line = reader.readLine();
        while (line != null) {
            Matcher matcher = PATTERN_WORKFOLDERMAPPING.matcher(line);
            if (matcher.find()) {
                mappings.add(new WorkspaceMapping(matcher.group(1), matcher.group(2)));
            }
            line = reader.readLine();
        }
        return mappings;
    }
}
