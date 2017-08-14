package hudson.plugins.tfs.model;

import hudson.ProxyConfiguration;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A {@link Serializable} adapter between {@link ProxyConfiguration} and {@link ProxyHostEx}.
 */
public class WebProxySettings implements Serializable {
    private static final long serialVersionUID = 401L;

    private final String hostName;
    private final int port;
    private final String proxyUser;
    private final Secret proxySecret;
    private final List<Pattern> noProxyHostPatterns;

    @SuppressWarnings("unused" /* Needed by Serializable interface */)
    private WebProxySettings() {
        this(null, -1, null, null, null);
    }

    /**
     * Convenience constructor, mostly for tests.
     *
     * @param hostName            the name (or address) of the proxy server.
     *                            May be {@code null}, meaning there is no proxy server configured.
     * @param port                the port that the proxy server is listening on
     * @param noProxyHostPatterns a list of {@link Pattern} representing hosts that should not be proxied.
     *                            May be {@code null}, meaning all hosts will be proxied.
     * @param proxyUser           the name of the user with which to authenticate to the proxy server.
     *                            May be {@code null}, meaning the proxy server doesn't need authentication.
     * @param proxySecret         the password of the user with which to authenticate to the proxy server.
     *                            May be {@code null}, meaning the proxy server doesn't need authentication.
     */
    public WebProxySettings(final String hostName, final int port, final List<Pattern> noProxyHostPatterns, final String proxyUser, final Secret proxySecret) {
        this.hostName = hostName;
        this.port = port;
        this.noProxyHostPatterns = copyNoProxyHostPatterns(noProxyHostPatterns);
        this.proxyUser = proxyUser;
        this.proxySecret = proxySecret;
    }

    /**
     * Initialize a {@link WebProxySettings} from a Jenkins {@link ProxyConfiguration}.
     *
     * @param proxyConfiguration the proxy settings as obtained from Jenkins.
     *                           May be {@code null}, meaning there is no proxy configured.
     */
    public WebProxySettings(final ProxyConfiguration proxyConfiguration) {
        if (proxyConfiguration != null) {
            this.hostName = proxyConfiguration.name;
            this.port = proxyConfiguration.port;
            this.proxyUser = proxyConfiguration.getUserName();
            this.noProxyHostPatterns = copyNoProxyHostPatterns(proxyConfiguration.getNoProxyHostPatterns());
            this.proxySecret = Secret.fromString(proxyConfiguration.getEncryptedPassword());
        } else {
            this.hostName = null;
            this.port = -1;
            this.proxyUser = null;
            this.proxySecret = null;
            this.noProxyHostPatterns = copyNoProxyHostPatterns(null);
        }
    }

    private static ArrayList<Pattern> copyNoProxyHostPatterns(final List<Pattern> noProxyHostPatterns) {
        return new ArrayList<Pattern>(
                noProxyHostPatterns == null
                        ? Collections.<Pattern>emptyList()
                        : noProxyHostPatterns
        );
    }

    /**
     * Initialize a {@link ProxyHostEx} from this {@link WebProxySettings} for the provided hostToProxy.
     * May return null, which either means there is no proxy server configured or it does not apply
     * to the provided hostToProxy.
     *
     * @param hostToProxy the name of the host for which proxying is considered.
     * @return an instance of {@link ProxyHostEx} or {@code null} if no proxy is to be used.
     */
    public ProxyHostEx toProxyHost(final String hostToProxy) {
        final ProxyHostEx proxyHost;
        if (this.hostName != null) {
            final boolean shouldProxy = shouldProxy(hostToProxy, noProxyHostPatterns);
            if (shouldProxy) {
                proxyHost = new ProxyHostEx(hostName, port, proxyUser, proxySecret);
            } else {
                proxyHost = null;
            }
        } else {
            proxyHost = null;
        }
        return proxyHost;
    }

    /**
     * Convert the host to the proxy information.
     */
    public Proxy toProxy(final String hostToProxy) {
        final Proxy proxy;
        if (this.hostName != null) {
            final boolean shouldProxy = shouldProxy(hostToProxy, noProxyHostPatterns);
            if (shouldProxy) {
                final InetSocketAddress proxyAddress = new InetSocketAddress(hostName, port);
                proxy = new Proxy(Proxy.Type.HTTP, proxyAddress);
                if (proxyUser != null && proxySecret != null) {
                    final Authenticator authenticator = new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            if (StringUtils.equalsIgnoreCase(hostName, this.getRequestingHost())) {
                                return new PasswordAuthentication(proxyUser, proxySecret.getPlainText().toCharArray());
                            }
                            return null;
                        }
                    };
                    Authenticator.setDefault(authenticator);
                }
            } else {
                proxy = Proxy.NO_PROXY;
            }
        } else {
            proxy = Proxy.NO_PROXY;
        }
        return proxy;
    }

    static boolean shouldProxy(final String host, final List<Pattern> noProxyHostPatterns) {
        // inspired by https://github.com/jenkinsci/git-client-plugin/commit/2fefeae06db79d09d6604994001f8f2bd21549e1
        boolean shouldProxy = true;
        for (final Pattern p : noProxyHostPatterns) {
            if (p.matcher(host).matches()) {
                shouldProxy = false;
                break;
            }
        }
        return shouldProxy;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public Secret getProxySecret() {
        return proxySecret;
    }
}
