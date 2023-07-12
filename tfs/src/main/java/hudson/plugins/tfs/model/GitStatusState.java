//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public enum GitStatusState {

    NotSet(0),
    Pending(1),
    Succeeded(2),
    Failed(3),
    Error(4),
    ;

    public static final Map<String, GitStatusState> CASE_INSENSITIVE_LOOKUP;

    static {
        final Map<String, GitStatusState> map = new TreeMap<String, GitStatusState>(String.CASE_INSENSITIVE_ORDER);
        for (final GitStatusState value : GitStatusState.values()) {
            map.put(value.name(), value);
        }
        CASE_INSENSITIVE_LOOKUP = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unused" /* Invoked by Jackson via @JsonCreator */)
    @JsonCreator
    public static GitStatusState caseInsensitiveValueOf(final String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        }
        if (!CASE_INSENSITIVE_LOOKUP.containsKey(name)) {
            throw new IllegalArgumentException("No enum constant " + name);
        }
        return CASE_INSENSITIVE_LOOKUP.get(name);
    }

    private final int value;

    GitStatusState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
