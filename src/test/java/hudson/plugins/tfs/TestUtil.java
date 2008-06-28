package hudson.plugins.tfs;

import hudson.FilePath;
import hudson.Util;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.webdav.lib.properties.GetContentLengthProperty;

public class TestUtil {


    public static Calendar getCalendar(int year, int month, int day) {
        return getCalendar(year, month, day, 0, 0, 0);
    }
    
    public static Calendar getCalendar(int year, int month, int day, int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        //calendar.set(year, month, day, hour, min, sec);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, sec);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        return calendar;
    }
    
    public static FilePath createTempFilePath() throws Exception {
        File parentFile = Util.createTempDir();
        FilePath workspace = new FilePath(parentFile);
        parentFile.delete();
        workspace.mkdirs();
        return workspace;
    }
}
