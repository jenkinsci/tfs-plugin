package hudson.plugins.tfs.model;

import hudson.util.Secret;

public class ProxyHostEx extends com.microsoft.tfs.core.httpclient.ProxyHost {

    private final String proxyUser;
    private final Secret proxySecret;

    public ProxyHostEx(final String hostname, final int port, final String proxyUser, final Secret proxySecret) {
        super(hostname, port);
        this.proxyUser = proxyUser;
        this.proxySecret = proxySecret;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public Secret getProxySecret() {
        return proxySecret;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
