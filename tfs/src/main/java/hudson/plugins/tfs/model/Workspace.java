//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

public class Workspace implements Serializable{

    private final String name;
    private final String computer;
    private final String owner;
    private final String comment;
    private final boolean isLocalWorkspace;

    public Workspace(String name, String computer, String owner, String comment, boolean isLocalWorkspace) {
        this.name = name;
        this.computer = computer;
        this.owner = owner;
        this.comment = comment;
        this.isLocalWorkspace = isLocalWorkspace;
    }
    
    public Workspace(String name) {
        this(name, "", "", "", false);
    }

    public String getName() {
        return name;
    }

    public String getComputer() {
        return computer;
    }

    public String getOwner() {
        return owner;
    }

    public String getComment() {
        return comment;
    }

	public boolean isLocalWorkspace() {
		return isLocalWorkspace;
	}

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 27).append(name).append(owner).append(computer).append(isLocalWorkspace).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final Workspace other = (Workspace) obj;
        EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.name, other.name);
        builder.append(this.owner, other.owner);
        builder.append(this.computer, other.computer);
        builder.append(this.isLocalWorkspace, other.isLocalWorkspace);
        return builder.isEquals();
    }
}
