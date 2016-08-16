package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.config.persistence.PersistenceStoreProvider;
import com.microsoft.tfs.core.persistence.FilesystemPersistenceStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ClonePersistenceStoreProvider implements PersistenceStoreProvider {

    private final FilesystemPersistenceStore cacheStore;
    private final FilesystemPersistenceStore configurationStore;
    private final FilesystemPersistenceStore logStore;
    private final String nodeName;

    public ClonePersistenceStoreProvider(final PersistenceStoreProvider sourcePersistenceStoreProvider, final String nodeName) {

        this.nodeName = nodeName;
        final FilesystemPersistenceStore sourceCache = sourcePersistenceStoreProvider.getCachePersistenceStore();
        final File cacheFolder = createAndCopy(sourceCache, nodeName);
        this.cacheStore = new FilesystemPersistenceStore(cacheFolder);

        final FilesystemPersistenceStore sourceConfiguration = sourcePersistenceStoreProvider.getConfigurationPersistenceStore();
        final File configurationFolder = createAndCopy(sourceConfiguration, nodeName);
        this.configurationStore = new FilesystemPersistenceStore(configurationFolder);

        final FilesystemPersistenceStore sourceLog = sourcePersistenceStoreProvider.getLogPersistenceStore();
        final File logFolder = createAndCopy(sourceLog, nodeName);
        this.logStore = new FilesystemPersistenceStore(logFolder);
    }

    static File createAndCopy(final FilesystemPersistenceStore sourceStore, final String nodeName) {
        final File sourceBase = sourceStore.getStoreFile();
        final String childName = sourceBase.getName();
        final File sourceParent = sourceBase.getParentFile();
        final File destinationBase = new File(sourceParent, nodeName);
        final File destination = new File(destinationBase, childName);
        if (!destination.isDirectory()) {
            try {
                FileUtils.copyDirectory(sourceBase, destination);
            }
            catch (final IOException e) {
                throw new Error(e);
            }
        }
        return destination;
    }

    public FilesystemPersistenceStore getCachePersistenceStore() {
        return cacheStore;
    }

    public FilesystemPersistenceStore getConfigurationPersistenceStore() {
        return configurationStore;
    }

    public FilesystemPersistenceStore getLogPersistenceStore() {
        return logStore;
    }

    public String getNodeName() {
        return nodeName;
    }
}
