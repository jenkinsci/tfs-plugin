package hudson.plugins.tfs;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.RepositoryBrowser;

/**
 * Repository browser for the Team Foundation Server SCM
 * 
 * @author Erik Ramfelt
 */
public abstract class TeamFoundationServerRepositoryBrowser extends RepositoryBrowser<ChangeSet> {
    
    private static final long serialVersionUID = 1L;
}
