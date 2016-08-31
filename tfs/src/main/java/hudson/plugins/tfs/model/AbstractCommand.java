package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractProject;
import hudson.plugins.tfs.model.servicehooks.Event;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public abstract class AbstractCommand {

    public AbstractCommand() {
    }

    public interface Factory {
        AbstractCommand create();
        String getSampleRequestPayload();
    }

    /**
     * Actually do the work of the command, using the supplied {@code requestPayload} and
     * {@code teamBuildPayload}, then returning the output as a {@link JSONObject}.
     *
     * @param project an {@link AbstractProject to operate on}
     * @param request a {@link StaplerRequest} to help build parameter values
     * @param requestPayload a {@link JSONObject} representing the command's input
     * @param mapper an {@link ObjectMapper} instance to use to convert the {@link Event#resource}
     * @param teamBuildPayload a {@link TeamBuildPayload} representing the command's input
     * @param delay how long to wait before the project starts executing
     *
     * @return a {@link JSONObject} representing the hook event's output
     */
    public abstract JSONObject perform(final AbstractProject project, final StaplerRequest request, final JSONObject requestPayload, final ObjectMapper mapper, final TeamBuildPayload teamBuildPayload, final TimeDuration delay);

}
