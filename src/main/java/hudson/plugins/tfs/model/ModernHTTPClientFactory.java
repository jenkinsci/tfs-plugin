package hudson.plugins.tfs.model;

import java.net.MalformedURLException;
import java.net.URL;

import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.httpclient.DefaultHTTPClientFactory;
import com.microsoft.tfs.core.config.httpclient.HTTPClientFactory;
import com.microsoft.tfs.core.httpclient.HttpClient;

public class ModernHTTPClientFactory extends DefaultHTTPClientFactory implements
		HTTPClientFactory {

	public ModernHTTPClientFactory(ConnectionInstanceData connectionInstanceData) {
		super(connectionInstanceData);
	}

	@Override
	public HttpClient newHTTPClient() {
		HttpClient client = super.newHTTPClient();

		if (hasProxy()) {
			client.getHostConfiguration().setProxy(getProxyHost(),
					getProxyPort());
		}

		return client;
	}

	private static boolean initialized = false;

	private static String sProxyHost = "";
	private static int iProxyPort = 0;

	private static void init() {
		if (initialized) {
			return;
		}

		if (System.getProperty("http.proxyHost") != null) {
			sProxyHost = System.getProperty("http.proxyHost");
			iProxyPort = Integer.parseInt(System.getProperty("http.proxyPort",
					"80"));
		} else {
			String httpProxy = System.getenv("http_proxy");
			if (httpProxy != null) {
				try {
					URL url = new URL(httpProxy);
					sProxyHost = url.getHost();
					iProxyPort = url.getPort();
				} catch (MalformedURLException e) {
					System.err
							.println("Not use http_proxy environment variable which is invalid: "
									+ e.getMessage());
				}
			}
		}

		initialized = true;
	}

	private static String getProxyHost() {
		init();
		return sProxyHost;
	}

	private static int getProxyPort() {
		init();
		return iProxyPort;
	}

	private static boolean hasProxy() {
		init();
		return !sProxyHost.isEmpty() && iProxyPort > 0;
	}

}
