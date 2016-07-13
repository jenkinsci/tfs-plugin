package hudson.plugins.tfs;

import hudson.triggers.SCMTrigger.SCMTriggerCause;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Indicates that a build was queued because of a VSTS push hook.
 */
public class VstsPushCause extends SCMTriggerCause {

    private final String pushedBy;

    public VstsPushCause(final String pushedBy) {
        this("", pushedBy);
    }

    public VstsPushCause(final File logFile, final String pushedBy) throws IOException {
        super(logFile);
        this.pushedBy = pushedBy;
    }

    public VstsPushCause(final String pollingLog, final String pushedBy) {
        super(pollingLog);
        this.pushedBy = pushedBy;
    }

    @Override
    public String getShortDescription() {
        final String template = "Started by VSTS push by %s";
        final String message = String.format(template, StringUtils.trimToEmpty(pushedBy));
        return message;
    }
}
