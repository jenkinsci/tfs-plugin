package hudson.plugins.tfs.model;

import hudson.plugins.tfs.commands.BriefHistoryCommand;
import hudson.plugins.tfs.commands.DetailedHistoryCommand;
import hudson.plugins.tfs.commands.UpdateWorkfolderCommand;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Project {

    private final String projectPath;
    private final Server server;

    public Project(Server server, String projectPath) {
        this.server = server;
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Returns a list of change sets containing modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getDetailedHistory(Calendar fromTimestamp, Calendar toTimestamp) throws IOException, InterruptedException, ParseException {
        DetailedHistoryCommand command = new DetailedHistoryCommand(server, projectPath, fromTimestamp, toTimestamp);
        Reader reader = null;
        try {
            reader = server.execute(command.getArguments());
            return command.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Returns a list of change sets not containing the modified items.
     * @param fromTimestamp the timestamp to get history from
     * @param toTimestamp the timestamp to get history to
     * @return a list of change sets
     */
    public List<ChangeSet> getBriefHistory(Calendar fromTimestamp, Calendar toTimestamp) throws IOException, InterruptedException, ParseException {
        BriefHistoryCommand command = new BriefHistoryCommand(server, projectPath, fromTimestamp, toTimestamp);
        Reader reader = null;
        try {
            reader = server.execute(command.getArguments());
            return command.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Gets all files from server.
     * @param localPath the local path to get all files into
     */
    public void getFiles(String localPath) throws IOException, InterruptedException {
        UpdateWorkfolderCommand command = new UpdateWorkfolderCommand(server, localPath);
        server.execute(command.getArguments()).close();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27).append(projectPath).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final Project other = (Project) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.projectPath, other.projectPath);
        return builder.isEquals();
    }
}
