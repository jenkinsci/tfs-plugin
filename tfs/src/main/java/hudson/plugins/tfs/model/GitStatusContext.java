//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class GitStatusContext {
    public String name;
    public String genre;

    public GitStatusContext() {
    }

    public GitStatusContext(final String name, final String genre) {
        this.name = name;
        this.genre = genre;
    }

    public static GitStatusContext fromJsonString(final String jsonString) {
        final JSONObject jsonObject = JSONObject.fromObject(jsonString);
        final GitStatusContext result;

        final JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(GitStatusContext.class);

        result = (GitStatusContext) JSONObject.toBean(jsonObject, jsonConfig);

        return result;
    }
}
