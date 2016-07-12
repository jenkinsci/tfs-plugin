package jenkins.security;

import hudson.Util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class ConfidentialStoreOverride implements Closeable {

    private static final File TEMP_FOLDER;
    private static final ConfidentialStore TEST_CONFIDENTIAL_STORE;
    private static final ThreadLocal<ConfidentialStore> TEST_THREAD_LOCAL;
    private static final Random TEST_RANDOM_SOURCE = new Random(4 /* chosen by fair dice roll */);

    static {
        try {
            TEMP_FOLDER = new File(Util.createTempDir(), "jenkins");
            TEST_CONFIDENTIAL_STORE = new DefaultConfidentialStore(TEMP_FOLDER) {
                public byte[] randomBytes(final int size) {
                    byte[] random = new byte[size];
                    TEST_RANDOM_SOURCE.nextBytes(random);
                    return random;
                }
            };
            TEST_THREAD_LOCAL = new ThreadLocal<ConfidentialStore>(){
                protected ConfidentialStore initialValue() {
                    return TEST_CONFIDENTIAL_STORE;
                }
            };
        }
        catch (final IOException e) {
            throw new Error(e);
        }
        catch (final InterruptedException e) {
            throw new Error(e);
        }
    }

    public static void set(final ThreadLocal<ConfidentialStore> override) {
        ConfidentialStore.TEST = override;
    }

    public ConfidentialStoreOverride() {
        this(TEST_THREAD_LOCAL);
    }

    public ConfidentialStoreOverride(final ThreadLocal<ConfidentialStore> override) {
        set(override);
    }

    public void close() throws IOException {
        set(null);
    }
}
