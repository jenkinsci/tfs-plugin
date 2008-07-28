package hudson.plugins.tfs.browsers;

import java.io.IOException;
import java.net.URL;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.RepositoryBrowser;

/**
 * Repository browser for the Team Foundation Server SCM
 * 
 * @author Erik Ramfelt
 */
public abstract class TeamFoundationServerRepositoryBrowser extends RepositoryBrowser<ChangeSet> {
    /**
     * Determines the link to the diff between the version
     * in the specified revision of {@link ChangeSet.Item} to its previous version.
     *
     * @return null if the browser doesn't have any URL for diff.
     */
    public abstract URL getDiffLink(ChangeSet.Item item) throws IOException;

    /**
     * Determines the link to a single file under TFS.
     *
     * @return null if the browser doesn't have any suitable URL.
     */
    public abstract URL getFileLink(ChangeSet.Item item) throws IOException;
    
    private static final long serialVersionUID = 1L;
}
