//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import org.eclipse.jgit.transport.URIish;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

public class GitCodePushedEventArgs implements Serializable {
    private static final long serialVersionUID = 1L;

    public URI collectionUri;
    public URI repoUri;
    public String projectId;
    public String repoId;
    public String commit;
    public String pushedBy;
    public String targetBranch;

    public URIish getRepoURIish() {
        final String repoUriString = repoUri.toString();
        try {
            return new URIish(repoUriString);
        } catch (final URISyntaxException e) {
            // shouldn't happen
            throw new Error(e);
        }
    }
}
