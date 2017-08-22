//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author angoya
 */
public class ReleaseArtifactVersionsResponse 
{
    private List<ArtifactVersion> artifactVersions = new ArrayList<ArtifactVersion>();

    /**
    * 
    * @return
    * The artifactVersions
    */
    public List<ArtifactVersion> getArtifactVersions()
    {
        return artifactVersions;
    }

    /**
    * 
    * @param artifactVersions
    * The artifactVersions
    */
    public void setArtifactVersions(List<ArtifactVersion> artifactVersions)
    {
        this.artifactVersions = artifactVersions;
    }
}
