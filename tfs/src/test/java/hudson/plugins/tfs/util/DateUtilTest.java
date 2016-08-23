package hudson.plugins.tfs.util;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import hudson.plugins.tfs.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DateUtilTest {

    private static final Calendar fixedPointInTime = Util.getCalendar(2013, 07, 02, 15, 40, 50);
    private static final DateVersionSpec dateVersionSpec = new DateVersionSpec(fixedPointInTime);

    @Test
    public void toString_date() throws Exception {
        final String actual = DateUtil.toString(new Date(1372779650000L));

        Assert.assertEquals("D2013-07-02T15:40:50Z", actual);
    }

    @Test
    public void toString_calendar() throws Exception {
        final String actual = DateUtil.toString(fixedPointInTime);

        Assert.assertEquals("D2013-07-02T15:40:50Z", actual);
    }

    @Test
    public void toString_dateVersionSpec() throws Exception {
        final String actual = DateUtil.toString(dateVersionSpec);

        Assert.assertEquals("D2013-07-02T15:40:50Z", actual);
    }

}
