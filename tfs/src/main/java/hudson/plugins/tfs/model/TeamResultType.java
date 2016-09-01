package hudson.plugins.tfs.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum TeamResultType {
    JUNIT("junit", "JUnit", Collections.singletonList("**/TEST-*.xml")),
    MAVEN("junit", "Maven", Arrays.asList(
            "**/surefire-reports/TEST-*.xml",
            "**/failsafe-reports/TEST-*.xml"
    )),
    ;

    private final String folderName;
    private final String displayName;
    private final List<String> defaultPatterns;

    TeamResultType(final String folderName, final String displayName, final List<String> defaultPatterns) {
        this.folderName = folderName;
        this.displayName = displayName;
        this.defaultPatterns = defaultPatterns;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDefaultPatterns() {
        return defaultPatterns;
    }
}
