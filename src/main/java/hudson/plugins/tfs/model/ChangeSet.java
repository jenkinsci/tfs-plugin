package hudson.plugins.tfs.model;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.Util;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.EditType;

@ExportedBean(defaultVisibility=999)
public class ChangeSet extends ChangeLogSet.Entry {

    private String version;
    private String user;
    private String domain;
    private Date date;
    private String comment;
    private final SimpleDateFormat TFS_DATETIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    private List<Item> items;

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
    public Object getDomain() {
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
        }
    }

    @Exported
    public Date getDate() {
        return date;
    }
    
    public void setDateStr(String dateStr) throws ParseException {
        date = TFS_DATETIME_FORMATTER.parse(dateStr);
    }

    @Exported
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Exported
    public List<Item> getItems() {
        return items;
    }
    
    public void add(ChangeSet.Item item) {
        items.add(item);
    }

    @Override
    protected void setParent(ChangeLogSet parent) {
        super.setParent(parent);
    }
    
    @ExportedBean(defaultVisibility=999)
    public static class Item {
        private String path;
        private String action;

        public Item() {
            this("","");
        }
        
        public Item(String path, String action) {
            this.path = path;
            this.action = action;
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
