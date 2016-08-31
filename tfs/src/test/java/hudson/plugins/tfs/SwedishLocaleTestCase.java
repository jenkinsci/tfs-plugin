package hudson.plugins.tfs;

import java.util.Locale;

import org.junit.After;
import org.junit.Before;

/**
 * Test class that helps setting (and resetting) the locale to swedish so 
 * dates in logs can be parsed properly.
 * 
 * @author Erik Ramfelt
 */
public abstract class SwedishLocaleTestCase {

    Locale defaultLocale;
    
    @Before
    public void setToSwedishLocale() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(new Locale("sv", "SE"));
    }
    
    @After
    public void resetDefaultLocale() {
        Locale.setDefault(defaultLocale);
    }
}
