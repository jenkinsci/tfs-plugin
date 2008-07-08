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
        DetailedHistoryCommand command = new DetailedHistoryCommand(projectPath, fromTimestamp, toTimestamp);
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
        BriefHistoryCommand command = new BriefHistoryCommand(projectPath, fromTimestamp, toTimestamp);
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
        UpdateWorkfolderCommand command = new UpdateWorkfolderCommand(localPath);
        server.execute(command.getArguments()).close();
    }
}
