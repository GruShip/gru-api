package com.tech.dream.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtility {

	public static Date stringToDateFormat(String dateInString, String format) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date date = dateFormat.parse(dateInString);
		return date;
		
	}
	
	public static String dateToStringFormat(Date date, String format) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String dateStr = dateFormat.format(date);
		return dateStr;
	}
	
	
	/***
	 * 
	 * @param date
	 * @param calendar - to specify add Date by DATE, HOUR, DAY, MONTH ,etc
	 * @param value - number that needs to be added to date 
	 * @return
	 */
	public static Date addDate(Date date, int calendar, int value){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(calendar, value);
		return cal.getTime(); 
	}
	
	public static void main(String[] args) {
		System.out.println(dateToStringFormat(new Date(), "yyyy_MM_dd_HH_mm_ss"));
	}
}
