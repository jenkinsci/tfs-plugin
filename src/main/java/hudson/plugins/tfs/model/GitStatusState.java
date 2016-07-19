package hudson.plugins.tfs.model;

public enum GitStatusState {

    NotSet(0),
    Pending(1),
    Succeeded(2),
    Failed(3),
    Error(4),
    ;

    private final int value;

    GitStatusState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
