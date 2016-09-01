package hudson.plugins.tfs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.plugins.tfs.model.servicehooks.Event;

import java.util.List;
import java.util.Map;

public class TeamBuildPayload {

    @JsonProperty("parameter")
    public List<BuildParameter> BuildParameters;

    @JsonProperty("team-build")
    public Map<String, String> BuildVariables;

    @JsonProperty("team-event")
    public Event ServiceHookEvent;
}
