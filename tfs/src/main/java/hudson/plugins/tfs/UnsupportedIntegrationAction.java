package hudson.plugins.tfs;

import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.BuildCommand;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;

/**
 * Contributed to a build whenever the rich TFS/Team Services integration endpoints
 * are used with unsupported parameters.
 */
public class UnsupportedIntegrationAction extends InvisibleAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String reason;

    public UnsupportedIntegrationAction(final String reason) {
        this.reason = reason;
    }

    public static boolean isSupported(@Nonnull final Run<?, ?> run, @Nonnull final TaskListener listener) {
        final UnsupportedIntegrationAction action = run.getAction(UnsupportedIntegrationAction.class);
        if (action != null) {
            final String message = BuildCommand.formatUnsupportedReason(action.reason);
            listener.error(message);
            return false;
        }
        return true;
    }

    public static void addToBuild(final List<Action> actions, final String reason) {
        final UnsupportedIntegrationAction action = new UnsupportedIntegrationAction(reason);
        actions.add(action);
    }

}
