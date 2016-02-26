package hudson.plugins.tfs.model;

import java.util.Locale;
import java.util.TimeZone;

import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.DefaultConnectionAdvisor;
import com.microsoft.tfs.core.config.httpclient.HTTPClientFactory;
import com.microsoft.tfs.core.httpclient.ProxyHost;

public class ModernConnectionAdvisor extends DefaultConnectionAdvisor {

    private final ProxyHost proxyHost;

    public ModernConnectionAdvisor(final ProxyHost proxyHost) {
        super(Locale.getDefault(), TimeZone.getDefault());
        this.proxyHost = proxyHost;
    }

    @Override
    public HTTPClientFactory getHTTPClientFactory(final ConnectionInstanceData connectionInstanceData) {
        return new ModernHTTPClientFactory(connectionInstanceData, proxyHost);
    }
}
