/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

/**
 * <p>Operations on the conversion of Data and String</p>
 * <p> format char define:</p>
 * <p> G        era designator          (Text)              AD </p>
 * <p> y        year                    (Number)            1996 </p>
 * <p> M        month in year           (Text & Number)     July & 07 </p>
 * <p> d        day in month            (Number)            10 </p>
 * <p> h        hour in am/pm (1~12)    (Number)            12 </p>
 * <p> H        hour in day (0~23)      (Number)            0  </p>
 * <p> m        minute in hour          (Number)            30 </p>
 * <p> s        second in minute        (Number)            55 </p>
 * <p> S        millisecond             (Number)            978 </p>
 * <p> E        day in week             (Text)              Tuesday </p>
 * <p> D        day in year             (Number)            189 </p>
 * <p> F        day of week in month    (Number)            2 (2nd Wed in July) </p>
 * <p> w        week in year            (Number)            27 </p>
 * <p> W        week in month           (Number)            2  </p>
 * <p> a        am/pm marker            (Text)              PM </p>
 * <p> k        hour in day (1~24)      (Number)            24 </p>
 * <p> K        hour in am/pm (0~11)    (Number)            0  </p>
 * <p> z        time zone               (Text)              Pacific Standard Time </p>
 * <p> '        escape for text         (Delimiter)            </p>
 * <p> ''       single quote            (Literal)           '  </p>
 *
 * @author lehf
 * @since 1.0
 * @version $Id: DateUtil.java
 */

public class DateUtil {
    
