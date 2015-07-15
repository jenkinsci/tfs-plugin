package hudson.util;

/**
 * Placed in the same package as {@link hudson.util.Secret} to be able to reach
 * its package-protected {@link hudson.util.Secret#SECRET} field, which allows
 * testing of encryption-using code without needing to launch all of Jenkins.
 */
public class SecretOverride {
    public static void set(final String secretKey) {
        Secret.SECRET = secretKey;
    }
}
