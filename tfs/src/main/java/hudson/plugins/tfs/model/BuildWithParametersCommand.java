package hudson.plugins.tfs.model;

import hudson.plugins.tfs.util.MediaType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Used for builds with parameters.
 */
public class BuildWithParametersCommand extends BuildCommand {

    /**
     * Factory for creating BuildWithParametersCommand instances.
     */
    public static class Factory implements AbstractCommand.Factory {

        @Override
        public AbstractCommand create() {
            return new BuildWithParametersCommand();
        }

        @Override
        public String getSampleRequestPayload() {
            final Class<? extends Factory> me = this.getClass();
            final InputStream stream = me.getResourceAsStream("BuildWithParametersCommand.json");
            try {
                return IOUtils.toString(stream, MediaType.UTF_8);
            } catch (final IOException e) {
                throw new Error(e);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }
}
