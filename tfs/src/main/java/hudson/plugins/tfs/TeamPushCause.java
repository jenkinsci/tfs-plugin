package hudson.plugins.tfs;

import hudson.triggers.SCMTrigger.SCMTriggerCause;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Indicates that a build was queued because of a TFS/Team Services Git code push event.
 */
public class TeamPushCause extends SCMTriggerCause {

    private final String pushedBy;

    public TeamPushCause(final String pushedBy) {
        this("", pushedBy);
    }

    public TeamPushCause(final File logFile, final String pushedBy) throws IOException {
        super(logFile);
        this.pushedBy = pushedBy;
    }

    public TeamPushCause(final String pollingLog, final String pushedBy) {
        super(pollingLog);
        this.pushedBy = pushedBy;
    }

    @Override
    public String getShortDescription() {
        final String template = "Started by TFS/Team Services push by %s";
        final String message = String.format(template, StringUtils.trimToEmpty(pushedBy));
        return message;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
