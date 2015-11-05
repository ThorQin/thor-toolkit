package com.github.thorqin;

import com.github.thorqin.toolkit.annotation.Service;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by thor on 11/5/15.
 */
@Service("myService")
public class MyService {

    public String getServerTime() {
        return new DateTime().toString(DateTimeFormat.mediumDateTime());
    }
}
