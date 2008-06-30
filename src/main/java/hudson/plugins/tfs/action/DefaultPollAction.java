package hudson.plugins.tfs.action;

import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.util.ToolArgumentBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Calendar;

/**
 * Action that poll for changes at a project on a TFS server.
 * 
 * @author Erik Ramfelt
 */
public class DefaultPollAction {
   
    /**
     * Returns if there are changes in the repository since the timestamp
     * @param tool the tool to execute arguments with
     * @param timestamp the from time stamp
     * @param project the TeamFoundation project
     * @return true, if there are any changes; false otherwise
     */
    public boolean hasChanges( TfTool tool, TeamFoundationProject project, Calendar timestamp) throws IOException, InterruptedException {
        ToolArgumentBuilder builder = new ToolArgumentBuilder(project);
        MaskedArgumentListBuilder arguments = builder.getBriefHistoryArguments(timestamp, Calendar.getInstance());
        Reader reader = tool.execute(arguments.toCommandArray(), arguments.toMaskArray());
        boolean hasChanges = parseBriefHistoryOutput(new BufferedReader(reader));
        reader.close();
        
        return hasChanges;
    }
    
    /**
     * Parses the brief history output in the reader and returns if there are any changes
     * @param consoleReader the reader containing console output
     * @return true, if the history output says it has changes; false otherwise
     */
    private boolean parseBriefHistoryOutput(BufferedReader consoleReader) throws IOException {
        String line = consoleReader.readLine();
        while (line != null) {                
            if (line.startsWith("---")) {
                return true;
            }
            line = consoleReader.readLine();
        }        
        return false;
    }
    

}
