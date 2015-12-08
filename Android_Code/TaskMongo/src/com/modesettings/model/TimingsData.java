package com.modesettings.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.widget.TimePicker;

public class TimingsData {

	private String type, mode;
	private int ruleId, day, timingsDataId, alarmStartDay;
	private String timings, endTimings;
	private SimpleDateFormat formatter;
	private Calendar calendar;
	
	public TimingsData(){
		formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a" , Locale.getDefault());
		
	}
	
	public int getTimingsDataId() {
		return timingsDataId;
	}
	
	public void setTimingsDataId(int timingsDataId) {
		this.timingsDataId = timingsDataId;
	}
	
	public int getDay() {
		return day;
	}
	
	public void setDay(int day) {
		this.day = day;
		
	}
	
	public String getEndTimings() {
		return endTimings;
	}
	
	public void setEndTimings(String endTimings) {
		this.endTimings = endTimings;
	}
	
	public String getTimings() {
		return timings;
	}
	
	public void setTimings(String timings) {
		this.timings = timings;
		
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public int getRuleId() {
		return ruleId;
	}
	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}
	
	public long getTimeInMillis(String time){
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		
		int curDay = calendar.get(Calendar.DAY_OF_WEEK);
		
		if(day == curDay){
			alarmStartDay = 0;				
		}else if(day < curDay){
			alarmStartDay = day + (7 - curDay); // how many days until the specified day
		}else{
			alarmStartDay = day - curDay;
		}
		
		String[] timing = time.split(":");
		
		int hour = Integer.parseInt(timing[0].trim());
		int minute = Integer.parseInt(timing[1].trim());
		
		calendar.add(Calendar.DATE, alarmStartDay);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);
		
		return calendar.getTimeInMillis();
	}
	
}
