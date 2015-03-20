package hudson.plugins.tfs.util;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import hudson.plugins.tfs.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class DateUtilTest {

    private static final Calendar fixedPointInTime = Util.getCalendar(2013, 07, 02, 15, 40, 50);

    @Test
    public void toString_fixedPointInTime() throws Exception {
        final String actual = DateUtil.toString(new DateVersionSpec(fixedPointInTime));

        Assert.assertEquals("D2013-07-02T15:40:50Z", actual);
    }

}
