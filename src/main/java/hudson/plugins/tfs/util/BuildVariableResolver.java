package hudson.plugins.tfs.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.util.VariableResolver;

/**
 * A {@link VariableResolver} that resolves certain Build variables.
 * <p>
 * The build variable resolver will resolve the following:
 * <ul>
 * <li> JOB_NAME - The name of the job</li>
 * <li> USER_NAME - The system property "user.name" on the Node that the Launcher
 * is being executed on (slave or master)</li>
 * <li> NODE_NAME - The name of the node that the Launcher is being executed on</li>
 * <li> Any environment variable that is set on the Node that the Launcher is
 * being executed on (slave or master)</li> 
 * </ul> 
 * 
 * @author Erik Ramfelt
 */
public class BuildVariableResolver implements VariableResolver<String> {
    
    private Map<String,LazyResolver> lazyResolvers = new HashMap<String, LazyResolver>();
    
    private List<VariableResolver<String>> otherResolvers = new ArrayList<VariableResolver<String>>();
    
    private final Computer computer;

    private static final Logger LOGGER = Logger.getLogger(BuildVariableResolver.class.getName());
    
    public BuildVariableResolver(final Job<?, ?> job) {
        computer = null;
        lazyResolvers.put("JOB_NAME", new LazyResolver() {
            public String getValue() {
                return job.getName();
            }            
        });
    }
    
    public BuildVariableResolver(final AbstractProject<?, ?> project, final Computer computer) {
        this.computer = computer;
        lazyResolvers.put("JOB_NAME", new LazyResolver() {
            public String getValue() {
                return project.getName();
            }            
        });
        lazyResolvers.put("NODE_NAME", new LazyComputerResolver() {
            public String getValue(Computer computer) {
                return (Util.fixEmpty(computer.getName()) == null ? "MASTER" : computer.getName());
            }            
        });
        lazyResolvers.put("USER_NAME", new LazyComputerResolver() {
            public String getValue(Computer computer) throws IOException, InterruptedException {
                return (String) computer.getSystemProperties().get("user.name");
            }            
        });
    }

    /**
     * Constructor that can be used with a {@linkplain AbstractBuild} instance. 
     * <p>
     * This constructor should not be called in a method that may be called by
     * {@link AbstractBuild#getEnvVars()}.  
     * @param build used to get the project and the build env vars
     */
    public BuildVariableResolver(final AbstractBuild<?, ?> build, final Computer computer)
            throws IOException, InterruptedException {
        this(build.getProject(), computer);
        
        final Map<String, String> envVars = build.getEnvironment(TaskListener.NULL);
        if (envVars != null) {
            otherResolvers.add(new VariableResolver.ByMap<String>(envVars));
        }
    }
    
    public String resolve(String variable) {
        try {
            if (lazyResolvers.containsKey(variable)) {
                return lazyResolvers.get(variable).getValue();
            } else {
                if (computer != null) {
                    otherResolvers.add(new VariableResolver.ByMap<String>(computer.getEnvironment()));
                }
                return new VariableResolver.Union<String>(otherResolvers).resolve(variable);
            }
        } catch (Exception e) {
            LOGGER.warning("Variable name '" + variable + "' look up failed because of " + e);
        }
        return null;
    }

    /**
     * Simple lazy variable resolver
     */
    private interface LazyResolver {
        String getValue() throws IOException, InterruptedException;
    }
    
    /**
     * Class to handle cases when a Launcher was not created from a computer.
     * @see Launcher#getComputer()
     */
    private abstract class LazyComputerResolver implements LazyResolver {
        protected abstract String getValue(Computer computer) throws IOException, InterruptedException;
        public String getValue() throws IOException, InterruptedException {
            return getValue(computer);
        }
    }
}
