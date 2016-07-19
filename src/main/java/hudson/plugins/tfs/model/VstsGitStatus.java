package hudson.plugins.tfs.model;

import net.sf.json.JSONObject;

public class VstsGitStatus {

    public GitStatusState state;
    public String description;
    public String targetUrl;
    public GitStatusContext context;

    public String toJson() {
        final JSONObject jsonObject = JSONObject.fromObject(this);
        final String result = jsonObject.toString();
        return result;
    }
}
