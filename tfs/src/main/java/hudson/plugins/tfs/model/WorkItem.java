package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.clients.workitem.internal.query.WorkItemRelation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Workaround for missing classes in vso-httpclient-java
 */
public class WorkItem
    extends WorkItemTrackingResource {

    private HashMap<String, Object> fields;
    private int id;
    private ArrayList<WorkItemRelation> relations;
    private int rev;

    public HashMap<String, Object> getFields() {
        return fields;
    }

    public void setFields(final HashMap<String, Object> fields) {
        this.fields = fields;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public ArrayList<WorkItemRelation> getRelations() {
        return relations;
    }

    public void setRelations(final ArrayList<WorkItemRelation> relations) {
        this.relations = relations;
    }

    public int getRev() {
        return rev;
    }

    public void setRev(final int rev) {
        this.rev = rev;
    }
}
