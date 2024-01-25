package root.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    public static long getDateDiff(Date dateInit, Date dateEnd, TimeUnit timeUnit) {
        long diffInMillies = dateEnd.getTime() - dateInit.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public static long deltaInMinutes(Date date){
        return getDateDiff(date,new Date(), TimeUnit.MINUTES);
    }

    public static long deltaInSeconds(Date date){
        return getDateDiff(date, new Date(), TimeUnit.SECONDS);
    }

    public static Date transformHours(String p) throws ParseException {
        SimpleDateFormat parser=new SimpleDateFormat("HH:mm");
        return parser.parse(p);
    }
}