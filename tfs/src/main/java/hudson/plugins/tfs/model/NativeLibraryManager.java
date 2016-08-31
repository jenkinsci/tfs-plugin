package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.persistence.FilesystemPersistenceStore;
import com.microsoft.tfs.core.persistence.PersistenceStore;
import com.microsoft.tfs.core.persistence.VersionedVendorFilesystemPersistenceStore;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class NativeLibraryManager implements NativeLibraryExtractor {
    private static final String VENDOR_NAME = "Microsoft";
    private static final String TFS_SDK = "TFS_SDK";
    private static final String VERSION = "14.0.1";
    private static final String nativeFolderPropertyName = "com.microsoft.tfs.jni.native.base-directory";
    private static final String NATIVE = "native";
    private static final Class<NativeLibraryManager> metaClass = NativeLibraryManager.class;
    private static final TreeMap<String, TreeMap<String, List<String>>> NATIVE_LIBRARIES =
            new TreeMap<String, TreeMap<String, List<String>>>();
    static {
        final TreeMap<String, List<String>> aix = new TreeMap<String, List<String>>();
        final List<String> aix_ppc = Arrays.asList(
                "libnative_auth.a",
                "libnative_console.a",
                "libnative_filesystem.a",
                "libnative_misc.a",
                "libnative_synchronization.a"
        );
        aix.put("ppc", aix_ppc);
        NATIVE_LIBRARIES.put("aix", aix);

        final TreeMap<String, List<String>> freebsd = new TreeMap<String, List<String>>();
        final List<String> freebsd_x86 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        freebsd.put("x86", freebsd_x86);
        final List<String> freebsd_x86_64 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        freebsd.put("x86_64", freebsd_x86_64);
        NATIVE_LIBRARIES.put("freebsd", freebsd);

        final TreeMap<String, List<String>> hpux = new TreeMap<String, List<String>>();
        final List<String> hpux_ia64_32 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        hpux.put("ia64_32", hpux_ia64_32);
        final List<String> hpux_PA_RISC = Arrays.asList(
                "libnative_auth.sl",
                "libnative_console.sl",
                "libnative_filesystem.sl",
                "libnative_misc.sl",
                "libnative_synchronization.sl"
        );
        hpux.put("PA_RISC", hpux_PA_RISC);
        NATIVE_LIBRARIES.put("hpux", hpux);

        final TreeMap<String, List<String>> linux = new TreeMap<String, List<String>>();
        final List<String> linux_arm = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        linux.put("arm", linux_arm);
        final List<String> linux_ppc = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        linux.put("ppc", linux_ppc);
        final List<String> linux_x86 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        linux.put("x86", linux_x86);
        final List<String> linux_x86_64 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        linux.put("x86_64", linux_x86_64);
        NATIVE_LIBRARIES.put("linux", linux);

        final TreeMap<String, List<String>> macosx = new TreeMap<String, List<String>>(new Comparator<String>() {
            @SuppressWarnings("ComparatorMethodParameterNotUsed" /* because of null key */)
            public int compare(final String o1, final String o2) {
                return 0;
            }
        });
        final List<String> macosx_universal = Arrays.asList(
                "libnative_auth.jnilib",
                "libnative_console.jnilib",
                "libnative_filesystem.jnilib",
                "libnative_keychain.jnilib",
                "libnative_misc.jnilib",
                "libnative_synchronization.jnilib"
        );
        macosx.put(null, macosx_universal);
        NATIVE_LIBRARIES.put("macosx", macosx);

        final TreeMap<String, List<String>> solaris = new TreeMap<String, List<String>>();
        final List<String> solaris_sparc = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        solaris.put("sparc", solaris_sparc);
        final List<String> solaris_x86 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        solaris.put("x86", solaris_x86);
        final List<String> solaris_x86_64 = Arrays.asList(
                "libnative_auth.so",
                "libnative_console.so",
                "libnative_filesystem.so",
                "libnative_misc.so",
                "libnative_synchronization.so"
        );
        solaris.put("x86_64", solaris_x86_64);
        NATIVE_LIBRARIES.put("solaris", solaris);

        final TreeMap<String, List<String>> win32 = new TreeMap<String, List<String>>();
        final List<String> win32_x86 = Arrays.asList(
                "native_auth.dll",
                "native_console.dll",
                "native_credential.dll",
                "native_filesystem.dll",
                "native_messagewindow.dll",
                "native_misc.dll",
                "native_registry.dll",
                "native_synchronization.dll"
        );
        win32.put("x86", win32_x86);
        final List<String> win32_x86_64 = Arrays.asList(
                "native_auth.dll",
                "native_console.dll",
                "native_credential.dll",
                "native_filesystem.dll",
                "native_messagewindow.dll",
                "native_misc.dll",
                "native_registry.dll",
                "native_synchronization.dll"
        );
        win32.put("x86_64", win32_x86_64);
        NATIVE_LIBRARIES.put("win32", win32);
    }

    private final PersistenceStore store;

    public NativeLibraryManager(final PersistenceStore store) {
        this.store = store;
    }

    public void extractFiles() throws IOException {
        // TODO: it would be great if we detected the current OS and architecture to extract only the needed files
        extractFiles(this);
    }

    static void extractFiles(final NativeLibraryExtractor extractor) throws IOException {
        for (final String operatingSystem : NATIVE_LIBRARIES.keySet()) {
            final TreeMap<String, List<String>> architecturesToFileNames = NATIVE_LIBRARIES.get(operatingSystem);
            for (final String architecture : architecturesToFileNames.keySet()) {
                final List<String> fileNames = architecturesToFileNames.get(architecture);
                for (final String fileName : fileNames) {
                    extractor.extractFile(operatingSystem, architecture, fileName);
                }
            }
        }
    }

    public void extractFile(final String operatingSystem, final String architecture, final String fileName) throws IOException {
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

    // it is important that this return a string containing forward slashes
    static String buildPathToNativeFile(final String operatingSystem, final String architecture, final String fileName) {
        final StringBuilder sb = new StringBuilder(NATIVE.length() + 1 + operatingSystem.length() + 1 + 7 /* max architecture length */ + 1 + fileName.length());
        sb.append(NATIVE).append('/');
        sb.append(operatingSystem).append('/');
        if (architecture != null) {
            sb.append(architecture).append('/');
        }
        sb.append(fileName);
        final String result = sb.toString();
        return result;
    }

    public static synchronized void initialize() throws IOException {
        final String nativeFolder = System.getProperty(nativeFolderPropertyName);
        if (nativeFolder == null) {
            final File vendor = new File(VENDOR_NAME);
            final File vendor_sdk = new File(vendor, TFS_SDK);
            final File vendor_sdk_version = new File(vendor_sdk, VERSION);
            final FilesystemPersistenceStore store = new UserHomePersistenceStore(vendor_sdk_version);
            final NativeLibraryManager manager = new NativeLibraryManager(store);
            manager.extractFiles();

            final File storeFile = store.getStoreFile();
            final File nativeFile = new File(storeFile, NATIVE);
            final String absolutePath = nativeFile.getAbsolutePath();
            System.setProperty(nativeFolderPropertyName, absolutePath);
        }
    }

}
