package hudson.plugins.tfs.util;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class that parses date depending on a Locale and Timezone.
 */
public class DateParser {

    private final TimeZone timezone;
    private final Locale locale;

    public DateParser(Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
    }
    
    public DateParser() {
        this.locale = Locale.getDefault();
        this.timezone = TimeZone.getDefault();
    }

    public Date parseDate(String dateString) throws ParseException {
        return DateUtil.parseDate(dateString, locale, timezone);
    }
}
