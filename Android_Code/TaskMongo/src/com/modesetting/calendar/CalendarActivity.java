package com.modesetting.calendar;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.modesettings.activity.ListRulesActivity;
import com.modesettings.activity.R;
import com.modesettings.activity.SettingsActivity;
import com.modesettings.model.Rule;
import com.modesettings.model.TimingsData;
import com.modesettings.util.SettingsDatabaseHandler;
import com.modesettings.util.TaskMongoAlarmReceiver;
import com.modesettings.util.Util;

@SuppressLint("NewApi")
public class CalendarActivity extends Activity {
	long lastEventId = -1;
	int ruleId=0;
	int	deleteId=0;
	private String TAG="array";
	private ArrayList<Integer> days=new ArrayList<Integer>();
	private SharedPreferences spref, sprefOnce;
	private ArrayList<String> calAllEventIds=new ArrayList<String>();
	private ArrayList<String> appAllEventIds=new ArrayList<String>();
	private List<String> commonElement = new ArrayList<String>();
	private List<String> deleteElement = new ArrayList<String>();
	private Rule rule=new Rule();
	private SettingsDatabaseHandler dbHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);
		requestCalendarSync();
	
		dbHandler=new SettingsDatabaseHandler(getApplicationContext());
		spref = getSharedPreferences("mongo", MODE_PRIVATE);
		sprefOnce = getSharedPreferences("sprefOnce", MODE_PRIVATE);
		lastEventId = GetMaxID();
		if (sprefOnce.getString("once", "").equals("")) {
			Editor editor1 = sprefOnce.edit();
			editor1.putString("once", "" + lastEventId);
			editor1.commit();

			Editor editor = spref.edit();
			editor.putLong("Id", lastEventId);
			editor.commit();
		}
		System.err.println("ID=" + lastEventId + " ShareID=" + spref.getLong("Id", 0));
		if (lastEventId > spref.getLong("Id", 0))//for new add event
		{
			Editor editor = spref.edit();
			editor.putLong("Id", lastEventId);
			editor.commit();
			customDailog("Do you want to save this event as a Mongo?","calendar");
			fetchAllEvent(true);
			
			
		} 
		else  //for new edit and delete events
		{
			appAllEventIds.clear();
			dbHandler = new SettingsDatabaseHandler(CalendarActivity.this);
			ArrayList<Rule> rules = dbHandler.getRules();
			for(int i=0;i<rules.size();i++){
				appAllEventIds.add(rules.get(i).getEventID());
			}
					
			 rule = dbHandler.getRuleId(""+lastEventId);
			if(rule!=null){
				try{
					ruleId=rule.getId();
					//get common value
					ArrayList<String> saveValue=readArray();
					Log.e("saveValue of calall", saveValue.toString());
					Log.e("appAllEventIds", appAllEventIds.toString());
					
					commonElement = new ArrayList<String>(saveValue);
					commonElement.retainAll(appAllEventIds);
					
					Log.e("commonElement", commonElement.toString());
					
					fetchAllEvent(false);
					Log.e("after del calAllEventIds", calAllEventIds.toString());
					//get delete value  
					deleteElement = new ArrayList<String>(commonElement);
					deleteElement.removeAll(calAllEventIds);
					
					Log.e("deleteElement", deleteElement.toString());
					}catch(Exception e)
						{
							e.printStackTrace();
							finish();
						}
				 		
			}
			
			if(deleteElement.size()==0) //edit event
			{
				try{
				//fetchAllEvent(true);
				commonElement = new ArrayList<String>(readArray());
				commonElement.retainAll(appAllEventIds);
				//finish();
				int editRuleId=0;String	eventId="";
				String mode=TaskMongoAlarmReceiver.VIBRATE_MODE;
					for(int i=0;i<commonElement.size();i++){
						rule = dbHandler.getRuleId(""+commonElement.get(i));
						if(rule!=null){
					
							editRuleId=rule.getId();
							mode=rule.getMode();
							eventId=rule.getEventID();
						}
						
						fetchEventData(Long.parseLong(eventId),"editEvent",editRuleId,mode);
					}
					if(editRuleId==0){
						
						finish();
						}
				}catch(Exception e)
				{
					e.printStackTrace();
					finish();
				}
				
				}
			else{ //delete event
				
				deleteId=Integer.parseInt(deleteElement.get(0).toString());
				Rule rule1 = dbHandler.getRuleId(""+deleteId);
				deleteId=rule1.getId();
				//customDailog("Do you want to this event changes as a Mongo?","delete");
				//fetchEventData(getId,"delete");
			//	fetchAllEvent(t);
			  if(deleteId!=0)
		        {			        	
		        	dbHandler = new SettingsDatabaseHandler(CalendarActivity.this);
					dbHandler.deleteRule(deleteId);
					Util.refreshAllAlarms(CalendarActivity.this);
					Toast.makeText(getApplicationContext(), "This event deleted successfully in task mongo app", Toast.LENGTH_SHORT).show();
					//Intent intent = new Intent(CalendarActivity.this,ListRulesActivity.class);
					//startActivity(intent);
					finish();
			       	
		        	}
			  finish(); 	
			}
			
		}

	}

	public void customDailog(String str,final String type) {
		final Dialog dialog = new Dialog(CalendarActivity.this);
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setCancelable(false);
		dialog.setTitle("Task Mongo");
		TextView txt = (TextView) dialog.findViewById(R.id.txt_dia);
		txt.setText(str);
		Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
		btn_no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
		btn_yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				try {
					fetchEventData(lastEventId,type,0,null);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		dialog.show();
	}

	public long GetMaxID() {
		ContentResolver cr = getContentResolver();
		Uri caluri = CalendarContract.Events.CONTENT_URI;
		Uri local_uri = caluri;
		if (caluri == null) {
			local_uri = Uri.parse("content://com.android.calendar/events");
		}
		Cursor cursor = cr.query(local_uri,
				new String[] { "MAX(_id) as max_id" }, null, null, "_id");

		cursor.moveToFirst();
		long max_val = cursor.getLong(cursor.getColumnIndex("max_id"));
		return max_val + 1;

	}

	private void fetchEventData(long eventID,String type,int editRuleId,String mode) {

		String event_Title = null, event_Desc = null;
		Date event_Start = null, event_end = null;
		ContentResolver cr = getContentResolver();
		Uri caluri = CalendarContract.Events.CONTENT_URI;
		// Uri atteuri = CalendarContract.Attendees.CONTENT_URI;
		Cursor cur1;// , cur2;
		String all = null,HAS_ALARM,deleted;
		String selection = "(" + Events._ID + " = ?)";
		String[] selectionArgs = new String[] { Long.toString(eventID - 1) };
		try {
			cur1 = cr.query(caluri, new String[] { Events.CALENDAR_ID,
					Events._ID,Events.HAS_ALARM, Events.DELETED, Events.TITLE, Events.DESCRIPTION,
					Events.DTSTART, Events.DTEND, Events.EVENT_LOCATION },
					selection, selectionArgs, null);

			if (cur1 != null) {
				while (cur1.moveToNext()) {
					HAS_ALARM = cur1.getString(cur1.getColumnIndex(Events.HAS_ALARM));
					deleted = cur1.getString(cur1.getColumnIndex(Events.DELETED));
					event_Title = cur1.getString(cur1
							.getColumnIndex(Events.TITLE));
					event_Desc = cur1.getString(cur1
							.getColumnIndexOrThrow(Events.DESCRIPTION));
					event_Start = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTSTART)));
					event_end = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTEND)));
					String event_loc = cur1.getString(cur1
							.getColumnIndex(Events.EVENT_LOCATION));
					/*
					 * String all_attendee = null; String all_Emails = null;
					 * 
					 * String cal_ID =
					 * cur1.getString(cur1.getColumnIndex(Events.CALENDAR_ID));
					 * String event_ID =
					 * cur1.getString(cur1.getColumnIndex(Events._ID));
					 */
					/*
					 * cur2 = cr.query(atteuri, new String[]{
					 * Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL } ,
					 * Attendees.EVENT_ID + "=" + eventID, null, null); if (cur2
					 * != null) { while (cur2.moveToNext()) { String
					 * attendee_name =
					 * cur2.getString(cur2.getColumnIndex(Attendees
					 * .ATTENDEE_NAME)); String attendee_Email =
					 * cur2.getString(cur2
					 * .getColumnIndex(Attendees.ATTENDEE_EMAIL));
					 * 
					 * all_attendee += "\n" + attendee_name; all_Emails += "\n"
					 * + attendee_Email; } cur2.close(); }
					 */
					all += "deleted: " + deleted + "\n"
							+ "Event Description: " + event_Desc + "\n"
							+ "Event Start: " + event_Start + "\n"
							+ "Events End: " + event_end + "\n"
							+ "HAS_ALARM: " + HAS_ALARM;
					// + "\n" + "Attendees: " + "\n" + all_attendee + "\n"
					// + "Emails: " + "\n" + all_Emails + "\n";
				}
				cur1.close();

				goToNext(type,"" + event_Title, "" + event_Desc, "" + event_Start,
						"" + event_end,""+eventID,editRuleId,mode);
			}
			//System.out.println("My log--------" + all);
			// Toast.makeText(DailogActivity.this, all,
			// Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			e.printStackTrace();
			 finish();
		}
	}

	private void goToNext(String type,String event_Title, String event_Desc,
			String event_Start, String event_end,String EventId,int editRuleId,String mode) {
		// TODO Auto-generated method stub
		String startTime = null;
		String endTime = null,startDate=null;
		String startDay = null,endDate=null;
		String inFormatter = "E MMM dd HH:mm:ss Z yyyy";// Event Start: Mon May
														// 02 05:30:00 IST 2016
		String outFormatter= "yyyy-MM-dd hh:mm:ss a" ;
		String TimeFormatter = "HH:mm";
		// String DateFormatter= "yyyy-MM-dd" ;
		String DayFormatter = "E";
		if (event_Start != null) {

			try {
				startDay = formateDateFromstring(inFormatter, DayFormatter, ""+ event_Start);
				startTime = formateDateFromstring(inFormatter,TimeFormatter, "" + event_Start);
				endTime = formateDateFromstring(inFormatter, TimeFormatter,"" + event_end);

				 startDate=formateDateFromstring(inFormatter,outFormatter,""+event_Start);
				 endDate=formateDateFromstring(inFormatter,outFormatter,""+event_end);
				/*if(startDay.equalsIgnoreCase("sun"))
				{
					
					}*/
			} 
			catch (Exception e) {
				e.printStackTrace();
			}

		}
		int days=findDate(startDate,endDate);
		System.err.println("days="+days);
		String totalDays=selectDays(days,startDay);
	
		if(type.equalsIgnoreCase("calendar")){
			Intent mIntent = new Intent(CalendarActivity.this,SettingsActivity.class);
			mIntent.putExtra("trigger", "calendar");
			mIntent.putExtra("title", event_Title);
			mIntent.putExtra("start", startTime);
			mIntent.putExtra("end", endTime);
			mIntent.putExtra("startDay", totalDays);
			mIntent.putExtra("eventId", ""+lastEventId);
			mIntent.putExtra("ruleId", ruleId);
			startActivity(mIntent);
			finish();
		}
		else{
			
			saveEditRule(event_Title, startTime, endTime,""+ EventId, totalDays,editRuleId,mode);
		}
		
	}


	public static String formateDateFromstring(String inputFormat,
			String outputFormat, String inputDate) {

		Date parsed = null;
		String outputDate = "";

		SimpleDateFormat df_input = new SimpleDateFormat(inputFormat,
				java.util.Locale.getDefault());
		SimpleDateFormat df_output = new SimpleDateFormat(outputFormat,
				java.util.Locale.getDefault());

		try {
			parsed = df_input.parse(inputDate);
			outputDate = df_output.format(parsed);

		} catch (Exception e) {
			// LOGE(TAG, "ParseException - dateFormat");
		}

		return outputDate;

	}
	private int findDate(String date1,String date2) {
		int day=0;
		Date startDate = null, endDate = null;
		try {
			DateFormat dtimeformatter = new SimpleDateFormat("yyyy-MM-dd");
			startDate = (Date) dtimeformatter.parse(date1);
			endDate = (Date) dtimeformatter.parse(date2);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(startDate);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(endDate);

		//System.err.println("cal=1=====" + cal1);
	//	System.err.println("cal2======" + cal2);
		day = getDaysDifference(cal1, cal2);
		return day+1;
	}
	public static int getDaysDifference(Calendar calendar1, Calendar calendar2) {
		if (calendar1 == null || calendar2 == null)
			return 0;

		return (int) ((calendar2.getTimeInMillis() - calendar1
				.getTimeInMillis()) / (1000 * 60 * 60 * 24));
	}
	private void fetchAllEvent(boolean save) {
		calAllEventIds.clear();
		String id=null, event_Title = null, event_Desc = null;
		String deleted="-1",HAS_ALARM;
		Date event_Start = null, event_end = null;
		ContentResolver cr = getContentResolver();
		Uri caluri = CalendarContract.Events.CONTENT_URI;
		// Uri atteuri = CalendarContract.Attendees.CONTENT_URI;
		Cursor cur1;// , cur2;
		String all = null;
	
		try {
			cur1 = cr.query(caluri, new String[] { Events.CALENDAR_ID,
					Events._ID, Events.DELETED,Events.TITLE, Events.DESCRIPTION,
					Events.DTSTART, Events.DTEND, Events.EVENT_LOCATION },
					null, null, null);

			if (cur1 != null) {
				while (cur1.moveToNext()) {
					id = cur1.getString(cur1.getColumnIndex(Events._ID));
				//	HAS_ALARM = cur1.getString(cur1.getColumnIndex(Events.);
					deleted = cur1.getString(cur1.getColumnIndex(Events.DELETED));
					event_Title = cur1.getString(cur1
							.getColumnIndex(Events.TITLE));
					event_Desc = cur1.getString(cur1
							.getColumnIndexOrThrow(Events.DESCRIPTION));
					event_Start = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTSTART)));
					
					event_end = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTEND)));
					String event_loc = cur1.getString(cur1
							.getColumnIndex(Events.EVENT_LOCATION));
				int getId=Integer.parseInt(id);
				int addOneVlaue=getId+1;
				//System.out.println("My log--------" + addOneVlaue);
				//delete=1,0 =not
				
				//check for delete or not from calendar
					if(deleted.equalsIgnoreCase("0")){
						calAllEventIds.add(""+addOneVlaue);
					}
					
					all += " "+ "d"+deleted+addOneVlaue + " Event title: " + event_Title + "\n"
						//	+ "Event Description: " + event_Desc + "\n"
							+ "Event Start: " + event_Start + "\n"
							+ "Events End: " + event_end ;
							//+ "Event Location: " + event_loc;
					
					//System.out.println("all fetch ids of cal--------" + all);
					
				}
				cur1.close();

				
			}
		
			
			/*//remove duplicate value
			 HashSet<String> hashSet = new HashSet<String>();
			    hashSet.addAll(calAllEventIds);
			    calAllEventIds.clear();
			    calAllEventIds.addAll(hashSet);*/
			   if(save){
			    saveArray(calAllEventIds);
			    }

		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}
	private void requestCalendarSync()
	{
	    AccountManager aM = AccountManager.get(this);
	    Account[] accounts = aM.getAccounts();

	    for (Account account : accounts)
	    {
	        int isSyncable = ContentResolver.getIsSyncable(account,  CalendarContract.AUTHORITY);

	        if (isSyncable > 0)
	        {
	            Bundle extras = new Bundle();
	            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
	            ContentResolver.requestSync(accounts[0], CalendarContract.AUTHORITY, extras);
	        }
	    }
	}
