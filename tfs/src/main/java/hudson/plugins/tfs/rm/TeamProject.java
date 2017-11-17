//CHECKSTYLE:OFF
package hudson.plugins.tfs.rm;

import java.util.List;

public class TeamProject {
    private Integer id;
    private List<Project> value;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public List<Project> getValue() {
        return value;
    }

    public void setValue(final List<Project> value) {
        this.value = value;
    }
}
