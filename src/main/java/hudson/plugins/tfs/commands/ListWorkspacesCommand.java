package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListWorkspacesCommand extends AbstractCommand implements ParseableCommand<List<Workspace>> {
    static final Pattern BRIEF_WORKSPACE_LIST_PATTERN = 
        Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)\\s*(.*)");
    private final WorkspaceFactory factory;

    public interface WorkspaceFactory {
        Workspace createWorkspace(String name, String computer, String owner, String comment);
    }
    
    public ListWorkspacesCommand(WorkspaceFactory factory, ServerConfigurationProvider provider) {
        super(provider);
        this.factory = factory;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspaces");
        arguments.add("/format:brief");
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
    
    public List<Workspace> parse(Reader consoleReader) throws IOException {
        List<Workspace> list = new ArrayList<Workspace>();
        
        BufferedReader reader = new BufferedReader(consoleReader);
        
        String line = reader.readLine();        
        while ((line != null) && (!line.startsWith("---------"))) {
            line = reader.readLine();
        }
        
        line = reader.readLine();
        while (line != null) {
            Matcher matcher = BRIEF_WORKSPACE_LIST_PATTERN.matcher(line);
            if (matcher.find()) {
                Workspace workspace = factory.createWorkspace(
                        matcher.group(1), 
                        matcher.group(3),
                        matcher.group(2),
                        matcher.group(4));
                list.add(workspace);
            }
            line = reader.readLine();
        }
        return list;
    }
}
