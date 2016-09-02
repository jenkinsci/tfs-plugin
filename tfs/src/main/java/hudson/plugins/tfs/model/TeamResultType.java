package hudson.plugins.tfs.model;

public enum TeamResultType {
    JUNIT("junit", "JUnit"),
    MAVEN("junit", "Maven"),
    ;

    private final String folderName;
    private final String displayName;

    TeamResultType(final String folderName, final String displayName) {
        this.folderName = folderName;
        this.displayName = displayName;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
