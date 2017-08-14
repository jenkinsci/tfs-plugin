package hudson.plugins.tfs.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = "UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD", justification = "Used by TeamBuildPayload")
public class BuildParameter {
    public String name;
    public String value;
}
