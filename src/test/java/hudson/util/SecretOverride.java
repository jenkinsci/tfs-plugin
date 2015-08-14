package hudson.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Placed in the same package as {@link hudson.util.Secret} to be able to reach
 * its package-protected {@link hudson.util.Secret#SECRET} field, which allows
 * testing of encryption-using code without needing to launch all of Jenkins.
 */
public class SecretOverride implements Closeable {

    public static void set(final String secretKey) {
        Secret.SECRET = secretKey;
    }

    public SecretOverride() {
        this("5e2422dc868f119d5033f4619a6f223d71d132a17f8a63f1056c9a1f57c65006");
    }

    public SecretOverride(final String secretKey) {
        set(secretKey);
    }

    public void close() throws IOException {
        set(null);
    }
}
