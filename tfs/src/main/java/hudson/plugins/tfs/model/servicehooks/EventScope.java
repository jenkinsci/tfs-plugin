package hudson.plugins.tfs.model.servicehooks;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Enum used to specify the scope of the incoming event from TFS/VSTS.
 */
public enum EventScope {
    /**
     * No input scope specified.
     */
    All,

    /**
     * Team Project scope.
     */
    Project,

    /**
     * Team scope.
     */
    Team,

    /**
     * Collection scope.
     */
    Collection,

    /**
     * Account scope.
     */
    Account,

    /**
     * Deployment scope.
     */
    Deployment;

    private static final Map<String, EventScope> CASE_INSENSITIVE_LOOKUP;

    static {
        final Map<String, EventScope> map = new TreeMap<String, EventScope>(String.CASE_INSENSITIVE_ORDER);
        for (final EventScope value : EventScope.values()) {
            map.put(value.name(), value);
        }
        CASE_INSENSITIVE_LOOKUP = Collections.unmodifiableMap(map);
    }

    /**
     * Use to compare event scopes to a string representation of the scope.
     */
    @SuppressWarnings("unused" /* Invoked by Jackson via @JsonCreator */)
    @JsonCreator
    public static EventScope caseInsensitiveValueOf(final String name) {
        if (name == null) {
            throw new NullPointerException("Name is null");
        }
        if (!CASE_INSENSITIVE_LOOKUP.containsKey(name)) {
            throw new IllegalArgumentException("No enum constant " + name);
        }
        return CASE_INSENSITIVE_LOOKUP.get(name);
    }
}