    public static Date getEndDate(Date date, int amount){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, amount);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.setLenient(false);
        return c.getTime();
    }
    public static Date getEndDate(Date date){
        return getEndDate(date, 0);
    }

    public static Date getStartDate(Date date, int amount){
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, amount);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.setLenient(false);
        return c.getTime();
    }
    public static Date getStartDate(Date date){
        return getStartDate(date, 0);
    }

    /**
     * <p>get Date from a formated string</p>
     *
     * <pre>
     * getDateFromString("031009")
     * getDateFromString("20031009")
     * </pre>
     *
     * @param strDate  the formated data String
     * @return a date associate with the parameter strDate
     */
    public static Date getDateFromString(String strDate) throws DateException {
        int length = strDate.length();
        if(length == 6) {
            return getDateFromString(strDate, "yyMMdd");
        }
        if(length == 8) {
            return getDateFromString(strDate, "yyyyMMdd");
        }
        if(length == 10) {
            return getDateFromString(strDate, "yyyy-MM-dd");
        }
        return getDateFromString(strDate, "yy-MM-dd");
    }

    /**
     * <p>get Date from a formated string</p>
     *
     * <pre>
     * getDateFromString("031009", "yyMMdd")
     * getDateFromString("20031009", "yyyyMMdd")
     * </pre>
     *
     * @param strDate  the formated data String
     * @param format   the format of the strDate
     * @return a date associate with the parameter strDate
     */
    public static Date getDateFromString(String strDate, String format) throws DateException {
        //String s2 = "19960245"; // yyyyMMdd
        if (strDate == null) {
            return null;
        }
        String strDateTrim = strDate.trim();
        java.text.DateFormat df2 = new java.text.SimpleDateFormat(format);
        df2.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        try {
            Date date2 = df2.parse(strDateTrim);
            return date2;
            // System.out.println(date2);
        } catch (ParseException e) {
            //e.printStackTrace();
        }
        return null;
        /*  try {
              SimpleDateFormat formatter = new SimpleDateFormat (format);
              ParsePosition pos = new ParsePosition(0);
              return formatter.parse(strDate, pos);
          }
          catch(Exception ex) {
              throw new DateException(ex.getMessage());
          }*/
    }

	/**
	* <p>date String from format 1 to format 2</p>
	*
	* <pre>
	* getFormat2FromStd("20030908", "yyyy-MM-dd") = "2003-09-08"
	* </pre>
	*
	* @param date  the data to be convert
	* @return a String associate with the parameter dt
	*/
   public static String getFormat2FromStd(String date, String format) throws DateException {
	   return getFormat2FromFormat1(date, "yyyyMMdd", format);
   }

	/**
     * <p>date String from format 1 to format 2</p>
     *
     * <pre>
     * getFormat2FromFormat1("03-09-08", "yy-MM-dd", "yyyyMMdd") = "20030908"
     * </pre>
     *
     * @param date  the data to be convert
     * @return a String associate with the parameter dt
     */
    public static String getFormat2FromFormat1(String date, String format1, String format2) throws DateException {
        return getStringFromDate(getDateFromString(date, format1), format2);
    }

    /**
     * <p>get String from a Date</p>
     *
     * <pre>
     * getDateFromString(new Date(System.currentTimeMillis()))
     * </pre>
     *
     * @param dt  the data to be convert
     * @return a String associate with the parameter dt
     */
    public static String getStringFromDate(Date dt) throws DateException {
        return getStringFromDate(dt, "yyyyMMdd");
    }

    /**
     * <p>get String from a Date</p>
     *
     * <pre>
     * getDateFromString(new Date(System.currentTimeMillis()), "yyMMdd HH:mm:ss") = "030910 12:23:30"
     * </pre>
     *
     * @param dt  the data to be convert
     * @param format   the format to be returned
     * @return a String associate with the parameter dt
     */
    public static String getStringFromDate(Date dt, String format) throws DateException {
		try {
        	SimpleDateFormat formatter = new SimpleDateFormat (format);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        	return formatter.format(dt);
		}
		catch(Exception ex) {
			throw new DateException(ex.getMessage(), ex);
		}
    }


    /**
     * <p>get the interval days between two date</p>
     *
     * <pre>
     * diffDate(dt1, dt2) = 2 // suppose dt1="030902" & dt2="030831"
     * </pre>
     *
     * @param dt1  the first data
     * @param dt2  the second data
     * @return the interval days between dt1 and dt2
     */
    public static int diffDate(Date dt1, Date dt2) {

        return getDistanceDay(dt1, dt2);
    }

    public static String getDistanceTime(String one, String two, String format) {
        Date oneDate = getDateFromString(one, format);
        Date twoDate = getDateFromString(two, format);
        return getDistanceTime(oneDate, twoDate);
    }

    public static String getDistanceTime( Date one, Date two) {


        DateTime startDate = new DateTime(one);
        DateTime endDate = new DateTime(two);
        if (one.after(two)) {
            startDate = new DateTime(two);
            endDate = new DateTime(one);
        }

        int days = Days.daysBetween(startDate, endDate).getDays();
        int hours = Hours.hoursBetween(startDate, endDate).getHours();
        hours -= days*24;
        int minutes = Minutes.minutesBetween(startDate, endDate).getMinutes();
        minutes = minutes - days*24*60 - hours*60;

        StringBuilder str = new StringBuilder();
        if (days > 0) {
            str.append(days).append('天');
        }
        if (hours > 0) {
            str.append(hours).append("小时");
        }
        if (minutes > 0) {
            str.append(minutes).append("分钟");
        }
        if (days == 0 && hours == 0 && minutes == 0) {
            str.append(0).append("分钟");
        }
        return str.toString();

    }

    /**
     * 计算两日期相隔天数
     * @param d1
     * @param d2
     * @return
     * @throws ParseException
     */
    public static int getDistanceDay(Date d1, Date d2) {
        DateTime startDate = new DateTime(d1);
        DateTime endDate = new DateTime(d2);
        if (d1.after(d2)) {
            startDate = new DateTime(d2);
            endDate = new DateTime(d1);
        }
        int days = Days.daysBetween(startDate, endDate).getDays();
        return days;
    }

    /**
     * <p>get the interval days between two date described by formater "yy-MM-dd"</p>
     *
     * <pre>
     * diffDate("20030902", "20030904") = 2
     * </pre>
     *
     * @param dt1  the first data
     * @param dt2  the second data
     * @return the interval days between dt1 and dt2
     */
    public static int diffDate(String dt1, String dt2) throws DateException {
        return diffDate(dt1, dt2, "yyyyMMdd");
    }

    /**
     * <p>get the interval days between two date described by desired formater</p>
     *
     * <pre>
     * diffDate("03-09-02", "03-09-04", "yy-MM-dd") = 2
     * </pre>
     *
     * @param dt1  the first data
     * @param dt2  the second data
     * @param format the formater of the dt1 and dt2
     * @return the interval days between dt1 and dt2
     */
    public static int diffDate(String dt1, String dt2, String format) throws DateException {
        return diffDate(getDateFromString(dt1, format), getDateFromString(dt2, format));
    }

    /**
     * <p>Adds the specified (signed) amount of day to the given date, based on the calendar's rules</p>
     *
     * <pre>
     * addDaysToDate(dt1, 2) = dt // if dt1="03-09-02" then dt="03-09-04"
     * </pre>
     *
     * @param dt1  the base data
     * @param days days to be added
     * @return the new date
     */
    public static Date addDaysToDate(Date dt1, int days) {
        Calendar cale = Calendar.getInstance();
        cale.setTime(dt1);
        cale.add(Calendar.DATE, days);
        return cale.getTime();
    }

    /**
     * <p>Adds the specified (signed) amount of day to the given date, based on the calendar's rules</p>
     *
     * <pre>
     * addDaysToDate("20030902", 2) = dt // dt="20030904"
     * </pre>
     *
     * @param dt1  the base data
     * @param days days to be added
     * @return the new date
     */
    public static Date addDaysToDate(String dt1, int days) throws DateException {
        return addDaysToDate(dt1, "yyyyMMdd", days);
    }

    /**
     * <p>Adds the specified (signed) amount of day to the given date, based on the calendar's rules</p>
     *
     * <pre>
     * addDaysToDate("03-09-02", "yy-MM-dd", 2) = dt // dt="03-09-04"
     * </pre>
     *
     * @param dt1  the base data
     * @param format the format of the dt1
     * @param days days to be added
     * @return the new date
     */
    public static Date addDaysToDate(String dt1, String format, int days) throws DateException {
        return addDaysToDate(getDateFromString(dt1, format), days);
    }

    /**
     * <p>get the system current date</p>
     *
     * <pre>
     * getCurrentDate() = "20030908"
     * </pre>
     *
     * @return the current date
     */
    public static String getCurrentDate() throws DateException {
        return getCurrentDateTime("yyyyMMdd");
    }

    /**
     * <p>get the system current time</p>
     *
     * <pre>
     * getCurrentTime() = "121208"
     * </pre>
     *
     * @return the current time
     */
    public static String getCurrentTime() throws DateException {
        return getCurrentDateTime("HHmmss");

    }

    /**
     * <p>get the system current date and time</p>
     *
     * <pre>
     * getCurrentTime() = "20030912 121208"
     * </pre>
     *
     * @return the current date and time
     */
    public static String getCurrentDateTime() throws DateException {
        return getCurrentDateTime("yyyyMMdd HHmmss");
    }

    /**
     * <p>get the system current date and time</p>
     *
     * <pre>
     * getCurrentTime("yy-MM-dd HH:mm:ss") = "03-09-12 12:12:08"
     * </pre>
     *
     * @return the current date and time
     */
    public static String getCurrentDateTime(String format) throws DateException {
        return getStringFromDate(new Date(System.currentTimeMillis()), format);
    }
    
    public static String getCurrentDateTime(long currentTimeMillis, String format) throws DateException {
        return getStringFromDate(new Date(currentTimeMillis), format);
    }

  

	public static String getUTCdatetimeAsString(Date date,String format){
	    final SimpleDateFormat sdf = new SimpleDateFormat(format);
	    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	    final String utcTime = sdf.format(date);
	    return utcTime;
	}

	public static Date getUTCdatetimeAsDate(Date date,String format){
	   String utc = getUTCdatetimeAsString(date,format);
	   return DateUtil.getDateFromString(utc, format);
	}

    public static boolean isSameDayOfWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDayOfWeek(cal1, cal2);
    }

    public static boolean isSameDayOfWeek(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.DAY_OF_WEEK) == cal2.get(Calendar.DAY_OF_WEEK));
    }

    public static boolean isSameDayOfMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDayOfMonth(cal1, cal2);
    }

    public static boolean isSameDayOfMonth(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH));
    }

    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
    }

    public static Date add(Date date, int calendarField, int amount) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

}

class DateException extends RuntimeException {

	public DateException() {
	}

	public DateException(String msg, Throwable cause) {
		super(msg, cause);
	}
}