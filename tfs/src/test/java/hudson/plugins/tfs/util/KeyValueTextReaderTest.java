package hudson.plugins.tfs.util;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;
import org.junit.Test;

public class KeyValueTextReaderTest {

	@Test
	public void assertKeysAreRead() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Key:Data\nOtherKey:  More data");
		assertThat(map.get("Key"), is("Data"));
		assertThat(map.get("OtherKey"), is("More data"));
	}
	
	@Test
	public void assertValueIsTrimmed() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Changeset: 12492");
		assertThat(map.get("Changeset"), is("12492"));
	}
	
	@Test
	public void assertKeyContainsSpace() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Change set: 12492");
		assertThat(map.get("Change set"), is("12492"));
	}
	
	@Test
	public void assertMultilineDataIsRead() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Key:Data\n  Some more information");
		assertThat(map.get("Key"), is("Data\nSome more information"));
	}
	
	@Test
	public void asserOnlyKeyIsReadIfValueContainsColon() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Key:Data 23:23:12");
		assertThat(map.get("Key"), is("Data 23:23:12"));
	}
	
	@Test
	public void assertValueBeginingOnNextRowIsParsedWithoutPrefixedEndline() throws Exception {
		Map<String, String> map = new KeyValueTextReader().parse("Comment:\n  Reviewer: \n  Approver: \n");
		assertThat(map.get("Comment"), is("Reviewer:\nApprover:"));
	}
}
