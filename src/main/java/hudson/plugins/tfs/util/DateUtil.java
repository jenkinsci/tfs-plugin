package hudson.plugins.tfs.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class DateUtil {

    private DateUtil() {        
    }

    public static final ThreadLocal<SimpleDateFormat> TFS_DATETIME_FORMATTER = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(new SimpleTimeZone(0,"GMT"));
            return dateFormat;
        }
    };
    
    public static Date parseDate(String dateString) throws ParseException {
        return parseDate(dateString, Locale.getDefault(), TimeZone.getDefault());
    }
    
    @SuppressWarnings("deprecation")
    public static Date parseDate(String dateString, Locale locale, TimeZone timezone) throws ParseException {
        Date date = null;
        dateString = dateString.replaceAll("(p|P)\\.(m|M)\\.", "PM").replaceAll("(a|A)\\.(m|M)\\.", "AM");
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
            DateFormat[] formats = createDateFormatsForLocaleAndTimeZone(locale, timezone);
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
        if (parseException == null) {
            throw new IllegalStateException("No dateformats found that can be used for parsing '" + input + "'");
        }
        throw parseException;
    }

    /**
     * Build an array of DateFormats that are commonly used for this locale
     * and timezone.
     */
    static DateFormat[] createDateFormatsForLocaleAndTimeZone(Locale locale, TimeZone timeZone) {
        List<DateFormat> formats = new ArrayList<DateFormat>();

        addDateTimeFormatsToList(locale, timeZone, formats);
        addDateFormatsToList(locale, timeZone, formats);

        return formats.toArray(new DateFormat[formats.size()]);
    }

    static void addDateFormatsToList(Locale locale, TimeZone timeZone, List<DateFormat> formats) {
        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            DateFormat df = DateFormat.getDateInstance(dateStyle, locale);
            df.setTimeZone(timeZone);
            formats.add(df);
        }
    }

    static void addDateTimeFormatsToList(Locale locale, TimeZone timeZone, List<DateFormat> formats) {
        for (int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++) {
            for (int timeStyle = DateFormat.FULL; timeStyle <= DateFormat.SHORT; timeStyle++) {
                DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
                if (timeZone != null) {
                    df.setTimeZone(timeZone);
                }
                formats.add(df);
            }
        }
    }
}
