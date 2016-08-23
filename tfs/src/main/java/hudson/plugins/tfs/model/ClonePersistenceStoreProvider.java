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
    private final String hostName;

    public ClonePersistenceStoreProvider(final PersistenceStoreProvider sourcePersistenceStoreProvider, final String hostName) {

        this.hostName = hostName;
        final FilesystemPersistenceStore sourceCache = sourcePersistenceStoreProvider.getCachePersistenceStore();
        final File cacheFolder = createAndCopy(sourceCache, hostName);
        this.cacheStore = new FilesystemPersistenceStore(cacheFolder);

        final FilesystemPersistenceStore sourceConfiguration = sourcePersistenceStoreProvider.getConfigurationPersistenceStore();
        final File configurationFolder = createAndCopy(sourceConfiguration, hostName);
        this.configurationStore = new FilesystemPersistenceStore(configurationFolder);

        final FilesystemPersistenceStore sourceLog = sourcePersistenceStoreProvider.getLogPersistenceStore();
        final File logFolder = createAndCopy(sourceLog, hostName);
        this.logStore = new FilesystemPersistenceStore(logFolder);
    }

    static File createAndCopy(final FilesystemPersistenceStore sourceStore, final String nodeName) {
        final File sourceBase = sourceStore.getStoreFile();
        final String childName = sourceBase.getName();
        final File sourceParent = sourceBase.getParentFile();
        final File destinationBase = new File(sourceParent, nodeName);
        final File destination = new File(destinationBase, childName);
        if (!destination.isDirectory() && sourceBase.isDirectory()) {
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

    public String getHostName() {
        return hostName;
    }
}
