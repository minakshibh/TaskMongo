package com.modesettings.util;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class ChangeAlarmModeService extends IntentService {

	private SettingsDatabaseHandler dbHandler;

	public ChangeAlarmModeService() {
		super("ChangeAlarmModeService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		String mode = arg0.getStringExtra(TaskMongoAlarmReceiver.ACTION_MODE);
		int id = arg0.getIntExtra(TaskMongoAlarmReceiver.ALARM_ID, -1);

		dbHandler = new SettingsDatabaseHandler(getApplicationContext());
		ArrayList<Integer> alarmData = dbHandler.getAlarmData();

		if (alarmData.contains(id)) {
			Log.e("Task Mongo", "id found");
			AudioManager am;
			am = (AudioManager) getBaseContext().getSystemService(
					Context.AUDIO_SERVICE);

			// Do some task
			if (mode.equalsIgnoreCase(TaskMongoAlarmReceiver.SILENT_MODE)) {
				am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Log.e("ChangeAlarmModeService", mode + " set");
			} else if (mode
					.equalsIgnoreCase(TaskMongoAlarmReceiver.NORMAL_MODE)) {
				am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				Log.e("ChangeAlarmModeService", mode + " set");
			} else if (mode
					.equalsIgnoreCase(TaskMongoAlarmReceiver.VIBRATE_MODE)) {
				am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				Log.e("ChangeAlarmModeService", mode + " set");
			}
		} else {
			Log.e("Task Mongo", "id not found");
		}
	}
}
