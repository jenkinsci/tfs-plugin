package hudson.plugins.tfs.util;

import hudson.model.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ActionHelper {

    public static Action[] create(final Collection<Action> actions, final Action... additionalActions) {
        final int totalCount = actions.size() + additionalActions.length;
        final List<Action> allActions = new ArrayList<Action>(totalCount);
        allActions.addAll(actions);
        Collections.addAll(allActions, additionalActions);
        final Action[] result = allActions.toArray(new Action[totalCount]);
        return result;
    }

}
