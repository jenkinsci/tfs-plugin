package hudson.plugins.tfs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that parsers key/values from a reader.
 * This is used when reading text files that contains keys and value pairs where the
 * value can be in one or several rows. First used when doing some more intelligent parsing
 * of the output from the tfs history command.
 * 
 * @author redsolo
 */
public class KeyValueTextReader {

	private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("([\\w\\s]*):(.*)");
	private static final String CONTINUED_VALUE_STRING = " ";

	public Map<String, String> parse(String string) throws IOException {
		return parse(new BufferedReader(new StringReader(string)));
	}

	public Map<String, String> parse(BufferedReader reader) throws IOException {
		HashMap<String,String> map = null;
		String line = reader.readLine();
		String value = null;
		String key = null;
		
		while (line != null) {
			if (line.startsWith(CONTINUED_VALUE_STRING)) {
				value = value + "\n" + line.trim();
			} else {
				if ((value != null) && (key != null)) {
					if (map == null) {
						map = new HashMap<String,String>();
					}
					map.put(key, value.trim());
					key = null;
					value = null;
				}
				Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
				if (matcher.matches()) {
					key = matcher.group(1);
					value = matcher.group(2);
				}
			}
			line = reader.readLine();
		}
		if ((value != null) && (key != null)) {
			if (map == null) {
				map = new HashMap<String,String>();
			}
			map.put(key, value.trim());
		}
		return map;
	}
}
