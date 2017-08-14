package hudson.plugins.tfs.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Helper to get resources.
 */
public final class ResourceHelper {

    private ResourceHelper() { }

    /**
     * Gets the resource file and returns it as a string.
     */
    public static String fetchAsString(final Class<?> referenceClass, final String fileName) {
        final InputStream stream = referenceClass.getResourceAsStream(fileName);
        try {
            return IOUtils.toString(stream, MediaType.UTF_8);
        } catch (final IOException e) {
            throw new Error(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
