package hudson.plugins.tfs.model;

import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.httpclient.DefaultHTTPClientFactory;
import com.microsoft.tfs.core.httpclient.HostConfiguration;
import com.microsoft.tfs.core.httpclient.HttpClient;
import com.microsoft.tfs.core.httpclient.HttpState;

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
    public void configureClientProxy(final HttpClient httpClient, final HostConfiguration hostConfiguration,final HttpState httpState, final ConnectionInstanceData connectionInstanceData) {
        hostConfiguration.setProxyHost(proxyHost);
    }
}
