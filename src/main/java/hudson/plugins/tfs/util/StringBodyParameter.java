package hudson.plugins.tfs.util;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.AnnotationHandler;
import org.kohsuke.stapler.InjectedParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link InjectedParameter} annotation to use on
 * {@link org.kohsuke.stapler.WebMethod} parameters to extract the body of the request
 * as a single string.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
@InjectedParameter(StringBodyParameter.StringBodyHandler.class)
public @interface StringBodyParameter {
    class StringBodyHandler extends AnnotationHandler<StringBodyParameter> {

        private static final Logger LOGGER = Logger.getLogger(StringBodyHandler.class.getName());


        @Override
        public Object parse(final StaplerRequest request, final StringBodyParameter a, final Class type, final String parameterName) throws ServletException {

            final String contentType = request.getContentType();

            if (MediaType.APPLICATION_JSON.equals(contentType)) {
                try {
                    return IOUtils.toString(request.getInputStream(), Charsets.UTF_8);
                }
                catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "Unable to obtain request body: {}", e.getMessage());
                }
            }
            else if (MediaType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
                return request.getParameter(parameterName);
            }

            return null;
        }

    }
}
