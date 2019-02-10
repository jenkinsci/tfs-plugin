package hudson.plugins.tfs;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Extension class of ParametersAction to pass in parameters as safe parameters..
 */
@Restricted(NoExternalUse.class)
public class SafeParametersAction extends ParametersAction {

    private List<ParameterValue> parameters;

    public SafeParametersAction(final List<ParameterValue> parameters) {
        this.parameters = parameters;
    }

    public SafeParametersAction(final ParameterValue... parameters) {
        this(Arrays.asList(parameters));
    }

    /**
     * Will be called when the object is unmarshalled.  Because the normal
     * constructors are not called in such a case, things may not be initialized
     * correctly.  This will check that ParametersAction.parameters is not null
     * @return Correctly initialized SafeParametersAction
     */
    protected Object readResolve() {
        try {
            // if ParametersAction is not a null everything is ok
            if (super.getParameters() != null) {
                return this;
            }
        } catch (NullPointerException npe) {
        }

        // Either super.getParameters was null or threw a NPE
        // so create a new SafeParametersAction that initializes
        // everything correctly.  This should rarely happen
        return new SafeParametersAction(this.getParameters());
    }

    @Override
    public List<ParameterValue> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    @Override
    public ParameterValue getParameter(final String name) {
        for (ParameterValue parameter : parameters) {
            if (parameter != null && parameter.getName().equals(name)) {
                return parameter;
            }
        }

        return null;
    }

    /**
    * Environment contributor for SafeParametersAction.
    */
    @Extension
    public static final class SafeParametersActionEnvironmentContributor extends EnvironmentContributor {

        @Override
        public void buildEnvironmentFor(final Run r, final EnvVars envs, final TaskListener listener) throws IOException, InterruptedException {
            SafeParametersAction action = r.getAction(SafeParametersAction.class);
            if (action != null) {
                for (ParameterValue p : action.getParameters()) {
                    envs.putIfNotNull(p.getName(), String.valueOf(p.getValue()));
                }
            }
        }
    }
}
