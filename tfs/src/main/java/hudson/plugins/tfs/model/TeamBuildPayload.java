package hudson.plugins.tfs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.plugins.tfs.model.servicehooks.Event;

import java.util.List;
import java.util.Map;

@SuppressFBWarnings(value = "NM_FIELD_NAMING_CONVENTION", justification = "Public so shouldn't be changed")
public class TeamBuildPayload {

    @JsonProperty("parameter")
    public List<BuildParameter> BuildParameters;

    @JsonProperty("team-build")
    public Map<String, String> BuildVariables;

    @JsonProperty("team-event")
    public Event ServiceHookEvent;
}
