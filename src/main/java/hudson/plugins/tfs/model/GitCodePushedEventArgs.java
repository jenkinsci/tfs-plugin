package hudson.plugins.tfs.model;

import net.sf.ezmorph.MorpherRegistry;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.JavaIdentifierTransformer;
import org.eclipse.jgit.transport.URIish;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class GitCodePushedEventArgs {
    public URI collectionUri;
    public URI repoUri;
    public String projectId;
    public String repoId;
    public String commit;
    public String pushedBy;
    public List<WorkItem> workItems;

    public static GitCodePushedEventArgs fromJsonString(final String jsonString) {
        final JSONObject jsonObject = JSONObject.fromObject(jsonString);
        final GitCodePushedEventArgs result;

        final JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(GitCodePushedEventArgs.class);
        final HashMap<String, Class> classMap = new HashMap<String, Class>();
        // TODO: the classMap is used without context, every property named here will try to use the type
        classMap.put("workItems", WorkItem.class);
        classMap.put("html", Link.class);
        jsonConfig.setClassMap(classMap);
        jsonConfig.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {
            @Override
            public String transformToJavaIdentifier(final String str) {
                return str.replace(".", "_");
            }
        });

        final MorpherRegistry registry = JSONUtils.getMorpherRegistry();

        // TODO: I don't like messing with singletons; is there a way to do this with JsonConfig?
        registry.registerMorpher(URIMorpher.INSTANCE);
        try {
            result = (GitCodePushedEventArgs) JSONObject.toBean(jsonObject, jsonConfig);
        }
        finally {
            registry.deregisterMorpher(URIMorpher.INSTANCE);
        }

        return result;
    }

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
