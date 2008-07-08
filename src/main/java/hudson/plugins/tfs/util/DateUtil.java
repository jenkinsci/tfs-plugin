package hudson.plugins.tfs.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

    private DateUtil() {        
    }
    
    @SuppressWarnings("deprecation")
    public static Date parseDate(String dateString) throws ParseException {
        Date date = null;
        try {
            // Use the deprecated Date.parse method as this is very good at detecting
            // dates commonly output by the US and UK standard locales of dotnet that
            // are output by the Microsoft command line client.
            date = new Date(Date.parse(dateString));
        } catch (IllegalArgumentException e) {
            // ignore - parse failed.
        }
        if (date == null) {
            // The old fashioned way did not work. Let's try it using a more
            // complex alternative.
            DateFormat[] formats = createDateFormatsForLocaleAndTimeZone(null, null);
            return parseWithFormats(dateString, formats);
        }
        return date;
    }

    static Date parseWithFormats(String input, DateFormat[] formats) throws ParseException {
        ParseException parseException = null;
        for (int i = 0; i < formats.length; i++) {
            try {
                return formats[i].parse(input);
            } catch (ParseException ex) {
                parseException = ex;
            }
        }

        throw parseException;
    }

    /**
     * Build an array of DateFormats that are commonly used for this locale
     * and timezone.
     */
    static DateFormat[] createDateFormatsForLocaleAndTimeZone(Locale locale, TimeZone timeZone) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        List<DateFormat> formats = new ArrayList<DateFormat>();

        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            for (int timeStyle = DateFormat.FULL; timeStyle <= DateFormat.SHORT; timeStyle++) {
                DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                if (timeZone != null) {
                    df.setTimeZone(timeZone);
                }
                formats.add(df);
            }
        }

        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            DateFormat df = DateFormat.getDateInstance(dateStyle, locale);
            df.setTimeZone(timeZone);
            formats.add(df);
        }

        return formats.toArray(new DateFormat[formats.size()]);
    }
}
