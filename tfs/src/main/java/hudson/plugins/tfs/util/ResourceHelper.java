package hudson.plugins.tfs.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceHelper {

    public static String fetchAsString(final Class<?> referenceClass, final String fileName) {
        final InputStream stream = referenceClass.getResourceAsStream(fileName);
        try {
            return IOUtils.toString(stream, MediaType.UTF_8);
        }
        catch (final IOException e) {
            throw new Error(e);
        }
        finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
