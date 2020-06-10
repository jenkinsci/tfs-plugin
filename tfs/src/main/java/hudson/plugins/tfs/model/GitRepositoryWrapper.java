//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

@ExportedBean(defaultVisibility = 999)
public class GitRepositoryWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public String defaultBranch;
    public String name;
    public String projectAbbreviation;
    public String projectDescription;
    public String projectName;
    public String projectUrl;
    public String remoteUrl;
    public String url;

    GitRepositoryWrapper() {

    }

    GitRepositoryWrapper(GitRepository gitRepository) {
        defaultBranch = gitRepository.getDefaultBranch();
        name = gitRepository.getName();
        projectAbbreviation = gitRepository.getProject().getAbbreviation();
        projectDescription = gitRepository.getProject().getDescription();
        projectName = gitRepository.getProject().getName();
        projectUrl = gitRepository.getProject().getUrl();
        remoteUrl = gitRepository.getRemoteUrl();
        url = gitRepository.getUrl();
    }
}
