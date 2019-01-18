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

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import junit.framework.TestCase;

public class DateUtilTest extends TestCase {

    @Test
    public void testAll() {
		  String ddd = "2012-07-05 04:50:00";
		  Date d  = DateUtil.getDateFromString(ddd, "yyyy-MM-dd HH:mm:ss");
		  String format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		  assertEquals(DateUtil.getStringFromDate(d, format), "2012-07-05T04:50:00.000Z");
		
		  String utc = DateUtil.getUTCdatetimeAsString(d,"yyyy-MM-dd HH:mm:ss");
		  assertEquals(utc, "2012-07-04 20:50:00");
		  assertThat(DateUtil.getStringFromDate(d, "EE MM dd HH:mm:ss yyyy")).isIn("星期四 07 05 04:50:00 2012","Thu 07 05 04:50:00 2012");
		  Calendar cale1 = Calendar.getInstance();
		  Calendar cale2 = Calendar.getInstance();
		  cale1.set(2003, 8, 25, 0, 30, 30);
		  cale2.set(2003, 7, 30, 02, 30, 30);
		  assertEquals(DateUtil.getStringFromDate(cale2.getTime(), "yy-M-d HH:mm:ss"), "03-8-30 02:30:30");
		  assertEquals(DateUtil.diffDate(cale1.getTime(), cale2.getTime()), 25);
		  assertEquals(DateUtil.diffDate(DateUtil.addDaysToDate(cale1.getTime(), -25), cale2.getTime()), 0);
		
		
		  assertEquals(DateUtil.diffDate("03-0825", "03-0925", "yy-MMdd"), 31);
		  Date today = new Date();
		  String todayStr = DateUtil.getStringFromDate(today, "yyyy/MM/dd");
		  assertEquals(DateUtil.getFormat2FromFormat1(DateUtil.getCurrentDate(), "yyyyMMdd", "yyyy/MM/dd HHmmss"), todayStr + " 000000");
		
		
		  assertEquals(DateUtil.getDistanceTime("2015-07-24 16:02", "2015-07-23 16:01", "yyyy-MM-dd HH:mm"), "1天1分钟");
		  assertEquals(DateUtil.diffDate("2015-07-24 16:02", "2015-07-23 16:01", "yyyy-MM-dd HH:mm"), 1);
    }

    @Test
	public void testGetStartEndDate() throws ParseException {
		SimpleDateFormat  df  =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date d = df.parse("2017-03-31 01:02:03");
		Date result = DateUtil.getEndDate(d);
		Calendar c = Calendar.getInstance();
		c.setTime(result);

		assertEquals(2, c.get(Calendar.MONTH));
		assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(59, c.get(Calendar.MINUTE));
		assertEquals(59, c.get(Calendar.SECOND));

		result = DateUtil.getStartDate(d);
		c.setTime(result);

		assertEquals(2, c.get(Calendar.MONTH));
		assertEquals(31, c.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, c.get(Calendar.MINUTE));
		assertEquals(0, c.get(Calendar.SECOND));

	}

	@Test
	public void testGetDateFromString() {
		Calendar c = Calendar.getInstance();
    	Date date = DateUtil.getDateFromString("170712");
    	c.setTime(date);
    	assertEquals(2017, c.get(Calendar.YEAR));
    	assertEquals(6, c.get(Calendar.MONTH));
    	assertEquals(12, c.get(Calendar.DAY_OF_MONTH));

		date = DateUtil.getDateFromString("20160314");
		c.setTime(date);
		assertEquals(2016, c.get(Calendar.YEAR));
		assertEquals(2, c.get(Calendar.MONTH));
		assertEquals(14, c.get(Calendar.DAY_OF_MONTH));

		date = DateUtil.getDateFromString("2015-05-16");
		c.setTime(date);
		assertEquals(2015, c.get(Calendar.YEAR));
		assertEquals(4, c.get(Calendar.MONTH));
		assertEquals(16, c.get(Calendar.DAY_OF_MONTH));

	}

	@Test
	public void testGetFormat2FromStd() {
    	assertEquals("2003-09-08", DateUtil.getFormat2FromStd("20030908", "yyyy-MM-dd"));
	}

	@Test
	public void testGetStringFromDate() {
		Calendar c = Calendar.getInstance();
		c.set(2017, 5, 4);
    	assertEquals("20170604", DateUtil.getStringFromDate(c.getTime()));
	}

	@Test
	public void testDiffDate() {
    	assertEquals(2, DateUtil.diffDate("20030902", "20030904"));
	}

	@Test
	public void testIsSameDayOfWeek() {
		Date date1 = DateUtil.getDateFromString("2015-07-12");
		Date date2 = DateUtil.getDateFromString("2015-07-19");
		assertTrue(DateUtil.isSameDayOfWeek(date1, date2));

	}

	@Test
	public void testIsSameDayOfMonth() {
		Date date1 = DateUtil.getDateFromString("2019-09-12");
		Date date2 = DateUtil.getDateFromString("2015-07-12");
		assertTrue(DateUtil.isSameDayOfMonth(date1, date2));
	}

	@Test
	public void testIsSameMonth() {
		Calendar c1 = Calendar.getInstance();
		c1.set(2017, 5, 4);
		Calendar c2 = Calendar.getInstance();
		c2.set(2018, 5, 10);
		assertTrue(DateUtil.isSameMonth(c1, c2));
	}


}