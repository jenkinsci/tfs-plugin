package hudson.plugins.tfs.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Used by TFS Build payload.
 */
@SuppressFBWarnings(value = "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD", justification = "Used by TeamBuildPayload")
public class BuildParameter {

    // Turn off checkstyle for the public fields in this class.
    //CHECKSTYLE:OFF

    public String name;

    public String value;

    //CHECKSTYLE:ON

}
