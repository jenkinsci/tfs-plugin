package hudson.plugins.tfs.util;

import hudson.Util;
import hudson.plugins.tfs.model.TeamFoundationCredentials;
import hudson.plugins.tfs.model.TeamFoundationProject;

import java.util.Calendar;

/**
 * Builds arguments for the Team Foundation command-line client.
 * 
 * @author Erik Ramfelt
 */
public class ToolArgumentBuilder {

    private final TeamFoundationProject project;

    public ToolArgumentBuilder(TeamFoundationProject project) {
        this.project = project;
    }

    /**
     * Returns arguments for the "history /format:brief" command
     * Describes previous changes made to one or more files or folders.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @param formatType type of format to get
     * @return arguments for the "history /format:brief" command
     */
    public MaskedArgumentListBuilder getBriefHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp) {
        return getHistoryArguments(fromTimestamp, toTimestamp, "brief");
    }

    /**
     * Returns arguments for the "history /format:detailed" command
     * Describes previous changes made to one or more files or folders.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @param formatType type of format to get
     * @return arguments for the "history /format:detailed" command
     */
    public MaskedArgumentListBuilder getDetailedHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp) {
        return getHistoryArguments(fromTimestamp, toTimestamp, "detailed");
    }
    
    /**
     * Returns arguments for the "history" command
     * Describes previous changes made to one or more files or folders.
     * @param fromTimestamp the from time stamp
     * @param toTimestamp the to time stamp
     * @param formatType type of format to get
     * @return arguments for the "history" command
     */
    public MaskedArgumentListBuilder getHistoryArguments(Calendar fromTimestamp, Calendar toTimestamp, String formatType) {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("history");
        arguments.add("/noprompt");
        arguments.add(String.format("/server:%s",  project.getServer()));
        arguments.add(project.getProject());
        arguments.add(String.format("/version:D%s~D%s", Util.XS_DATETIME_FORMATTER.format(fromTimestamp.getTime()), 
                Util.XS_DATETIME_FORMATTER.format(toTimestamp.getTime())));
        arguments.add("/recursive");
        arguments.add(String.format("/format:%s", formatType));        
        addCredentials(project.getCredentials(), arguments);        
        return arguments;
    }

    /**
     * Returns arguments for the "workspace /new" command
     * Enables you to create, delete, and modify properties and mappings associated with a workspace.
     * @param workspaceName the name of the workspace to create
     * @return arguments for the "workspace /new" command
     */
    public MaskedArgumentListBuilder getNewWorkspaceArguments(String workspacename) {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspace");
        arguments.add("/new");
        arguments.add(String.format("/server:%s", project.getServer()));
        addCredentials(project.getCredentials(), arguments);
        if (workspacename != null) {
            arguments.add(workspacename);
        }        
        return arguments;
    }

    /**
     * Returns arguments for the "workspace /delete" command
     * Enables you to create, delete, and modify properties and mappings associated with a workspace.
     * @param workspaceName the name of the workspace to delete
     * @return arguments for the "workspace /delete" command
     */
    public MaskedArgumentListBuilder getDeleteWorkspaceArguments(String workspaceName) {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workspace");
        arguments.add(String.format("/remove:%s", workspaceName));
        arguments.add(String.format("/server:%s", project.getServer()));
        addCredentials(project.getCredentials(), arguments);        
        return arguments;
    }

    /**
     * Returns arguments for the "workfold" command.
     * Creates, modifies, or displays information about the mappings between your 
     * workspace folders and the source control server folders to which they correspond.
     * @param localFolder the local folder
     * @param workspaceName the name of the workspace, optional
     * @return arguments for the "workfold" command
     */
    public MaskedArgumentListBuilder getWorkfoldArguments(String localFolder, String workspaceName) {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("workfold");
        arguments.add(String.format("/server:%s", project.getServer()));
        addCredentials(project.getCredentials(), arguments);
        if (workspaceName != null) {
            arguments.add(String.format("/workspace:%s", workspaceName));
        }
        arguments.add(project.getProject());
        arguments.add(localFolder);        
        return arguments;
    }

    /**
     * Returns arguments for the "get" command.
     * Retrieves a read-only copy of one or more files from the source control 
     * server to the local disk. Any intermediate folders are created if necessary.
     * <p/>
     * The version to retrieve may be specified through the 'version' option or as a 
     * version specification suffix to the item specification (example: '$/file.txt;C34').
     * @return arguments for the "get" command.
     */
    public MaskedArgumentListBuilder getGetArguments() {
        MaskedArgumentListBuilder arguments = new MaskedArgumentListBuilder();        
        arguments.add("get");
        arguments.add("/recursive");
        arguments.add(String.format("/server:%s", project.getServer()));
        addCredentials(project.getCredentials(), arguments);
        return arguments;
    }
    
    /**
     * Add credentials to the arguments if they are not null
     * @param credentials the credentials
     * @param arguments the arguments to add the credentials string too.
     */
    private void addCredentials(TeamFoundationCredentials credentials, MaskedArgumentListBuilder arguments) {
        if (credentials != null) {
            arguments.addMaskedArgument(String.format("/login:%s@%s,%s", 
                    credentials.getUsername(), credentials.getDomain(), credentials.getPassword()));             
        }
    }
}
