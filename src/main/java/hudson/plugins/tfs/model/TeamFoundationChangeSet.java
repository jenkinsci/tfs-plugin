package hudson.plugins.tfs.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import hudson.model.User;
import hudson.scm.ChangeLogSet;

@ExportedBean(defaultVisibility=999)
public class TeamFoundationChangeSet extends ChangeLogSet.Entry {

    private String version;
    private String user;
    private Date date;
    private String comment;
    
    private List<Item> items;

    public TeamFoundationChangeSet(String version, Date date, String user, String comment) {
        this.version = version;
        this.date = date;
        this.user = user;
        this.comment = comment;
        items = new ArrayList<Item>();
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

    @Exported
    public String getUser() {
        return user;
    }

    @Exported
    public Date getDate() {
        return date;
    }

    @Exported
    public String getComment() {
        return comment;
    }

    @Exported
    public List<Item> getItems() {
        return items;
    }    
    
    @ExportedBean(defaultVisibility=999)
    public static class Item {
        private String path;
        private String action;
        
        public Item(String path, String action) {
            this.path = path;
            this.action = action;
        }

        @Exported
        public String getPath() {
            return path;
        }

        @Exported   
        public String getAction() {
            return action;
        }
    }
}
