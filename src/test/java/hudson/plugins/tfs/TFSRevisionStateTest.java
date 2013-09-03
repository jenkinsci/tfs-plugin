package hudson.plugins.tfs;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.ModelBuilder;
import org.kohsuke.stapler.export.Property;

public class TFSRevisionStateTest {

    @Test
    public void exportedProperties() {
        ModelBuilder mb = new ModelBuilder();
        Model<TFSRevisionState> m = mb.get(TFSRevisionState.class);

        List<Property> actual = m.getProperties();

        List<String> actualPropertyNames = new ArrayList<String>();
        for (Property p : actual) {
            actualPropertyNames.add(p.name);
        }
        assertCollectionContains(actualPropertyNames, "changesetVersion", "projectPath");
    }
    
    private static final String NewLine = System.getProperty("line.separator");
    
    private static <T> void assertCollectionContains(Iterable<T> actual, T... expected)
    {
        ArrayList<T> expectedItems = new ArrayList<T>(expected.length);
        for (T t : expected) {
            expectedItems.add(t);
        }
        
        ArrayList<T> extraItems = new ArrayList<T>();
        for (T a : actual) {
            if (expectedItems.contains(a)) {
                expectedItems.remove(a);
            }
            else {
                extraItems.add(a);
            }
        }
        StringBuilder sb = null;
        if (expectedItems.size() > 0) {
            sb = new StringBuilder();
            sb.append("Expected the following item");
            if (expectedItems.size() != 1) {
                sb.append('s');
            }
            sb.append(':').append(NewLine);
            for (T t : expectedItems) {
                sb.append('<').append(t).append('>').append(NewLine);
            }
        }
        if (extraItems.size() > 0) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            sb.append("Did not expect the following item");
            if (extraItems.size() != 1) {
                sb.append('s');
            }
            sb.append(':').append(NewLine);
            for (T t : extraItems) {
                sb.append('<').append(t).append('>').append(NewLine);
            }
        }
        if (sb != null){
            fail(sb.toString());
        }
    }
}
