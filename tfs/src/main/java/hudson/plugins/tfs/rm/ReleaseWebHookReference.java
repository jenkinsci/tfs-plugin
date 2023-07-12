package hudson.plugins.tfs.rm;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.logging.Logger;

/**
 *
 * @author Kalyan
 */
public class ReleaseWebHookReference extends AbstractDescribableImpl<ReleaseWebHookReference> {
    private static final Logger logger = Logger.getLogger(ReleaseWebHookReference.class.getName());

    private final String webHookName;

    @DataBoundConstructor
    public ReleaseWebHookReference(final String webHookName) {
        this.webHookName = webHookName;
    }

    public String getWebHookName() {
        return this.webHookName;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ReleaseWebHookReference> {

        @Override
        public String getDisplayName() {
            return "Release Webhook";
        }

        /**
         * Fills the webHook name input.
         * @return
         */
        public ListBoxModel doFillWebHookNameItems() {
            StandardListBoxModel listBoxModel = new StandardListBoxModel();

            for (ReleaseWebHook webHook : ReleaseWebHookHelper.getReleaseWebHookConfigurations()) {
                listBoxModel.add(webHook.getWebHookName());
            }

            return listBoxModel;
        }
    }
}
