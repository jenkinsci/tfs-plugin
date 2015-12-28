package hudson.plugins.tfs.model;

import java.util.Locale;
import java.util.TimeZone;

import com.microsoft.tfs.core.config.ConnectionInstanceData;
import com.microsoft.tfs.core.config.DefaultConnectionAdvisor;
import com.microsoft.tfs.core.config.httpclient.HTTPClientFactory;

public class ModernConnectionAdvisor extends DefaultConnectionAdvisor {
	private HTTPClientFactory factory = null;

	public ModernConnectionAdvisor(Locale locale, TimeZone timeZone) {
		super(locale, timeZone);
	}

	@Override
	public HTTPClientFactory getHTTPClientFactory(
			ConnectionInstanceData instanceData) {
		if (factory == null) {
			factory = new ModernHTTPClientFactory(instanceData);
		}

		return factory;
	}

}
