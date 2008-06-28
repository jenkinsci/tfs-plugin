package hudson.plugins.tfs.util;

import hudson.plugins.tfs.model.TeamFoundationCredentials;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.util.ArgumentListBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ToolArgumentBuilder {

    /** UTC Date format - best one to pass dates across the wire. */
    private static final String TFS_UTC_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    
    private final TeamFoundationProject project;

    public ToolArgumentBuilder(TeamFoundationProject project) {
        this.project = project;
    }

    /**
     * Build up the arguments necessary for a brief history command.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @return an ArgumentListBuilder containing the arguments for the TF tool.
     */
    public ArgumentListBuilder getBriefHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp) {
        return getHistoryArguments(fromTimestamp, toTimestamp, "brief");
    }

    /**
     * Build up the arguments necessary for a detailed history command.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @return an ArgumentListBuilder containing the arguments for the TF tool.
     */
    public ArgumentListBuilder getDetailedHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp) {
        return getHistoryArguments(fromTimestamp, toTimestamp, "detailed");
    }

    /**
     * Build up the arguments necessary for a history command.
     * @param project the project to poll for changes
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @param formatType type of format to get
     * @return an ArgumentListBuilder containing the arguments for the TF tool.
     */
    public ArgumentListBuilder getHistoryArguments(TeamFoundationProject project, Calendar fromTimestamp, Calendar toTimestamp, String formatType) {
        return getHistoryArguments(fromTimestamp, toTimestamp, formatType);
    }

    /**
     * Build up the arguments necessary for a history command.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @param formatType type of format to get
     * @return an ArgumentListBuilder containing the arguments for the TF tool.
     */
    public ArgumentListBuilder getHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp, String formatType) {
        ArgumentListBuilder arguments = new ArgumentListBuilder();
        
        arguments.add("history");
        arguments.add("/noprompt");
        arguments.add("/server:" + project.getServer());
        arguments.add(project.getProject());
        arguments.add("/version:D" + formatUTCDate(fromTimestamp) + "~D" + formatUTCDate(toTimestamp));
        arguments.add("/recursive");
        arguments.add("/format:" + formatType);
        
        TeamFoundationCredentials credentials = project.getCredentials();
        if (credentials != null) {
            arguments.add("/login:" + credentials.getLoginStr());            
        }
        
        return arguments;
    }

    /**
     * Convert the passed date into the UTC Date format best used when talking
     * to Team Foundation Server command line.
     * @param timestamp calendar to format
     * @return a formatted UTC date string
     */
    private String formatUTCDate(Calendar timestamp) {
        DateFormat f = new SimpleDateFormat(TFS_UTC_DATE_FORMAT);
        f.setTimeZone(TimeZone.getTimeZone("GMT"));
        return f.format(timestamp.getTime());
    }
}
