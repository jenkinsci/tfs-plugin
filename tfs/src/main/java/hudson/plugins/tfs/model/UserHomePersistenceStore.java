package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.persistence.FilesystemPersistenceStore;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Inspired by http://stackoverflow.com/a/20885974
 */
public class UserHomePersistenceStore extends FilesystemPersistenceStore {
    public UserHomePersistenceStore(final File subDirectory) {
        // TODO: improve with LocalAppData on Windows and a folder starting with '.' on the rest
        super(new File(System.getProperty("user.home"), subDirectory.getPath()));
    }

    @Override
    public OutputStream getItemOutputStream(final String itemName) throws IOException {
        final File itemFile = this.getItemFile(itemName);
        final File folder = itemFile.getParentFile();
        if (!folder.exists()) {
            final boolean fullyCreated = folder.mkdirs();
            if (!fullyCreated) {
                throw new IOException("Unable to create folder structure for " + folder.getAbsolutePath());
            }
        }
        return super.getItemOutputStream(itemName);
    }
}
