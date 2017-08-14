//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

/**
 * Enum to represent the type of build result (junit tests, cobertura report, etc.).
 */
public enum TeamResultType {
    JUNIT("junit", "JUnit"),
    NUNIT("nunit", "NUnit"),
    VS_TEST("vstest", "VSTest"),
    XUNIT("xunit", "XUnit"),
    COBERTURA("cobertura", "Cobertura"),
    JACOCO("jacoco", "JaCoCo");

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
