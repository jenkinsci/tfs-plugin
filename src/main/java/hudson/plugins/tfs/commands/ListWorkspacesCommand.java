package hudson.plugins.tfs.commands;

import hudson.Util;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.util.TextTableParser;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ListWorkspacesCommand extends AbstractCommand implements ParseableCommand<List<Workspace>> {
    private final WorkspaceFactory factory;
    private final String computer;

    public interface WorkspaceFactory {
        Workspace createWorkspace(String name, String computer, String owner, String comment);
    }
    
    public ListWorkspacesCommand(WorkspaceFactory factory, ServerConfigurationProvider provider) {
        this(factory, provider, null);
    }

    public ListWorkspacesCommand(WorkspaceFactory factory, ServerConfigurationProvider config, String computer) {
        super(config);
        this.computer = computer;
        this.factory = factory;
    }

    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspaces");
        arguments.add("-format:brief");
        if (Util.fixEmpty(computer) != null) {
            arguments.add(String.format("-computer:%s", computer));
        }
        addServerArgument(arguments);
        addLoginArgument(arguments);
        return arguments;
    }
    
    public List<Workspace> parse(Reader consoleReader) throws IOException {
        List<Workspace> list = new ArrayList<Workspace>();
        
        TextTableParser parser = new TextTableParser(consoleReader, 1);
        while (parser.nextRow()) {
            Workspace workspace = factory.createWorkspace(
                parser.getColumn(0), 
                parser.getColumn(2),
                parser.getColumn(1),
                Util.fixNull(parser.getColumn(3)));
            list.add(workspace);            
        }
        return list;
    }

    static void log(final List<Workspace> workspaces, final PrintStream logger) {
        int maxName = "Workspace".length();
        int maxOwner = "Owner".length();
        int maxComputer = "Computer".length();
        int maxComment = "Comment".length();
        final String template =
                "%1$-" + maxName + "s %2$-" + maxOwner + "s %3$-" + maxComputer + "s %4$-" + maxComment + "s";
        final String header = String.format(template, "Workspace", "Owner", "Computer", "Comment");
        logger.println(header);
        final String divider = String.format(template,
                StringUtils.repeat("-", maxName),
                StringUtils.repeat("-", maxOwner),
                StringUtils.repeat("-", maxComputer),
                StringUtils.repeat("-", maxComment));
        logger.println(divider);
    }
}
