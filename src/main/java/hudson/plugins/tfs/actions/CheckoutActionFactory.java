package hudson.plugins.tfs.actions;

import hudson.model.AbstractBuild;
import hudson.plugins.tfs.commands.EnvironmentStrings;

public abstract class CheckoutActionFactory {
	
	public static CheckoutAction getInstance(@SuppressWarnings("rawtypes") AbstractBuild build, CheckoutInfo checkoutInfo) {
		String info = (String) build.getBuildVariables().get(EnvironmentStrings.CHECKOUT_INFO.getValue());
		CheckoutStrategyParser parser = new CheckoutStrategyParser(info);
		
		CheckoutAction checkout = null;
		if (parser.hasCheckoutParameter()) {
			checkoutInfo.setCheckoutStrategyValue(parser.getValue());
			Versionspec strategy = parser.getVersionspec();
			if (!parser.hasCheckoutParameter() || Versionspec.D.equals(strategy)) {
				checkout = new CheckoutActionByTimestamp(checkoutInfo);
			} else if (Versionspec.L.equals(strategy)) {
				checkout = new CheckoutActionByLabel(checkoutInfo);
			} else {
				throw new IllegalArgumentException("There's no implementation for this checkout strategy: " + strategy);
			}
		} else {
			checkout = new CheckoutActionByTimestamp(checkoutInfo);
		}
		
		return checkout;
	}

}
