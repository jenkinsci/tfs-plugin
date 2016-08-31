package hudson.plugins.tfs.model;

import java.io.IOException;

public interface NativeLibraryExtractor {
    void extractFile(String operatingSystem, String architecture, String fileName) throws IOException;
}
