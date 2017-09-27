package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.core.webapi.model.TeamProjectReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitCommitRef;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPush;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.IdentityRef;
import hudson.model.Action;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.model.servicehooks.ResourceContainer;
import hudson.plugins.tfs.util.ResourceHelper;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A Git push event corresponding to the push of a set of commits on VSTS/TFS.
 */
public class GitPushEvent extends AbstractHookEvent {

    /**
     * Factory for creating a GitPushEvent.
     */
    public static class Factory implements AbstractHookEvent.Factory {

        @Override
        public AbstractHookEvent create() {
            return new GitPushEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return ResourceHelper.fetchAsString(this.getClass(), "GitPushEvent.json");
        }
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage) {
        final Object resource = serviceHookEvent.getResource();
        final GitPush gitPush = mapper.convertValue(resource, GitPush.class);

        final GitCodePushedEventArgs args = decodeGitPush(gitPush, serviceHookEvent);
        final CommitParameterAction parameterAction = new CommitParameterAction(args);
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(parameterAction);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, actions, false);
        final JSONObject response = fromResponseContributors(contributors);
        return response;
    }

    static URI determineCollectionUri(final URI repoApiUri) {
        final String path = repoApiUri.getPath();
        final int i = path.indexOf("_apis/");
        if (i == -1) {
            final String template = "Repository url '%s' did not contain '_apis/'.";
            throw new IllegalArgumentException(String.format(template, repoApiUri));
        }
        final String pathBeforeApis = path.substring(0, i);
        final URI uri;
        try {
            uri = new URI(repoApiUri.getScheme(), repoApiUri.getAuthority(), pathBeforeApis, repoApiUri.getQuery(), repoApiUri.getFragment());
        } catch (final URISyntaxException e) {
            throw new Error(e);
        }
        return uri;
    }

    static URI determineCollectionUri(final GitRepository repository, final Event serviceHookEvent) {
        URI result = null;
        final Map<String, ResourceContainer> containers = serviceHookEvent.getResourceContainers();
        if (containers != null) {
            final String collection = "collection";
            if (containers.containsKey(collection)) {
                final ResourceContainer collectionContainer = containers.get(collection);
                final String baseUrl = collectionContainer.getBaseUrl();
                if (StringUtils.isNotEmpty(baseUrl)) {
                    result = URI.create(baseUrl);
                }
            }
        }
        if (result == null) {
            final String repoApiUrlString = repository.getUrl();
            final URI repoApiUri = URI.create(repoApiUrlString);
            result = determineCollectionUri(repoApiUri);
        }
        return result;
    }

    static String determineProjectId(final GitRepository repository) {
        final TeamProjectReference project = repository.getProject();
        final String result = project.getName();
        return result;
    }

    static String determineCommit(final GitPush gitPush) {
        final List<GitCommitRef> commits = gitPush.getCommits();
        if (commits == null || commits.size() < 1) {
            return null;
        }
        final GitCommitRef commit = commits.get(0);
        return commit.getCommitId();
    }

    static String determinePushedBy(final GitPush gitPush) {
        final IdentityRef pushedBy = gitPush.getPushedBy();
        final String result = pushedBy.getDisplayName();
        return result;
    }

    static String determineTargetBranch(final GitPush gitPush) {
        // In the form of ref/heads/master
        final String targetBranch = gitPush.getRefUpdates().get(0).getName();
        String[] items = targetBranch.split("/");
        return items[items.length - 1];
    }

    static GitCodePushedEventArgs decodeGitPush(final GitPush gitPush, final Event serviceHookEvent) {
        final GitRepository repository = gitPush.getRepository();
        final URI collectionUri = determineCollectionUri(repository, serviceHookEvent);
        final String repoUriString = repository.getRemoteUrl();
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getName();
        final String commit = determineCommit(gitPush);
        final String pushedBy = determinePushedBy(gitPush);
        final String targetBranch = GitPushEvent.determineTargetBranch(gitPush);

        final GitCodePushedEventArgs args = new GitCodePushedEventArgs();
        args.collectionUri = collectionUri;
        args.repoUri = repoUri;
        args.projectId = projectId;
        args.repoId = repoId;
        args.commit = commit;
        args.pushedBy = pushedBy;
        args.targetBranch = targetBranch;
        return args;
    }
}
