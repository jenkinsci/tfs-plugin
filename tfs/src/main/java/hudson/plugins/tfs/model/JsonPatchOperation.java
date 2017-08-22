//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.visualstudio.services.webapi.patch.Operation;

/**
 * Workaround for broken version in
 * com.microsoft.visualstudio.services.webapi.patch.json
 * where #getPath() returns this.op
 */
public class JsonPatchOperation {

    private Operation op;
    private String path;
    private Object value;

    public Operation getOp() {
        return op;
    }

    public void setOp(Operation op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
