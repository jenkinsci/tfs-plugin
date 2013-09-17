package hudson.plugins.tfs.commands;

import hudson.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.DateUtil;
import hudson.plugins.tfs.util.TextTableParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * TF command for retrieving a brief history.
 *
 * @author Erik Ramfelt
 */
public class BriefHistoryCommand extends AbstractHistoryCommand {

    /**
     *
     * @param projectPath the project path to get the history for
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     */
    public BriefHistoryCommand(ServerConfigurationProvider provider,
                               String projectPath, Calendar fromTimestamp, Calendar toTimestamp) {
        super(provider, projectPath, fromTimestamp, toTimestamp);
    }

    public BriefHistoryCommand(ServerConfigurationProvider provider,
                               String projectPath, int fromChangeset, Calendar toTimestamp) {
        super(provider, projectPath, fromChangeset, toTimestamp);
    }

    @Override
    protected String getFormat() {
        return "brief";
    }

    /**
     * Parse the data in the reader and return a list of change sets.
     * @param consoleReader console output
     * @return a list of change sets from the console output; empty if none could be found.
     */
    public List<ChangeSet> parse(Reader consoleReader) throws ParseException, IOException {
        Date lastBuildDate = fromTimestamp == null ? null : fromTimestamp.getTime();
        List<ChangeSet> list = new ArrayList<ChangeSet>();

        TextTableParser parser = new TextTableParser(new BufferedReader(consoleReader), 1);
        while (parser.nextRow()) {
            ChangeSet changeset = new ChangeSet(parser.getColumn(0),
                    DateUtil.parseDate(parser.getColumn(2)),
                    parser.getColumn(1),
                    Util.fixNull(parser.getColumn(3)));
            if (lastBuildDate == null || changeset.getDate().after(lastBuildDate)) {
                list.add(changeset);
            }
        }
        return list;
    }
}
