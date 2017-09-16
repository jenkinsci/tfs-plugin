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
    private final String runConfig;

    public TeamPushCause(final String pushedBy, final String runConfig) {
        this("", pushedBy, runConfig);
    }

    public TeamPushCause(final File logFile, final String pushedBy, final String runConfig) throws IOException {
        super(logFile);
        this.pushedBy = pushedBy;
        this.runConfig = runConfig;
    }

    public TeamPushCause(final String pollingLog, final String pushedBy, final String runConfig) {
        super(pollingLog);
        this.pushedBy = pushedBy;
        this.runConfig = runConfig;
    }

    @Override
    public String getShortDescription() {
        final String template = "Started by TFS/Team Services push by %s";
        final String message = String.format(template, StringUtils.trimToEmpty(pushedBy));
        return message;
    }

    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getRunConfig() {
        return this.runConfig;
    }
}
