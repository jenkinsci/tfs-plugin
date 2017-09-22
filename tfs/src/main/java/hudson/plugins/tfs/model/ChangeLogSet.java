package hudson.plugins.tfs.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.scm.RepositoryBrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ChangeLogSet for the Team Foundation Server SCM
 * The log set will set the parent of the log entries in the constructor.
 *
 * @author Erik Ramfelt
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Public so shouldn't be changed")
public class ChangeLogSet extends hudson.scm.ChangeLogSet<ChangeSet> {

    private final List<ChangeSet> changesets;

    public ChangeLogSet(final Run<?, ?> build, final RepositoryBrowser<?> browser, final List<ChangeSet> changesets) {
        super(build, browser);
        this.changesets = changesets;
        for (ChangeSet changeset : changesets) {
            changeset.setParent(this);
        }
    }

    @Deprecated
    /* TODO: Used by TeamSystemWebAccessBrowserTest, should update to use non-deprecated method instead */
    public ChangeLogSet(final AbstractBuild build, final ChangeSet[] changesetArray) {
        this(build, build.getProject().getScm().getEffectiveBrowser(), changesetArray);
    }

    public ChangeLogSet(final Run<?, ?> build, final RepositoryBrowser<?> browser, final ChangeSet[] changesetArray) {
        super(build, browser);
        changesets = new ArrayList<ChangeSet>();
        for (ChangeSet changeset : changesetArray) {
            changeset.setParent(this);
            changesets.add(changeset);
        }
    }

    @Override
    public boolean isEmptySet() {
        return changesets.isEmpty();
    }

    /** Returns a ChangeSet iterator. */
    public Iterator<ChangeSet> iterator() {
        return changesets.iterator();
    }
}
