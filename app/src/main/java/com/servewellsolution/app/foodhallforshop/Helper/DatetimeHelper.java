package com.servewellsolution.app.foodhallforshop.Helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Breeshy on 9/4/2016 AD.
 */

public class DatetimeHelper {
    public static String convertDate(String date) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tzz = cal.getTimeZone();

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(tzz.getID());
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yyyy ' เวลา' HH:mm");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertDate2(String date) {
        try {

            Calendar cal = Calendar.getInstance();
            TimeZone tzz = cal.getTimeZone();

            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sourceFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date parsed = sourceFormat.parse(date); // => Date is in UTC now

            TimeZone tz = TimeZone.getTimeZone(tzz.getID());
            SimpleDateFormat destFormat = new SimpleDateFormat("dd MMM yyyy");
            destFormat.setTimeZone(tz);

            String result = destFormat.format(parsed);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
