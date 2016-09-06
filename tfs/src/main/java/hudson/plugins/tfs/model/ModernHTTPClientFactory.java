package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.httpclient.DefaultHTTPClientFactory;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.HostConfiguration;
import com.microsoft.tfs.core.httpclient.HttpClient;
import com.microsoft.tfs.core.httpclient.HttpState;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.httpclient.auth.AuthScope;
import hudson.util.Secret;

public class ModernHTTPClientFactory extends DefaultHTTPClientFactory {

    private final ProxyHostEx proxyHost;

    public ModernHTTPClientFactory(final ConnectionInstanceData connectionInstanceData) {
        this(connectionInstanceData,  null);
    }

    public ModernHTTPClientFactory(final ConnectionInstanceData connectionInstanceData, final ProxyHostEx proxyHost) {
        super(connectionInstanceData);
        this.proxyHost = proxyHost;
    }

    @Override
    protected String getUserAgentExtraString(final HttpClient httpClient, final ConnectionInstanceData connectionInstanceData) {
        // https://stackoverflow.com/a/6773868/
        final Class<? extends ModernHTTPClientFactory> me = this.getClass();
        final Package us = me.getPackage();
        String version = us.getImplementationVersion();
        if (version == null) {
            version = "devtest";
        }
        return "TFS Plugin for Jenkins " + version;
    }

    @Override
    public void configureClientProxy(final HttpClient httpClient, final HostConfiguration hostConfiguration,final HttpState httpState, final ConnectionInstanceData connectionInstanceData) {
        hostConfiguration.setProxyHost(proxyHost);

        if (proxyHost != null) {
            final String proxyUser = proxyHost.getProxyUser();
            final Secret proxySecret = proxyHost.getProxySecret();
            if (proxyUser != null && proxySecret != null) {
                httpState.setProxyCredentials(
                        AuthScope.ANY,
                        new UsernamePasswordCredentials(proxyUser, proxySecret.getPlainText())
                );
            }
            else {
                httpState.setProxyCredentials(
                        AuthScope.ANY,
                        new DefaultNTCredentials()
                );
            }
        }
    }
}
