package hudson.plugins.tfs.action;

import hudson.AbortException;
import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.model.TeamFoundationChangeSet;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.util.ToolArgumentBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Action to retrieve a list of change sets from a Team Foundation repository.
 * 
 * @author Erik Ramfelt
 * @author <a href="http://www.woodwardweb.com">Martin Woodward</a>
 */
public class DefaultHistoryAction {

    private static final String CHANGESET_SEPERATOR = "------------";
    
    /**
     * The magic regex to identify the key data elements within the
     * changeset
     */
    private static final Pattern PATTERN_CHANGESET = Pattern.compile("^[^:]*:[ \t]([0-9]*)\n"
            + "[^:]*:[ \t](.*)\n[^:]*:[ \t](.*)\n"
            + "[^:]*:((?:\n.*)*)\n\n[^\n :]*:(?=\n  )((?:\n[ \t]+.*)*)");

    /**
     * An additional regex to split the items into their parts (change type
     * and filename)
     */
    private static final Pattern PATTERN_ITEM = Pattern.compile("\n  ([^$]+) (\\$/.*)");

    
    public List<TeamFoundationChangeSet> getChangeSets(TfTool tool, TeamFoundationProject project, 
            Calendar fromTimestamp, Calendar toTimestamp) throws IOException, InterruptedException {
        ToolArgumentBuilder builder = new ToolArgumentBuilder(project);
        MaskedArgumentListBuilder arguments = builder.getDetailedHistoryArguments(fromTimestamp, toTimestamp);        
        BufferedReader reader = new BufferedReader(tool.execute(arguments.toCommandArray(), arguments.toMaskArray()));
        try {
            return parseDetailedHistoryOutput(reader, fromTimestamp.getTime());
        } catch (ParseException pe) {
            tool.getListener().fatalError(pe.getMessage());
            throw new AbortException();
        } finally {
            reader.close();
        }
    }

    private List<TeamFoundationChangeSet> parseDetailedHistoryOutput(BufferedReader consoleReader, Date lastBuildDate) throws IOException, ParseException {
        ArrayList<TeamFoundationChangeSet> list = new ArrayList<TeamFoundationChangeSet>();
        
        StringBuilder builder = new StringBuilder();
        String line;
        int linecount = 0;
        boolean foundAtLeastOneChangeSet = false;
        
        while ((line = consoleReader.readLine()) != null) {
            linecount++;
            if (line.startsWith(CHANGESET_SEPERATOR)) {
                foundAtLeastOneChangeSet = true;
                if (linecount > 1) {
                    // We are starting a new changeset.
                    TeamFoundationChangeSet changeSet = parseChangeSetOutput(builder.toString(), lastBuildDate);
                    if (changeSet != null) {
                        list.add(changeSet);
                    }
                    builder.setLength(0);
                }
            } else {
                builder.append(line).append('\n');
            }
        }
        
        if (foundAtLeastOneChangeSet) {
            TeamFoundationChangeSet changeSet = parseChangeSetOutput(builder.toString().trim(), lastBuildDate);
            if (changeSet != null) {
                list.add(changeSet);
            }
        }
        Collections.reverse(list);
        return list;
    }

    /**
     * Returns a change set from the string containing ONE change set
     * @param changeSetString string containing ONE change set output
     * @param lastBuildDate the last build date
     * @return a change set; null if the change set was too old or invalid.
     */
    private TeamFoundationChangeSet parseChangeSetOutput(String changeSetString, Date lastBuildDate) throws ParseException {
        TeamFoundationChangeSet changeset = null;
        
        Matcher m = PATTERN_CHANGESET.matcher(changeSetString);
        if (m.find()) {
            String revision = m.group(1);
            String userName = m.group(2).trim();

            Date modifiedTime = parseDate(m.group(3));
            
            // CC-735.  Ignore changesets that occured before the specified lastBuild.
//            if (modifiedTime.compareTo(lastBuildDate) < 0) {
//                return null;
//            }

            // Remove the indentation from the comment
            String comment = m.group(4).replaceAll("\n  ", "\n");
            if (comment.length() > 0) {
                // remove leading "\n"
                comment = comment.trim();
            }

            // Parse the items.
            Matcher itemMatcher = PATTERN_ITEM.matcher(m.group(5));
            while (itemMatcher.find()) {
                if (changeset == null) {
                    changeset = new TeamFoundationChangeSet(revision, modifiedTime, userName, comment);
                }

                // In a similar way to Subversion, TFS will record additions
                // of folders etc
                // Therefore we have to report all modifictaion by the file
                // and not split
                // into file and folder as there is no easy way to
                // distinguish
                // $/path/filename
                // from
                // $/path/foldername
                //
                String path = itemMatcher.group(2);
                String action = itemMatcher.group(1).trim();
                if (!path.startsWith("$/")) {
                    // If this happens then we have a bug, output some data
                    // to make it easy to figure out what the problem was so
                    // that we can fix it.
                    throw new ParseException("Parse error. Mistakenly identified \"" + path
                            + "\" as an item, but it does not appear to "
                            + "be a valid TFS path.  Please report this as a bug.  Changeset" + "data = \"\n"
                            + changeSetString + "\n\".", itemMatcher.start());
                }
                changeset.getItems().add(new TeamFoundationChangeSet.Item(path, action));
            }
        }
        if (changeset == null) {
            // We should always find at least one item. If we don't
            // then this will be because we have not parsed correctly.
            throw new ParseException("Parse error. Unable to find an item within "
                    + "a changeset.  Please report this as a bug.  Changeset" 
                    + "data = \"\n" + changeSetString + "\n\".",
                    0);
        }
        return changeset;
    }
    
    @SuppressWarnings("deprecation")
    private static Date parseDate(String dateString) throws ParseException {
        Date date = null;
        try {
            // Use the deprecated Date.parse method as this is very good at detecting
            // dates commonly output by the US and UK standard locales of dotnet that
            // are output by the Microsoft command line client.
            date = new Date(Date.parse(dateString));
        } catch (IllegalArgumentException e) {
            // ignore - parse failed.
        }
        if (date == null) {
            // The old fashioned way did not work. Let's try it using a more
            // complex alternative.
            DateFormat[] formats = createDateFormatsForLocaleAndTimeZone(null, null);
            return parseWithFormats(dateString, formats);
        }
        return date;
    }

    private static Date parseWithFormats(String input, DateFormat[] formats) throws ParseException {
        ParseException parseException = null;
        for (int i = 0; i < formats.length; i++) {
            try {
                return formats[i].parse(input);
            } catch (ParseException ex) {
                parseException = ex;
            }
        }

        throw parseException;
    }

    /**
     * Build an array of DateFormats that are commonly used for this locale
     * and timezone.
     */
    private static DateFormat[] createDateFormatsForLocaleAndTimeZone(Locale locale, TimeZone timeZone) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        List<DateFormat> formats = new ArrayList<DateFormat>();

        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            for (int timeStyle = DateFormat.FULL; timeStyle <= DateFormat.SHORT; timeStyle++) {
                DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                if (timeZone != null) {
                    df.setTimeZone(timeZone);
                }
                formats.add(df);
            }
        }

        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            DateFormat df = DateFormat.getDateInstance(dateStyle, locale);
            df.setTimeZone(timeZone);
            formats.add(df);
        }

        return formats.toArray(new DateFormat[formats.size()]);
    }
}