private void saveArray(ArrayList<String> arrayList){
	
	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	Editor editor = sharedPrefs.edit();
	Gson gson = new Gson();
	String json = gson.toJson(arrayList);
	editor.putString(TAG, json);
	editor.commit();
	}
	
public ArrayList<String> readArray() {
  
    List<String> favorites;

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

    if (settings.contains(TAG)) {
        String jsonFavorites = settings.getString(TAG, null);
        Gson gson = new Gson();
        String[] favoriteItems = gson.fromJson(jsonFavorites,
        		String[].class);

        favorites = Arrays.asList(favoriteItems);
        favorites = new ArrayList<String>(favorites);
    } else
        return null;

    return (ArrayList<String>) favorites;
}
private void saveEditRule(String title,String getStartTime,String getEndTime,String eventId,String strdays,int editRuleId,String mode){

	try{
	Rule rule = new Rule();
	rule.setDescription(title);
	rule.setMode(mode);
	rule.setIsEnabled("true");
	String sHour, sMin, eHour, eMin;
	
	
	rule.setStartTime(getStartTime);
	rule.setEndTime(getEndTime);

	rule.setSelectedDays(strdays);
	rule.setEventID(eventId);
	
	
	ArrayList<TimingsData> timingsData = new ArrayList<TimingsData>();
	
	TimingsData startTime, endTime, tempStartTime, tempEndTime;
	//days = getSelectedDays();
	if(!strdays.equals("")){
	String[] arraydays = strdays.split(",");
	
		for(int i=0; i<arraydays.length; i++){
			days.add(getDayIndex(arraydays[i]));
		}
	}
	for(int j = 0; j < days.size(); j++){
		try{
//			Date sDate = formatter.parse(rData.getStartDateTime());
			int day = days.get(j);
			
			startTime = new TimingsData();
			endTime = new TimingsData();
			startTime.setTimings(rule.getStartTime());
			startTime.setMode(rule.getMode());
			startTime.setRuleId(rule.getId());
			startTime.setDay(day);
			startTime.setType(TaskMongoAlarmReceiver.ACTION_START);
			startTime.setEndTimings(rule.getEndTime());
			
			if(!isTimeCorrect(""+startTime,""+endTime)){

				startTime.setEndTimings("23:59");
				tempEndTime = new TimingsData();
				tempEndTime.setTimings("23:59");
				tempEndTime.setMode(rule.getMode());
				tempEndTime.setRuleId(rule.getId());
				tempEndTime.setType(TaskMongoAlarmReceiver.ACTION_END);
				tempEndTime.setDay(day);
				
				Log.e("time greater than start time", "adding one "+day);
				if(day == Calendar.SATURDAY)
					day = Calendar.SUNDAY;
				else
					day += 1;
				
				tempStartTime = new TimingsData();
				tempStartTime.setTimings("00:00");
				tempStartTime.setMode(rule.getMode());
				tempStartTime.setRuleId(rule.getId());
				tempStartTime.setDay(day);
				tempStartTime.setType(TaskMongoAlarmReceiver.ACTION_START);
				tempStartTime.setEndTimings(rule.getEndTime());
				
				timingsData.add(tempEndTime);
				timingsData.add(tempStartTime);
			}
		//	
//			Date eDate = formatter.parse(rData.getEndDateTime());
			
			endTime.setTimings(rule.getEndTime());
			endTime.setMode(rule.getMode());
			endTime.setRuleId(rule.getId());
			endTime.setType(TaskMongoAlarmReceiver.ACTION_END);
			endTime.setDay(day);
			
			timingsData.add(startTime);
			timingsData.add(endTime);
		}catch(Exception e){
			e.printStackTrace();
			 finish();
		}
	}
	
	
		rule.setTimingsData(timingsData);
		
		SettingsDatabaseHandler dbHandler = new SettingsDatabaseHandler(CalendarActivity.this);
		int id = dbHandler.saveRule(rule, editRuleId);
		System.err.println(" "+rule+" "+editRuleId);
		if(id!=-1){
			Util.refreshAllAlarms(CalendarActivity.this);
			Toast.makeText(getApplicationContext(), "This event edited successfully in task mongo app", Toast.LENGTH_SHORT).show();
			}
		 finish();
		}catch(Exception e)
		{
			e.printStackTrace();
			 finish();
		}
	}
	private int getDayIndex(String day){
		if(day.trim().equals("Sun"))
			return Calendar.SUNDAY;
		else if(day.trim().equals("Mon"))
			return Calendar.MONDAY;
		else if(day.trim().equals("Tue"))
			return Calendar.TUESDAY;
		else if(day.trim().equals("Wed"))
			return Calendar.WEDNESDAY;
		else if(day.trim().equals("Thur"))
			return Calendar.THURSDAY;
		else if(day.trim().equals("Fri"))
			return Calendar.FRIDAY;
		else if(day.trim().equals("Sat"))
			return Calendar.SATURDAY;
		else
			return 0;    	
	}

	private boolean isTimeCorrect(String startTime,String endTime) {
		/*if(startTimePicker.getCurrentHour()>endTimePicker.getCurrentHour())
			return false;
		else if(startTimePicker.getCurrentHour()==endTimePicker.getCurrentHour() && startTimePicker.getCurrentMinute()>=endTimePicker.getCurrentMinute())
			return false;
		else
			return true;*/

		float totalStartMins=0;
		float totalEndMins=0;
		        try {
		            String[] parts = startTime.split(":");
		            String part1 = parts[0]; //
		            String part2 = parts[1]; //
		            float fPart1 = Float.parseFloat(part1);
		            float fPart2 = Float.parseFloat(part2);
		            float hoursInMins = fPart1 * 60;
		            totalStartMins = hoursInMins + fPart2;
		  
		            String[] parts2 = endTime.split(":");
		            String part21 = parts2[0]; //
		            String part22 = parts2[1]; //
		            float fPart21 = Float.parseFloat(part21);
		            float fPart22 = Float.parseFloat(part22);
		            float hoursInMins2 = fPart21 * 60;
		            totalEndMins = hoursInMins2 + fPart22;
		            
		            if(totalStartMins>totalEndMins){
						return false;
					}
					else{
						return true;
					}
		           
		        } catch (Exception e) {
		            e.printStackTrace();
		            return true;
		        }
			
		    
	}
	private String selectDays(int days,String dayName) {
		// TODO Auto-generated method stub
		String totalDays="";
		if(days==1)
		{
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat";
			}
			
		}
		else if(days==2){
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun";
			}
			
		}
		else if(days==3){
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon,Tue";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue,Wed";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed,Thur";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur,Fri";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat,Sun";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun,Mon";
			}	
			}
		else if(days==4){
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon,Tue,Wed";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue,Wed,Thur";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed,Thur,Fri";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur,Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri,Sat,Sun";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat,Sun,Mon";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun,Mon,Tue";
			}	
		}
		else if(days==5){
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon,Tue,Wed,Thur";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue,Wed,Thur,Fri";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed,Thur,Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur,Fri,Sat,Sun";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri,Sat,Sun,Mon";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat,Sun,Mon,Tue";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun,Mon,Tue,Wed";
			}	
		}
		else if(days==6){
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon,Tue,Wed,Thur,Fri";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue,Wed,Thur,Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed,Thur,Fri,Sat,Sun";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur,Fri,Sat,Sun,Mon";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri,Sat,Sun,Mon,Tue";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat,Sun,Mon,Tue,Wed";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun,Mon,Tue,Wed,Thur";
			}	
		}
		else{
			if(dayName.equalsIgnoreCase("sun"))
			{
				totalDays="Sun,Mon,Tue,Wed,Thur,Fri,Sat";
			}
			else if(dayName.equalsIgnoreCase("mon"))
			{
				totalDays="Mon,Tue,Wed,Thur,Fri,Sat,Sun";
			}
			else if(dayName.equalsIgnoreCase("tue"))
			{
				totalDays="Tue,Wed,Thur,Fri,Sat,Sun,Mon";
			}
			else if(dayName.equalsIgnoreCase("wed"))
			{
				totalDays="Wed,Thur,Fri,Sat,Sun,Mon,Tue";
			}
			else if(dayName.equalsIgnoreCase("thu"))
			{
				totalDays="Thur,Fri,Sat,Sun,Mon,Tue,Wed";
			}
			else if(dayName.equalsIgnoreCase("fri"))
			{
				totalDays="Fri,Sat,Sun,Mon,Tue,Wed,Thur";
			}
			else if(dayName.equalsIgnoreCase("sat"))
			{
				totalDays="Sat,Sun,Mon,Tue,Wed,Thur,Fri";
			}	
		}
		
		return totalDays;
	}
}
