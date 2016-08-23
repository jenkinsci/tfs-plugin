package hudson.plugins.tfs;

import hudson.FilePath;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

public class Util {

    private Util() {
    }

    public static Calendar getCalendar(int year, int month, int day) {
        return getCalendar(year, month, day, 0, 0, 0, "GMT");
    }
    
    public static Calendar getCalendar(int year, int month, int day, int hour, int min, int sec) {
        return getCalendar(year, month, day, hour, min, sec, "GMT");
    }

    public static Calendar getCalendar(int year, int month, int day, int hour, int min, int sec, String timezone) {
        return getCalendar(year, month, day, hour, min, sec, TimeZone.getTimeZone(timezone));
    }

    public static Calendar getCalendar(int year, int month, int day, int hour, int min, int sec, TimeZone timezone) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        calendar.setTimeZone(timezone);
        return calendar;
    }
    
    public static FilePath createTempFilePath() throws Exception {
        File parentFile = hudson.Util.createTempDir();
        FilePath workspace = new FilePath(parentFile);
        parentFile.delete();
        workspace.mkdirs();
        return workspace;
    }

    /**
     * Create a boxed copy of the boolean array since JUnit assertArrayEquals() does not take boolean[]
     * @param array copy from
     * @return a boxed copy of the array
     */
    public static Boolean[] toBoxedArray(boolean[] array) {
        Boolean[] copy = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i];
        }
        return copy;
    }
}
