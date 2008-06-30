package hudson.plugins.tfs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.util.ForkOutputStream;

/**
 * Class that encapsulates the Team Foundation command client.
 * 
 * @author Erik Ramfelt
 */
public class TfTool {

    private Launcher launcher;
    private TaskListener listener;
    private FilePath workspace;
    private final String executable;
    
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
        Proc proc = launcher.launch(toolArguments, toolMasks, new String[]{}, 
                null, new ForkOutputStream(consoleStream, listener.getLogger()), 
                workspace);
        consoleStream.close();
        
        int result = proc.join();
        if (result == 0) {
            return new InputStreamReader(new ByteArrayInputStream(consoleStream.toByteArray()));
        } else {
            listener.fatalError(String.format("Executable returned an unexpected result code [%d]", result));
            throw new AbortException();
        }
    }
}
