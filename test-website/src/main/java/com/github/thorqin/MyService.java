package com.github.thorqin;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.utility.Localization;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by thor on 11/5/15.
 */
@Service("myService")
public class MyService {

    public String getServerTime(Localization loc) {
        DateTimeFormatter format = DateTimeFormat.forPattern(DateTimeFormat.patternForStyle("LM", loc.getLocale()));
        return new DateTime().toString(format);
    }
}
