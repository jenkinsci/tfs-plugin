package hudson.plugins.tfs.actions;

import org.apache.commons.lang.StringUtils;

public class CheckoutStrategyParser {

	private static final int SECOND_CHAR_INDEX = 1;
	private final String versionspecAndArg;
	private String versionspec;
	private String value;
	
	
	public CheckoutStrategyParser(String versionspecAndArg) {
		this.versionspecAndArg = versionspecAndArg;
		parseVersionspecAndArgs();
	}

	
	private void parseVersionspecAndArgs() {
		String firstChar = null;
		if (StringUtils.isNotEmpty(versionspecAndArg)) {
			firstChar = String.valueOf(versionspecAndArg.charAt(0));
			
			if (!isSupportedVersionspec(firstChar) ) {
				throw new CheckoutStrategyException("There's no support for this checkout strategy: " + versionspec);
			} 
			
			this.value = getVersionspecArg();
			this.versionspec = firstChar;
		}
	}


	private boolean isSupportedVersionspec(String firstChar) {
		return Versionspec.L.name().equals(firstChar) || Versionspec.D.name().equals(firstChar);
	}


	private String getVersionspecArg() {
		return versionspecAndArg.substring(SECOND_CHAR_INDEX, versionspecAndArg.length());
	}


	public boolean hasCheckoutParameter() {
		return StringUtils.isNotEmpty(versionspec) && StringUtils.isNotEmpty(value);
	}

	
	public Versionspec getVersionspec() {
		if (hasCheckoutParameter()) {
			return Versionspec.valueOf(this.versionspec);
		}
		
		return Versionspec.D;
	}

	
	public String getValue() {
		return this.value;
	}

}
