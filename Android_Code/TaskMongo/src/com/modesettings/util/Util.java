package com.modesettings.util;

import java.util.ArrayList;
import java.util.Calendar;

import android.R.bool;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.sax.EndTextElementListener;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.widget.TextView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.modesetting.gps.BackgroundLocationService;
import com.modesettings.model.Rule;
import com.modesettings.model.TimingsData;

public class Util {

	public static final String APP_PREFERENCE = "TaskMongoPreferences";
	public static final String IS_ICON_CREATED = "IsIconCreated";
	private static Context context;
	static	public String network_error="Please check your internet connection, try again";
	public static long milliSecondsForWeek = 7 * 24 * 60 * 60 * 1000;
		
//	public static ArrayList<Integer> alarmDataIds = new ArrayList<Integer>();
 	/*
	 * public static void setRule(Context ctx, Rule rule) { context = ctx;
	 * 
	 * try {
	 * 
	 * cancelAllAlarms();
	 * 
	 * // Schedule rule for (int i = 0; i < rule.getRuleData().size(); i++) {
	 * RuleData ruleData = rule.getRuleData().get(i); // Check for overlapping
	 * // ....
	 * 
	 * // If no overlapping setDateTimeOfAlarm(ruleData.getStartDateTime(),
	 * rule.getMode(), ruleData.getDataId());
	 * 
	 * // If overlapping // setDateTimeOfAlarm(day, overlappingEndHour, //
	 * overlappingEndMinute, pIntent);
	 * 
	 * // set the rule back to normal at end time
	 * setDateTimeOfAlarm(ruleData.getEndDateTime(),
	 * TaskMongoAlarmReceiver.NORMAL_MODE, ruleData.getDataId() +
	 * TaskMongoAlarmReceiver.timeLapse); } toast("Started...", ctx); } catch
	 * (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); }
	 * }
	 */

