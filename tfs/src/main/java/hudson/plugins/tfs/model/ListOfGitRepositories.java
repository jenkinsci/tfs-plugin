//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;

import java.util.List;

public class ListOfGitRepositories {
    public int count;
    public List<GitRepository> value;
}
