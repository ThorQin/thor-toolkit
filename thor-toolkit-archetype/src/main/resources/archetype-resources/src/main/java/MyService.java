#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.schedule.annotation.ScheduleJob;

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
        System.out.println("Do in job..." + getServerTime());
    }


    public String getServerTime() {
        return new DateTime().toString(DateTimeFormat.mediumDateTime());
    }
}
