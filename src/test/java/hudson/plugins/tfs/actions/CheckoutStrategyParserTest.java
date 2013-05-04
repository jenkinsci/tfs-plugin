package hudson.plugins.tfs.actions;

import junit.framework.Assert;

import org.junit.Test;

public class CheckoutStrategyParserTest {
	
	private static final String EMPTY_VERSIONSPEC = "";
	private static final String NULL_VERSIONSPEC = null;
	private static final String INVALID_VERSIONSPEC = "Xwhatever";
	private static final String LABEL_VERSIONSPEC = "Lwhatever";
	private static final String DATETIME_VERSIONSPEC = "Dwhatever";
	
	
	@Test
	public void hasCheckoutInfoWithEmptyVersionspec() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(EMPTY_VERSIONSPEC);
		
		Assert.assertFalse(checkoutStrategyParser.hasCheckoutParameter());
	}
	
	@Test
	public void reading_versionspec_with_empty_versionspec_returns_versionspec_datetime() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(EMPTY_VERSIONSPEC);
		
		Assert.assertEquals(Versionspec.D, checkoutStrategyParser.getVersionspec());
	}
	
	
	@Test
	public void reading_value_with_empty_label_returns_null() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(EMPTY_VERSIONSPEC);
		
		Assert.assertEquals(null, checkoutStrategyParser.getValue());
	}
	
	
	@Test
	public void reading_versionspec_with_null_versionspec_returns_versionspec_datetime() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(NULL_VERSIONSPEC);
		
		Assert.assertEquals(Versionspec.D, checkoutStrategyParser.getVersionspec());
	}
	

	@Test
	public void reading_value_with_null_versionspec_return_value_null() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(NULL_VERSIONSPEC);
		
		Assert.assertEquals(null, checkoutStrategyParser.getValue());
	}
	
	
	@Test
	public void hasCheckoutInfoWithNullLabel() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(NULL_VERSIONSPEC);
		
		Assert.assertFalse(checkoutStrategyParser.hasCheckoutParameter());
	}
	
	
	@Test(expected=CheckoutStrategyException.class)
	public void passing_an_invalid_versionspecAndArgs_and_reading_versionspec() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(INVALID_VERSIONSPEC);
		
		checkoutStrategyParser.getVersionspec();
	}
	
	
	@Test(expected=CheckoutStrategyException.class)
	public void passing_an_invalid_versionspecAndArgs_and_reading_value() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(INVALID_VERSIONSPEC);
		
		checkoutStrategyParser.getValue();
	}
	
	
	@Test
	public void passing_a_valid_versionspecAndArgs_and_reading_versionspec() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(LABEL_VERSIONSPEC);
		
		Assert.assertEquals(Versionspec.L, checkoutStrategyParser.getVersionspec());
	}
	
	
	@Test
	public void passing_a_valid_versionspecAndArgs_and_reading_value() {
		CheckoutStrategyParser checkoutStrategyParser = new CheckoutStrategyParser(LABEL_VERSIONSPEC);
		
		Assert.assertEquals("whatever", checkoutStrategyParser.getValue());
	}

	
	
}
