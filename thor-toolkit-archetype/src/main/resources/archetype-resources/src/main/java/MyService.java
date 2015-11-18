#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.schedule.annotation.ScheduleJob;
import com.github.thorqin.toolkit.utility.Localization;

/**
 * Created by thor on 10/20/15.
 */
@Service("myService")
public class MyService {

    /*
    @Service("logger")
    Logger logger;
    */

    // schedule use cron expression, can specify multiple triggers
    @ScheduleJob(name = "job1", schedule = "0/1 * * ? * * *")
    void doJob() {
        System.out.println("Do in job... " + new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
    }


    public String getServerTime(Localization loc) {
        DateTimeFormatter format = DateTimeFormat.forPattern(
                DateTimeFormat.patternForStyle("LM", loc.getLocale()));
        return new DateTime().toString(format);
    }
}
