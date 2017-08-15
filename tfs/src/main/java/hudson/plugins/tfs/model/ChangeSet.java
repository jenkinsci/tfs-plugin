//CHECKSTYLE:OFF
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

    private User authorUser;
    private User checkedInByUser;
    private String version;
    private String userString;
    private String domain;
    private Date date;
    private String comment;
    private List<Item> items;
    private String checkedInByUserString;
    
    public ChangeSet() {
        this("", null, "", "");
    }
    
    public ChangeSet(String version, Date date, String userString, String comment) {
        this.version = version;
        this.date = (date != null) ? new Date(date.getTime()) : null;
        this.comment = comment;
        items = new ArrayList<>();
        setUser(userString);
    }
    
    public ChangeSet(String version, Date date, User author, String comment) {
        this.version = version;
        this.date = (date != null) ? new Date(date.getTime()) : null;
        this.authorUser = author;
        this.userString = author.getId();
        this.comment = comment;
        items = new ArrayList<>();
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
    public Collection<? extends hudson.scm.ChangeLogSet.AffectedFile> getAffectedFiles() {
        return items;
    }

    @Override
    public ChangeLogSet getParent() {
        return (ChangeLogSet)super.getParent();
    }

    @Override
    public User getAuthor() {
        if(authorUser == null) {
            authorUser = User.get(userString);
        }
        return authorUser;
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

    /**
     * Returns a human readable display name of the changeset number.
     *
     * <p>
     * This method is primarily intended for visualization of the data.
     */
    @Exported
    public String getCommitId() {
        return version;
    }

    @Exported
    public String getDomain() {
        return domain;
    }

    @Exported
    public String getUser() {
        return userString;
    }
    
    public void setUser(String user) {        
        String[] split = user.split("\\\\");
        if (split.length == 2) {
            this.domain = split[0];
            this.userString = split[1];
        } else {
            this.userString = user;
            this.domain = null;
        }
    }

    @Exported
    public Date getDate() {
        if (date != null) {
            return new Date(date.getTime());
        }
        return null;
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

    public void setCheckedInBy(String checkedInByUserString) {
        if (checkedInByUserString != null) {
            String[] split = checkedInByUserString.split("\\\\");
            if (split.length == 2) {
                this.checkedInByUserString = split[1];
            } else {
                this.checkedInByUserString = checkedInByUserString;
            }
        }
    }

    public String getCheckedInBy() {
        return checkedInByUserString;
    }

    public User getCheckedInByUser() {
        if (checkedInByUser == null) {
           checkedInByUser = User.get(checkedInByUserString);  
        }
        return checkedInByUser;
    }

    public void setCheckedInByUser(User checkedInBy) {
        this.checkedInByUser = checkedInBy;
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
    public static class Item implements hudson.scm.ChangeLogSet.AffectedFile {
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
