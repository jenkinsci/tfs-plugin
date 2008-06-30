package hudson.plugins.tfs.model;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;

import java.util.Iterator;
import java.util.List;

/**
 * ChangeLogSet for the Team Foundation Server SCM
 * The log set will set the parent of the log entries in the constructor.
 * 
 * @author Erik Ramfelt
 */
public class TeamFoundationChangeLogSet extends ChangeLogSet<TeamFoundationChangeSet> {

    List<TeamFoundationChangeSet> changesets;
    
    public TeamFoundationChangeLogSet(AbstractBuild<?, ?> build, List<TeamFoundationChangeSet> changesets) {
        super(build);
        this.changesets = changesets;
        for (TeamFoundationChangeSet changeset : changesets) {
            changeset.setParent(this);
        }
    }

    @Override
    public boolean isEmptySet() {
        return changesets.isEmpty();
    }

    public Iterator<TeamFoundationChangeSet> iterator() {
        return changesets.iterator();
    }
}
