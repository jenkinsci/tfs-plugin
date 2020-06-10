package hudson.plugins.tfs.rm;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import java.net.URI;
import java.net.URISyntaxException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;

/**
 *
 * @author Kalyan
 */
public class ReleaseWebHook extends AbstractDescribableImpl<ReleaseWebHook> {
    private static final Logger logger = Logger.getLogger(ReleaseWebHook.class.getName());

    private final String webHookName;
    private final String payloadUrl;
    private final String secret;

    @DataBoundConstructor
    public ReleaseWebHook(final String webHookName, final String payloadUrl, final String secret) {
        this.webHookName = webHookName;
        this.payloadUrl = payloadUrl;
        this.secret = secret;
    }

    public ReleaseWebHook(final String webHookName, final String payloadUrl) {
        this.webHookName = webHookName;
        this.payloadUrl = payloadUrl;
        this.secret = StringUtils.EMPTY;
    }

    public String getWebHookName() {
        return this.webHookName;
    }

    public String getPayloadUrl() {
        return this.payloadUrl;
    }

    public String getSecret() {
        return this.secret;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * DescriptorImpl.
     */
    @Extension
    public static class DescriptorImpl extends Descriptor<ReleaseWebHook> {

        @Override
        public String getDisplayName() {
            return "Release Webhook";
        }

        /**
         * Validates Payload URL.
         * @param value
         * @return
         */
        @SuppressWarnings("unused")
        public FormValidation doCheckPayloadUrl(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.warning("Please provide a value");
            }

            final URI uri;
            try {
                uri = new URI(value);
            } catch (final URISyntaxException e) {
                return FormValidation.error("Malformed Payload URL (%s)", e.getMessage());
            }

            final String hostName = uri.getHost();
            if (StringUtils.isBlank(hostName)) {
                return FormValidation.error("Please provide a host name");
            }

            return FormValidation.ok();
        }

        /**
         * Validates WebHook Name.
         * @param value
         * @return
         */
        @SuppressWarnings("unused")
        public FormValidation doCheckWebHookName(@QueryParameter final String value) {
            if (StringUtils.isBlank(value)) {
                return FormValidation.error("Please provide a value");
            }

            String pattern = "^[A-Za-z0-9_-]+$";
            if (!value.matches(pattern)) {
                return FormValidation.error("Only allowed characters are alphaphetic, numeric, hypen and underscore");
            }

            return FormValidation.ok();
        }
    }
}
