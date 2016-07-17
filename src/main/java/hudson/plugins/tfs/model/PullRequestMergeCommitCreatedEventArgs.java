package hudson.plugins.tfs.model;

import net.sf.ezmorph.MorpherRegistry;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.JavaIdentifierTransformer;

import java.util.HashMap;

public class PullRequestMergeCommitCreatedEventArgs extends GitCodePushedEventArgs {
    public int pullRequestId;
    public int iterationId;

    public static PullRequestMergeCommitCreatedEventArgs fromJsonString(final String jsonString) {
        final JSONObject jsonObject = JSONObject.fromObject(jsonString);
        final PullRequestMergeCommitCreatedEventArgs result;

        final JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(PullRequestMergeCommitCreatedEventArgs.class);
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
            result = (PullRequestMergeCommitCreatedEventArgs) JSONObject.toBean(jsonObject, jsonConfig);
        }
        finally {
            registry.deregisterMorpher(URIMorpher.INSTANCE);
        }

        return result;
    }

}
