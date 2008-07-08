package hudson.plugins.tfs.commands;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;

/**
 * Command that issues a tf command line client command.
 * 
 * @author Erik Ramfelt
 *
 * @param <T> the return type when parsing the output from the command line client.
 */
public interface ParseableCommand<T> extends Command {
    
    /**
     * Returns data from parsing the command line client output in reader
     * @param reader reader containing the output from the command line client
     * @return parsed data
     * @throws ParseException thrown if there was a problem parsing the data
     * @throws IOException thrown if there was a problem reading the data from the reader
     */
    T parse(Reader reader) throws ParseException, IOException;
}
