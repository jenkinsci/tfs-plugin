package hudson.plugins.tfs.commands;

import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.util.TextTableParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

import org.apache.commons.lang.StringUtils;

public abstract class AbstractChangesetVersionCommand extends AbstractCommand implements ParseableCommand<String> {

    private final String path;

    public AbstractChangesetVersionCommand(
            ServerConfigurationProvider configurationProvider, String path) {
        super(configurationProvider);
        this.path = path;
    }

    abstract String getVersionSpecification();
    
    /**
     * Returns arguments for TFS history command:
     * 
     *    <i>tf history {path} -recursive -noprompt -stopafter:1 -version:{versionSpecification} -format:brief</i></p>
     *    
     */
    public MaskedArgumentListBuilder getArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();
        arguments.add("history");
        arguments.add(path);
        arguments.add("-recursive");
        arguments.add("-stopafter:1");
        arguments.add("-noprompt");
        arguments.add(String.format("-version:%s", getVersionSpecification()));
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