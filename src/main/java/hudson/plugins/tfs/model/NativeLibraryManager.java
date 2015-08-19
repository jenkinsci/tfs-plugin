package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.persistence.PersistenceStore;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NativeLibraryManager {
    private static final File NATIVE = new File("native");
    private static final Class<NativeLibraryManager> metaClass = NativeLibraryManager.class;

    private final PersistenceStore store;

    public NativeLibraryManager(final PersistenceStore store) {
        this.store = store;
    }

    void extractFile(final String operatingSystem, final String architecture, final String fileName) throws IOException {
        final String pathToNativeFile = buildPathToNativeFile(operatingSystem, architecture, fileName);
        if (!store.containsItem(pathToNativeFile)) {
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = metaClass.getResourceAsStream(pathToNativeFile);
                outputStream = store.getItemOutputStream(pathToNativeFile);
                IOUtils.copy(inputStream, outputStream);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    static String buildPathToNativeFile(String operatingSystem, String architecture, String fileName) {
        final File n_os = new File(NATIVE, operatingSystem);
        final File n_os_arch = new File(n_os, architecture);
        final File n_os_arch_file = new File(n_os_arch, fileName);
        return n_os_arch_file.getPath();
    }

}
