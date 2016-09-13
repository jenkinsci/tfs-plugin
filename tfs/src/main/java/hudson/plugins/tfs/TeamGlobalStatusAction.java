package hudson.plugins.tfs;

import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Run;

import java.io.Serializable;
import java.util.List;

/**
 * Added to the build when triggered by TFS/Team Services AND the "Enable Team Status for all jobs"
 * option was enabled.
 */
public class TeamGlobalStatusAction extends InvisibleAction implements Serializable {

    public static void addIfApplicable(final List<Action> actions) {
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        if (config.isEnableTeamStatusForAllJobs()) {
            actions.add(new TeamGlobalStatusAction());
        }
    }

    public static boolean isApplicable(final Run<?, ?> run) {
        final TeamGlobalStatusAction action = run.getAction(TeamGlobalStatusAction.class);
        return action != null;
    }

}
