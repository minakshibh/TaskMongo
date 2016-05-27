package com.modesettings.util;

import com.modesetting.gps.BackgroundLocationService;
import com.modesettings.activity.ListRulesActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
 
/**
 * BroadCastReceiver for Alarms, displays notifications as it receives alarm
 * and then starts ChangeAlarmModeService to update alarm schedule with AlarmManager
 * @author Dhimitraq Jorgji
 *
 */
public class TaskMongoAlarmReceiver extends BroadcastReceiver {
	public static String ACTION_ALARM = "com.alarammanager.alaram";
	public static String ACTION_MODE = "com.phone.mode";
	public static String SILENT_MODE = "silent";
	public static String NORMAL_MODE = "normal";
	public static String VIBRATE_MODE = "vibrate";
	public static int timeLapse = 100000;
	public static String ACTION_START = "action_start";
	public static String ACTION_END = "action_end";
	public static String ALARM_ID = "alarmId";
	
	 @Override
	 public void onReceive(Context context, Intent intent) {
	
	
	  Bundle bundle = intent.getExtras();
	  String action = bundle.getString(ACTION_ALARM);
	  String mode = bundle.getString(ACTION_MODE);
	  int id = bundle.getInt(ALARM_ID);
	  
	  Log.e("Alarm Receiver", "Entered.. Mode = "+mode+" , id = "+id);
	  
	  if (action.equals(ACTION_ALARM)) {
		 Intent inService = new Intent(context,ChangeAlarmModeService.class);
		 inService.putExtra(ACTION_MODE, mode);
		 inService.putExtra(ALARM_ID, id);
		 context.startService(inService);
	  }
	 }
}