	public static void deleteRule(Context ctx, Rule rule) {
		context = ctx;

		try {

			// Schedule rule
			for (int i = 0; i < rule.getTimingsData().size(); i++) {
				TimingsData timingData = rule.getTimingsData().get(i);

				cancelRule(timingData.getTimingsDataId(), context);

				// cancelRule(ruleData.getDataId()
				// + TaskMongoAlarmReceiver.timeLapse, context);
			}

			toast("Cancelled...", ctx);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * public static int getDay(String day){
	 * 
	 * if(day.equals("Sunday")){ return Calendar.SUNDAY; }else
	 * if(day.equals("Monday")){ return Calendar.MONDAY; }else
	 * if(day.equals("Tuesday")){ return Calendar.TUESDAY; }else
	 * if(day.equals("Wednesday")){ return Calendar.WEDNESDAY; }else
	 * if(day.equals("Thursday")){ return Calendar.THURSDAY; }else
	 * if(day.equals("Friday")){ return Calendar.FRIDAY; }else { return
	 * Calendar.SATURDAY; }
	 * 
	 * }
	 */
	public static void cancelAllAlarms(Context ctx) {
		context = ctx;
		
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent updateServiceIntent = new Intent(context,
				TaskMongoAlarmReceiver.class);
		PendingIntent pendingUpdateIntent = PendingIntent.getService(context,
				0, updateServiceIntent, 0);

		// Cancel alarms
		try {
			alarmManager.cancel(pendingUpdateIntent);
			Log.e("TaskMongo", "AlarmManager canceled all alarms");
		} catch (Exception e) {
			Log.e("TaskMongo",
					"AlarmManager update was not canceled. " + e.toString());
		}
	}

	private static void setDateTimeOfAlarm(long dateTimeOfAlarm, String mode,
			int id, Context ctx, boolean repeat) {

		context = ctx;
		try {
			AlarmManager alarms = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			Intent intent = new Intent(context, TaskMongoAlarmReceiver.class);
			intent.putExtra(TaskMongoAlarmReceiver.ACTION_ALARM,
					TaskMongoAlarmReceiver.ACTION_ALARM);
			intent.putExtra(TaskMongoAlarmReceiver.ACTION_MODE, mode);
			intent.putExtra(TaskMongoAlarmReceiver.ALARM_ID, id);

			final PendingIntent pIntent = PendingIntent.getBroadcast(context,
					id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

			/*
			 * SimpleDateFormat formatter = new SimpleDateFormat(
			 * "yyyy-MM-dd hh:mm:ss a");
			 * 
			 * Date date = formatter.parse(dateTimeOfAlarm);
			 */
			/*
			 * SimpleDateFormat formatter = new
			 * SimpleDateFormat("dd.MM.yyyy, HH:mm"); String oldTime =
			 * "26.11.2015, 20:34"; Date oldDate = formatter.parse(oldTime);
			 * long oldMillis = oldDate.getTime();
			 */
			
			// long timeInMillis = date.getTime();

			if(repeat){
				alarms.setRepeating(AlarmManager.RTC_WAKEUP, dateTimeOfAlarm, milliSecondsForWeek, pIntent);
			}else{
				alarms.set(AlarmManager.RTC_WAKEUP, dateTimeOfAlarm, pIntent);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void refreshAllAlarms(Context ctx) {
		context = ctx;
		
		cancelAllAlarms(context);
		Log.e("refresh","refreshAllAlarms");
		SettingsDatabaseHandler dbHandler = new SettingsDatabaseHandler(context);
		ArrayList<Integer> days = new ArrayList<Integer>();

		days.add(Calendar.SUNDAY);
		days.add(Calendar.MONDAY);
		days.add(Calendar.TUESDAY);
		days.add(Calendar.WEDNESDAY);
		days.add(Calendar.THURSDAY);
		days.add(Calendar.FRIDAY);
		days.add(Calendar.SATURDAY);

		TimingsData bookedUntilTime = null;

		dbHandler.deleteAlarmData();
		
		TimingsData curTimingsData = new TimingsData();
		curTimingsData.setTimingsDataId(-1);
		
		for (int i = 0; i < days.size(); i++) {
			bookedUntilTime = null;
			
			ArrayList<Rule> ruleList = dbHandler.getRulesForSpecificDay(days
					.get(i));
			ArrayList<TimingsData> timingsDataList = dbHandler
					.getTimingsDataForSpecificDay(days.get(i));

			// It's today and no rule is there
			if(days.get(i) == Calendar.getInstance().get(Calendar.DAY_OF_WEEK) && timingsDataList.size() == 0){
				dbHandler.saveAlarmData(123456);
				setDateTimeOfAlarm(System.currentTimeMillis(), TaskMongoAlarmReceiver.NORMAL_MODE,
						123456 , context, false);
				Log.e("It's today, no rule is available so setting normal mode", "mode: NORMAL"  + " ,, id: 123456" );
				BackgroundLocationService.PriorityCheck=true;
			}else{
			
			for (int j = 0; j < timingsDataList.size(); j++) {
				TimingsData timingData = timingsDataList.get(j);
				long startTimings = timingData.getTimeInMillis(timingData.getTimings());

				if (null == bookedUntilTime
						|| getPriority(timingData.getMode()) >= getPriority(bookedUntilTime.getMode())
						|| startTimings >= bookedUntilTime.getTimeInMillis(bookedUntilTime.getEndTimings())) {

					long curTime = System.currentTimeMillis();
					
					if (timingData.getType().equals(
							TaskMongoAlarmReceiver.ACTION_START)) {
						dbHandler.saveAlarmData(timingData.getTimingsDataId());
						
						if(startTimings < curTime){
							if(timingData.getTimeInMillis(timingData.getEndTimings()) > curTime){
								/*Log.e("alarm set 4", "mode: " + timingData.getMode() + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
										+ timingData.getTimings());
								dbHandler.saveAlarmData(timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse);
								setDateTimeOfAlarm(curTime, timingData.getMode(),
										timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse, context, false);*/
								if(curTimingsData.getTimingsDataId()==-1 || (curTimingsData.getTimingsDataId()!=-1 && getPriority(curTimingsData.getMode())< getPriority(timingData.getMode())))
								{
									curTimingsData.setTimingsDataId(timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse);
									curTimingsData.setMode(timingData.getMode());
								}
							}
							Log.e("shifted time", "one week forward");
							startTimings = startTimings + milliSecondsForWeek;
						}
						BackgroundLocationService.PriorityCheck=false;
						setDateTimeOfAlarm(startTimings, timingData.getMode(),
									timingData.getTimingsDataId(), context, true);
						
						Log.e("Alarm type: START", "mode: " + timingData.getMode() + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
								+ timingData.getTimings());
						
						bookedUntilTime = timingData;
						
					} else {
						Rule selectedRule = null;
						for (int x = 0; x < ruleList.size(); x++) {
							Rule rule = ruleList.get(x);
							if (timingData.getTimings().compareTo(
									rule.getStartTime()) >= 0
									&& timingData.getTimings().compareTo(
											rule.getEndTime()) < 0) {
								if (null == selectedRule) {
									selectedRule = rule;
								}else if(getPriority(rule.getMode()) > getPriority(selectedRule.getMode())){
									selectedRule = rule;
								}
							}
						}
						if (null == selectedRule){
							dbHandler.saveAlarmData(timingData.getTimingsDataId());
							
							if(startTimings < curTime){
								if(j==timingsDataList.size()-1){
									Log.e("Alarm type: END, no further rule found and end time has already passed away", "mode: " +  TaskMongoAlarmReceiver.NORMAL_MODE + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
											+ timingData.getTimings());
									dbHandler.saveAlarmData(timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse);
									BackgroundLocationService.PriorityCheck=true;
									setDateTimeOfAlarm(curTime, TaskMongoAlarmReceiver.NORMAL_MODE,
											timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse, context, false);
								}
								startTimings = startTimings + milliSecondsForWeek;
								Log.e("shifted time", "one week forward");
							}
							
							BackgroundLocationService.PriorityCheck=true;
							setDateTimeOfAlarm(startTimings,
									TaskMongoAlarmReceiver.NORMAL_MODE,
									timingData.getTimingsDataId(), context, true);
							Log.e("Alarm type: END, no further rule found", "mode: " + TaskMongoAlarmReceiver.NORMAL_MODE + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
									+ timingData.getTimings());
							
						}else{
							dbHandler.saveAlarmData(timingData.getTimingsDataId());
							
							if(startTimings < curTime){
								if(timingData.getTimeInMillis(selectedRule.getEndTime()) > curTime){
									Log.e("Alarm type: END, rule found but starttime has already passed away", "mode: " + selectedRule.getMode() + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
											+ timingData.getTimings());
									dbHandler.saveAlarmData(timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse);
									BackgroundLocationService.PriorityCheck=false;
									setDateTimeOfAlarm(curTime, selectedRule.getMode(),
											timingData.getTimingsDataId() + TaskMongoAlarmReceiver.timeLapse, context, false);
								}
								startTimings = startTimings + milliSecondsForWeek;
								Log.e("shifted time", "one week forward");
							}
							BackgroundLocationService.PriorityCheck=false;
							setDateTimeOfAlarm(startTimings,
									selectedRule.getMode(),
									timingData.getTimingsDataId(), context, true);
							Log.e("Alarm type: END, rule found, activalting selected rule", "mode: " + selectedRule.getMode() + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
									+ timingData.getTimings());
							bookedUntilTime.setMode(selectedRule.getMode());
							bookedUntilTime.setTimings(selectedRule.getStartTime());
							bookedUntilTime.setEndTimings(selectedRule.getEndTime());
							
						}
					}
				}
			}
			}
		}

		/*ArrayList<Integer> alarmdata = dbHandler.getAlarmData();
		
		
		for(int i = 0; i < alarmdata.size(); i++){
			Log.e("alarm Id : ", ""+ alarmdata.get(i));
		}*/
		
		if(curTimingsData.getTimingsDataId()!=-1){
//			Log.e("alarm set 4", "mode: " + timingData.getMode() + " ,, id: " + timingData.getTimingsDataId() + " ,time: "
//					+ timingData.getTimings());
			dbHandler.saveAlarmData(curTimingsData.getTimingsDataId());
			setDateTimeOfAlarm(System.currentTimeMillis(), curTimingsData.getMode(),
					curTimingsData.getTimingsDataId(), context, false);
		}
		/*
		 * for (int i = 0; i < rules.size(); i++) { Rule rule = new Rule(); rule
		 * = rules.get(i);
		 * 
		 * ArrayList<RuleData> ruleData = rule.getRuleData(); TimingsData
		 * startTime, endTime;
		 * 
		 * for(int j = 0; j < ruleData.size(); j++){ try{ RuleData rData = new
		 * RuleData(); // Date sDate =
		 * formatter.parse(rData.getStartDateTime());
		 * 
		 * startTime = new TimingsData();
		 * startTime.setTimings(Long.parseLong(rData.getStartDateTime()));
		 * startTime.setMode(rule.getMode());
		 * startTime.setRuleDataId(rData.getDataId());
		 * startTime.setType(TaskMongoAlarmReceiver.ACTION_START);
		 * 
		 * // Date eDate = formatter.parse(rData.getEndDateTime()); endTime =
		 * new TimingsData();
		 * 
		 * endTime.setTimings(Long.parseLong(rData.getEndDateTime()));
		 * startTime.setEndTimings(endTime.getTimings());
		 * 
		 * endTime.setMode(rule.getMode());
		 * endTime.setRuleDataId(rData.getDataId());
		 * endTime.setType(TaskMongoAlarmReceiver.ACTION_END);
		 * 
		 * 
		 * timingsList.add(startTime); timingsList.add(endTime);
		 * }catch(Exception e){ e.printStackTrace(); } } }
		 * 
		 * Collections.sort(timingsList, new Util().new TimingsComparator());
		 * 
		 * TimingsData bookedUntilTime = null;
		 * 
		 * // for one day for(int i = 0; i < timingsList.size(); i++){
		 * TimingsData timingData = timingsList.get(i); if(null==bookedUntilTime
		 * || getPriority(timingData.getMode()) >=
		 * getPriority(bookedUntilTime.getMode()) ||
		 * timingData.getTimings()>=bookedUntilTime.getTimings()){
		 * 
		 * if(timingData.getType().equals(TaskMongoAlarmReceiver.ACTION_START)){
		 * setDateTimeOfAlarm(timingData.getTimings(), timingData.getMode(),
		 * i+TaskMongoAlarmReceiver.timeLapse); bookedUntilTime = timingData;
		 * }else{ for(int j = 0; j < ) } } }
		 */
	}

	public static int getPriority(String mode) {
		if (mode.equals(TaskMongoAlarmReceiver.SILENT_MODE))
			return 3;
		else if (mode.equals(TaskMongoAlarmReceiver.VIBRATE_MODE))
			return 2;
		else
			return 1;

	}

	public static void cancelRule(int id, Context ctx) {
		context = ctx;

		try {
			Intent intent = new Intent(context, TaskMongoAlarmReceiver.class);
			intent.putExtra(TaskMongoAlarmReceiver.ACTION_ALARM,
					TaskMongoAlarmReceiver.ACTION_ALARM);

			final PendingIntent pIntent = PendingIntent.getBroadcast(context, 123,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);

			AlarmManager alarms = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);

			alarms.cancel(pIntent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void toast(String message, Context ctx) {
		context = ctx;
		
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	 static public void hideKeyboard(Context cxt) {
	        //   context=cxt;
	        InputMethodManager inputManager = (InputMethodManager) cxt.getSystemService(Context.INPUT_METHOD_SERVICE);

	        // check if no view has focus:
	        View view = ((Activity) cxt).getCurrentFocus();
	        if (view != null) {
	            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	        }
	    }

	 public static void fontAwesomeApply(Context act,TextView...params)
	    {
	        Typeface typeFace_icon= Typeface.createFromAsset(act.getAssets(), "fontawesome-webfont.ttf");
	        for (TextView tv : params) {
	            tv.setTypeface(typeFace_icon);
	        }
	    }
	 static public boolean isNetworkAvailable(Context mCtx) {
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
	 static	public void alertMessage(final Context ctx,String str)
		{
			context=ctx;
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			alert.setTitle("");
			alert.setMessage(str);
			alert.setPositiveButton("OK", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				
				}

			
			});
			alert.show();
		}
}
