package hudson.plugins.tfs.model;

import java.io.IOException;

/**
 * An interface for native library extractors.
 */
public interface NativeLibraryExtractor {

    /**
     * Method to extract files.
     * @param operatingSystem
     * @param architecture
     * @param fileName
     * @throws IOException
     */
    void extractFile(String operatingSystem, String architecture, String fileName) throws IOException;
}
