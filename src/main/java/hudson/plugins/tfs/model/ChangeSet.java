package hudson.plugins.tfs.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.User;
import hudson.plugins.tfs.util.DateUtil;
import hudson.scm.EditType;

@ExportedBean(defaultVisibility=999)
public class ChangeSet extends hudson.scm.ChangeLogSet.Entry {

    private String version;
    private String user;
    private String domain;
    private Date date;
    private String comment;
    private List<Item> items;
	private String checkedInByUser;
    
    public ChangeSet() {
        this("", null, "", "");
    }
    
    public ChangeSet(String version, Date date, String user, String comment) {
        this.version = version;
        this.date = date;
        this.comment = comment;
        items = new ArrayList<Item>();
        setUser(user);
    }
    
    @Override
    public Collection<String> getAffectedPaths() {
        Collection<String> paths = new ArrayList<String>(items.size());
        for (Item item : items) {
            paths.add(item.getPath());
        }
        return paths;
    }

    @Override
    public ChangeLogSet getParent() {
        return (ChangeLogSet)super.getParent();
    }

    @Override
    public User getAuthor() {
        return User.get(user);
    }

    @Override
    public String getMsg() {
        return comment;
    }
    
    @Exported
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Exported
    public String getDomain() {
        return domain;
    }

    @Exported
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {        
        String[] split = user.split("\\\\");
        if (split.length == 2) {
            this.domain = split[0];
            this.user = split[1];
        } else {
            this.user = user;
            this.domain = null;
        }
    }

    @Exported
    public Date getDate() {
        return date;
    }
    
    public void setDateStr(String dateStr) throws ParseException {
        date = DateUtil.TFS_DATETIME_FORMATTER.get().parse(dateStr);
    }

    @Exported
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

	public void setCheckedInBy(String user) {
		checkedInByUser = user;
	}

	public String getCheckedInBy() {
		return checkedInByUser;
	}

    public User getCheckedInByUser() {
        return User.get(checkedInByUser);
    }

    @Exported
    public List<Item> getItems() {
        return items;
    }
    
    public void add(ChangeSet.Item item) {
        items.add(item);
        item.setParent(this);
    }

    @Override
    protected void setParent(hudson.scm.ChangeLogSet parent) {
        super.setParent(parent);
    }
    
    @ExportedBean(defaultVisibility=999)
    public static class Item {
        private String path;
        private String action;
        private ChangeSet parent;

        public Item() {
            this("","");
        }
        
        public Item(String path, String action) {
            this.path = path;
            this.action = action;
        }

        public ChangeSet getParent() {
            return parent;
        }

        void setParent(ChangeSet parent) {
            this.parent = parent;
        }

        @Exported
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Exported   
        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        @Exported
        public EditType getEditType() {
            if (action.equalsIgnoreCase("delete")) {
                return EditType.DELETE;
            }
            if (action.equalsIgnoreCase("add")) {
                return EditType.ADD;
            }
            return EditType.EDIT;
        }
    }
}
