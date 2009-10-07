package hudson.plugins.tfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.remoting.Callable;
import hudson.util.ForkOutputStream;

/**
 * Class that encapsulates the Team Foundation command client.
 * 
 * @author Erik Ramfelt
 */
public class TfTool {

    static final int SUCCESS_EXIT_CODE = 0;
    static final int PARTIAL_SUCCESS_EXIT_CODE = 1;
    
    private Launcher launcher;
    private TaskListener listener;
    private FilePath workspace;
    private final String executable;
    
    private static final Logger LOGGER = Logger.getLogger(TfTool.class.getName());
    
    public TfTool(String executable, Launcher launcher, TaskListener listener, FilePath workspace) {
        this.executable = executable;
        this.launcher = launcher;
        this.listener = listener;
        this.workspace = workspace;
    }

    public TaskListener getListener() {
        return listener;
    }
    
    /**
     * Returns the host name of the computer that is running the TF tool
     * @return the host name; or null if there was a problem looking it up
     */
    public String getHostname() throws IOException, InterruptedException {
        try {
            return workspace.act(new Callable<String, UnknownHostException>() {
                private static final long serialVersionUID = 1L;
                public String call() throws UnknownHostException {
                    return InetAddress.getLocalHost().getHostName();
                }            
            });
        } catch (UnknownHostException e) {
            LOGGER.warning("Could not resolve local hostname needed to list workspaces. " + e);
            return null;
        }
    }

    /**
     * Execute the arguments, and return the console output as a Reader
     * @param arguments arguments to send to the command-line client.
     * @return a Reader containing the console output
     */
    public Reader execute(String[] arguments) throws IOException, InterruptedException {
        return execute(arguments, null);
    }

    /**
     * Execute the arguments, and return the console output as a Reader
     * @param arguments arguments to send to the command-line client.
     * @param masks which of the commands that should be masked from the console.
     * @return a Reader containing the console output
     */
    public Reader execute(String[] arguments, boolean[] masks) throws IOException, InterruptedException {

        String[] toolArguments = new String[arguments.length + 1];
        toolArguments[0] = executable;
        for (int i = 0; i < arguments.length; i++) {
            toolArguments[i + 1] = arguments[i];
        }
        
        boolean[] toolMasks = new boolean[arguments.length + 1];
        if (masks != null) {
            toolMasks = new boolean[masks.length + 1];
            toolMasks[0] = false;
            for (int i = 0; i < masks.length; i++) {
                toolMasks[i + 1] = masks[i];
            }
        }
        
        ByteArrayOutputStream consoleStream = new ByteArrayOutputStream();
        Proc proc = launcher.launch().cmds(toolArguments).masks(toolMasks)
                .stdout(new ForkOutputStream(consoleStream, listener.getLogger()))
                .pwd(workspace).start();
        consoleStream.close();
        
        int result = proc.join();
        LOGGER.fine(String.format("The TFS command '%s' returned with an error code of %d", toolArguments[1], result));
        if ((result == SUCCESS_EXIT_CODE) || (result == PARTIAL_SUCCESS_EXIT_CODE)) {
            return new InputStreamReader(new ByteArrayInputStream(consoleStream.toByteArray()));
        } else {
            listener.fatalError(String.format("Executable returned an unexpected result code [%d]", result));
            throw new AbortException();
        }
    }
}
