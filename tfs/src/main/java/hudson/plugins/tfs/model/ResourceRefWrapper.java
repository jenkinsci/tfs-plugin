//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.microsoft.visualstudio.services.webapi.model.ResourceRef;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.Serializable;

@ExportedBean(defaultVisibility = 999)
public class ResourceRefWrapper implements Serializable {
    private static final long serialVersionUID = 1L;

    public String id;
    public String url;

    ResourceRefWrapper() {

    }

    ResourceRefWrapper(ResourceRef resourceRef) {
        id = resourceRef.getId();
        url = resourceRef.getUrl();
    }
}
