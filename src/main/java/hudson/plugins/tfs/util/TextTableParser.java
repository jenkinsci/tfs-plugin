package hudson.plugins.tfs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the text table output from the TF tool.
 * The table will always begin with a number of columns that specifies how wide each
 * column is. The text table parser will use the number of columns and their width to
 * be able to parse the lines that comes after the column definition. Some columns
 * are optional, ie they does not contain any data and the line ends before the
 * optional column begins. 
 * <p>
 * An example of how a separator definition may look. The table consists of 3 columns; 
 * column 1 contains a string with at most 5 chars, column 2 contains a string with at most
 * 2 chars and column 3 may contain a string with at most 4 chars. The third column is optional.
 * <pre>
 * ----- -- ----
 * Data1 A  Data
 * Data2 B
 * Data3 C  Data
 * </pre>
 * <code>
 * TextTableParser t = new TextTableParser(reader, 1);
 * t.nextRow() // first row
 * assertEquals("Data1", t.getColumn(0));
 * assertEquals("Data", t.getColumn(2));
 * t.nextRow(); // Second row
 * assertEquals("Data2", t.getColumn(0));
 * assertNull(t.getColumn(2));
 * t.nextRow(); // Third row
 * assertEquals("Data3", t.getColumn(0));
 * assertEquals("Data", t.getColumn(2));
 * </code>

 * @author Erik Ramfelt
 */
public class TextTableParser {

    private static final Pattern SEPARATOR_PATTERN = Pattern.compile("(-+)");
    private final BufferedReader reader;
    private List<ColumnRange> columns;
    
    private String currentLine;
    private final int optionalColumnCount;
    private int lastMandatoryColumnStart;

    public TextTableParser(Reader reader) throws IOException {
        this(reader, 0);
    }
    
    public TextTableParser(Reader reader, int optionalColumnCount) throws IOException {
        this.reader = new BufferedReader( reader );
        this.optionalColumnCount = optionalColumnCount;
        init();
    }

    private void init() throws IOException {
        String line = reader.readLine();
        columns = new ArrayList<ColumnRange>();
        while (line != null) {
        	if (line.startsWith("-")) {
	            Matcher matcher = SEPARATOR_PATTERN.matcher(line);
	            if (matcher.find()) {
	                do  {
	                    columns.add(new ColumnRange(matcher.start(), matcher.end()));
	                } while (matcher.find());
	                break;
	            }
        	}
            line = reader.readLine();
        }
        if (columns.size() > 0){
            lastMandatoryColumnStart = columns.get(columns.size() - 1 - optionalColumnCount).start;
        }
    }

    /**
     * Returns the number of columns
     * @return the number of columns
     */
    public int getColumnCount() throws IOException {
        return columns.size();
    }

    /**
     * Return the value in the specified column
     * @param index the column index
     * @return the value in the specified column; null if there is no value (the column is optional)
     */
    public String getColumn(int index) throws IOException {
        if (currentLine == null) {
            throw new IllegalStateException("There is no active row.");
        }
        
        ColumnRange columnRange = columns.get(index);        
        if (currentLine.length() < columnRange.start) {
            return null;
        }
        
        if (currentLine.length() < columnRange.end) {
            return currentLine.substring(columnRange.start).trim();
        } else {
            return currentLine.substring(columnRange.start, columnRange.end).trim();
        }
    }
    
    /**
     * Move to the next row
     * @return true, if there was a next row; false, if there is no next row.
     * @throws IOException
     */
    public boolean nextRow() throws IOException {
        do {
            currentLine = reader.readLine();
        } while ((currentLine != null) && (currentLine.length() < lastMandatoryColumnStart));
        return (currentLine != null);
    }
    
    private static class ColumnRange {
        private final int start;
        private final int end;
        public ColumnRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
