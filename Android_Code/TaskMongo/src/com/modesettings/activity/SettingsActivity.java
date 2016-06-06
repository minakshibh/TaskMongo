package com.modesettings.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.modesettings.model.Rule;
import com.modesettings.model.TimingsData;
import com.modesettings.util.SettingsDatabaseHandler;
import com.modesettings.util.TaskMongoAlarmReceiver;
import com.modesettings.util.Util;

public class SettingsActivity extends Activity {

	private Button cancel, save;
	private TextView back;
	private EditText desc;
	private TimePicker startTimePicker, endTimePicker;
	private ToggleButton[] tgWeekDays = new ToggleButton[7];
	private ToggleButton[] tgModes = new ToggleButton[3];
	
	private final int NORMAL = 0, SILENT = 1, VIBRATE = 2;
	private String selectedMode = "";
	private ArrayList<Integer> days;
	private SimpleDateFormat formatter;
	private Calendar calendar;
	private String trigger;
	private Rule rule;
	private int ruleId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settings);
		days = new ArrayList<Integer>();
		
		/*IntentFilter filter = new IntentFilter(TaskMongoAlarmReceiver.ACTION_ALARM);
		BroadcastReceiver mReceiver = new TaskMongoAlarmReceiver();
		registerReceiver(mReceiver, filter);*/
		
	
		formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a" , Locale.getDefault());
		
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getDefault());
		
		cancel = (Button) findViewById(R.id.btnCancel);
		save = (Button) findViewById(R.id.btnSave);
		back = (TextView) findViewById(R.id.btnBack);
		desc = (EditText) findViewById(R.id.txtDesc);
		startTimePicker = (TimePicker) findViewById(R.id.startTimePicker);
		endTimePicker = (TimePicker) findViewById(R.id.endTimePicker);

		tgWeekDays[0] = (ToggleButton) findViewById(R.id.tgSun);
		tgWeekDays[0].setTag(Calendar.SUNDAY);
		
		tgWeekDays[1] = (ToggleButton) findViewById(R.id.tgMon);
		tgWeekDays[1].setTag(Calendar.MONDAY);
		
		tgWeekDays[2] = (ToggleButton) findViewById(R.id.tgTue);
		tgWeekDays[2].setTag(Calendar.TUESDAY);
		
		tgWeekDays[3] = (ToggleButton) findViewById(R.id.tgWed);
		tgWeekDays[3].setTag(Calendar.WEDNESDAY);
		
		tgWeekDays[4] = (ToggleButton) findViewById(R.id.tgThur);
		tgWeekDays[4].setTag(Calendar.THURSDAY);
		
		tgWeekDays[5] = (ToggleButton) findViewById(R.id.tgFri);
		tgWeekDays[5].setTag(Calendar.FRIDAY);
		
		tgWeekDays[6] = (ToggleButton) findViewById(R.id.tgSat);
		tgWeekDays[6].setTag(Calendar.SATURDAY);

		tgModes[NORMAL] = (ToggleButton) findViewById(R.id.tgNormal);
		tgModes[SILENT] = (ToggleButton) findViewById(R.id.tgSilent);
		tgModes[VIBRATE] = (ToggleButton) findViewById(R.id.tgVibrate);

		cancel.setOnClickListener(clickListener);
		save.setOnClickListener(clickListener);
		back.setOnClickListener(clickListener);
		
		tgModes[NORMAL].setOnCheckedChangeListener(checkedChangeListener);
		tgModes[SILENT].setOnCheckedChangeListener(checkedChangeListener);
		tgModes[VIBRATE].setOnCheckedChangeListener(checkedChangeListener);
		
		keyBoard_Button();
		trigger = getIntent().getStringExtra("trigger");
	
		if(trigger.equals("edit")){
						
			rule = getIntent().getParcelableExtra("Rule");
			ruleId = rule.getId();
			
			desc.setText(rule.getDescription());
			
			String[] startTime = rule.getStartTime().split(":");
			startTimePicker.setCurrentHour(Integer.parseInt(startTime[0]));
			startTimePicker.setCurrentMinute(Integer.parseInt(startTime[1]));
			
			String[] endTime = rule.getEndTime().split(":");
			endTimePicker.setCurrentHour(Integer.parseInt(endTime[0]));
			endTimePicker.setCurrentMinute(Integer.parseInt(endTime[1]));
				
			String[] selection = rule.getSelectedDays().split(", ");
			
			for(int i = 0; i<selection.length; i++){
				days.add(getDayIndex(selection[i]));
			}
			
			String mode = rule.getMode();
			if(mode.equals(TaskMongoAlarmReceiver.NORMAL_MODE))
				tgModes[NORMAL].setChecked(true);
			else if(mode.equals(TaskMongoAlarmReceiver.VIBRATE_MODE))
				tgModes[VIBRATE].setChecked(true);
			else if(mode.equals(TaskMongoAlarmReceiver.SILENT_MODE))
				tgModes[SILENT].setChecked(true);
				
			/*
			private ToggleButton[] tgModes = new ToggleButton[3];
			
			private String selectedMode = "";
			private ArrayList<Integer> days;
			private SimpleDateFormat formatter;
			private Calendar calendar;
			private String trigger;
			private Rule rule;*/
		}
		else if(trigger.equalsIgnoreCase("calendar"))
		{
			desc.setText(getIntent().getStringExtra("title"));
			
			String getStartTime=getIntent().getStringExtra("start");
			String[] startTime = getStartTime.split(":");
			startTimePicker.setCurrentHour(Integer.parseInt(startTime[0]));
			startTimePicker.setCurrentMinute(Integer.parseInt(startTime[1]));
		
			
			String getEndTime=getIntent().getStringExtra("end");
			String[] endTime = getEndTime.split(":");
			endTimePicker.setCurrentHour(Integer.parseInt(endTime[0]));
			endTimePicker.setCurrentMinute(Integer.parseInt(endTime[1]));
			
			
			String[] selection = getIntent().getStringExtra("startDay").split(",");
			
			for(int i = 0; i<selection.length; i++){
				days.add(getDayIndex(selection[i]));
			}
			
		}
		/*else if(trigger.equalsIgnoreCase("calEdit"))
		{
			ruleId = getIntent().getIntExtra("ruleId", -1);
			desc.setText(getIntent().getStringExtra("title"));
			String getStartTime=getIntent().getStringExtra("start");
			String[] startTime = getStartTime.split(":");
			startTimePicker.setCurrentHour(Integer.parseInt(startTime[0]));
			startTimePicker.setCurrentMinute(Integer.parseInt(startTime[1]));
		
			
			String getEndTime=getIntent().getStringExtra("end");
			String[] endTime = getEndTime.split(":");
			endTimePicker.setCurrentHour(Integer.parseInt(endTime[0]));
			endTimePicker.setCurrentMinute(Integer.parseInt(endTime[1]));
			
			
			String[] selection = getIntent().getStringExtra("startDay").split(",");
			
			for(int i = 0; i<selection.length; i++){
				days.add(getDayIndex(selection[i]));
			}
		}*/
				
		initDays(trigger);
		
		AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("5560E5DC05DF22FB254226E6DDFEE790").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        
	}

	private void initDays(String trigger) {
		// TODO Auto-generated method stub
		if(trigger.equals("edit")){
			for(int i = 0; i < tgWeekDays.length; i++){
				if(days.contains(tgWeekDays[i].getTag()))
					tgWeekDays[i].setChecked(true);
				else
					tgWeekDays[i].setChecked(false);
			}
		}
		else if(trigger.equalsIgnoreCase("calendar")){
			for(int i = 0; i < tgWeekDays.length; i++){
				if(days.contains(tgWeekDays[i].getTag()))
					tgWeekDays[i].setChecked(true);
				else
					tgWeekDays[i].setChecked(false);
			}
		}
		else{
			int curDay = calendar.get(Calendar.DAY_OF_WEEK);
			
			for(int i = 0; i < tgWeekDays.length; i++){
				if(tgWeekDays[i].getTag().equals(curDay))
					tgWeekDays[i].setChecked(true);
				else
					tgWeekDays[i].setChecked(false);
			}
		}
		
		days.clear();
	}

	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			if (buttonView.getId() == tgModes[NORMAL].getId()){
				if(isChecked){
					tgModes[SILENT].setChecked(false);
					tgModes[VIBRATE].setChecked(false);
				}
			} else if (buttonView.getId() == tgModes[SILENT].getId()){
				if(isChecked){
					tgModes[NORMAL].setChecked(false);
					tgModes[VIBRATE].setChecked(false);
				}
			} else if (buttonView.getId() == tgModes[VIBRATE].getId()){
				if(isChecked){
					tgModes[SILENT].setChecked(false);
					tgModes[NORMAL].setChecked(false);
				}
			}
		}
	};
	
	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == cancel) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						SettingsActivity.this);
				alert.setTitle("Unsaved changes will be lost.");
				alert.setMessage("Are you sure?");
				alert.setPositiveButton("No", null);
				alert.setNegativeButton("Yes",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								finish();
							}
						});
				alert.show();
			} else if (v == back) {
				if(trigger.equalsIgnoreCase("calendar"))
				{
				Intent intent = new Intent(SettingsActivity.this,ListRulesActivity.class);
				startActivity(intent);
				finish();
				}
				else
				{
					finish();
						}
			} else if (v == save) {
				selectedMode = getSelectedMode();
				days = getSelectedDays();
				
				AlertDialog.Builder alert = new AlertDialog.Builder(
						SettingsActivity.this);
				alert.setTitle("Incomplete Information");
				if (desc.getText().toString().trim().equals("")
						||  selectedMode.equals("")|| days.size()==0
						/*|| (!isTimeCorrect() && !hasConsecutiveDays())*/) {

					if (desc.getText().toString().trim().equals(""))
						alert.setMessage("Please enter description of Mongo.");
					else if (days.size()==0)
						alert.setMessage("Please select atleast one day.");
					else if (selectedMode.equals(""))
						alert.setMessage("Please select atleast one mode.");
					/*else if (!isTimeCorrect() && !hasConsecutiveDays())
						alert.setMessage("You must select two consecutive days if you are selecting ");
*/
					alert.setPositiveButton("Okay", null);
					alert.show();

				} else {
					if(!isTimeCorrect()){
						AlertDialog.Builder warning = new AlertDialog.Builder(SettingsActivity.this);
						warning.setTitle("WARNING! The end time is less than the start time!");
						warning.setMessage("Mongo will end on next day. Do you wish to continue?");
						warning.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								saveRule();
							}
						});
						warning.setNegativeButton("No", null);
						warning.show();
					}else{
						saveRule();
					}
				}
			}
		}
	};
	 private void  keyBoard_Button()
	    {
		 desc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
	 			@Override
				public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
				    if (actionId == EditorInfo.IME_ACTION_NEXT) {
		                   
	                    Util.hideKeyboard(SettingsActivity.this);
	                    return true;
	                }
	                return false;
				}
	        });
	    }
	private String getDate(Calendar time, int daysToAdd, TimePicker timePicker){
		
		time.add(Calendar.DATE, daysToAdd);
		time.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
		time.set(Calendar.MINUTE, timePicker.getCurrentMinute());
		time.set(Calendar.SECOND, 0);
		
		String date = formatter.format(time.getTime());
		return String.valueOf(time.getTime());
	}
	
	private String getSelectedDays(ArrayList<Integer> days){
    	String selectedDays="";
    	for(int i = 0; i<days.size(); i++){
    		
    		if(i==days.size()-1)
    			selectedDays = selectedDays + getDay(days.get(i));
    		else
    			selectedDays = selectedDays + getDay(days.get(i)) + ", ";
    	}
    	
    	return selectedDays;
    }
    
    private String getDay(int day){
    	switch(day){
    	case Calendar.SUNDAY:
    		return "Sun";
    	case Calendar.MONDAY:
    		return "Mon";
    	case Calendar.TUESDAY:
    		return "Tue";
    	case Calendar.WEDNESDAY:
    		return "Wed";
    	case Calendar.THURSDAY:
    		return "Thur";
    	case Calendar.FRIDAY:
    		return "Fri";
    	case Calendar.SATURDAY:
    		return "Sat";
    	default:
    		return "";
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
    
	private void saveRule(){

		Rule rule = new Rule();
		rule.setDescription(desc.getText().toString().trim());
		rule.setMode(selectedMode);
		rule.setIsEnabled("true");
		String sHour, sMin, eHour, eMin;
		
		if(startTimePicker.getCurrentHour()<10)
			sHour = "0"+startTimePicker.getCurrentHour();
		else
			sHour = "" +startTimePicker.getCurrentHour();
		
		if(startTimePicker.getCurrentMinute()<10)
			sMin = "0"+startTimePicker.getCurrentMinute();
		else
			sMin = "" +startTimePicker.getCurrentMinute();
		
		if(endTimePicker.getCurrentHour()<10)
			eHour = "0"+endTimePicker.getCurrentHour();
		else
			eHour = "" +endTimePicker.getCurrentHour();
		
		if(endTimePicker.getCurrentMinute()<10)
			eMin = "0"+endTimePicker.getCurrentMinute();
		else
			eMin = "" +endTimePicker.getCurrentMinute();
		
		rule.setStartTime(sHour+":"+sMin);
		rule.setEndTime(eHour+":"+eMin);
	
		rule.setSelectedDays(getSelectedDays(days));
		if(getIntent().getStringExtra("eventId")!=null)
		{
			rule.setEventID(getIntent().getStringExtra("eventId"));
			}
		else{
			rule.setEventID("-1");
		}
		
		ArrayList<TimingsData> timingsData = new ArrayList<TimingsData>();
		
		TimingsData startTime, endTime, tempStartTime, tempEndTime;
		
		for(int j = 0; j < days.size(); j++){
			try{
//				Date sDate = formatter.parse(rData.getStartDateTime());
				int day = days.get(j);
				
				startTime = new TimingsData();
				endTime = new TimingsData();
				startTime.setTimings(rule.getStartTime());
				startTime.setMode(rule.getMode());
				startTime.setRuleId(rule.getId());
				startTime.setDay(day);
				startTime.setType(TaskMongoAlarmReceiver.ACTION_START);
				startTime.setEndTimings(rule.getEndTime());
				
				if(!isTimeCorrect()){

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
				
//				Date eDate = formatter.parse(rData.getEndDateTime());
				
				endTime.setTimings(rule.getEndTime());
				endTime.setMode(rule.getMode());
				endTime.setRuleId(rule.getId());
				endTime.setType(TaskMongoAlarmReceiver.ACTION_END);
				endTime.setDay(day);
				
				timingsData.add(startTime);
				timingsData.add(endTime);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		/*ArrayList<RuleData> ruleData = new ArrayList<RuleData>();
		
		for(int i = 0; i<selectedDays.size(); i++){
			RuleData rule_data = new RuleData();
			int day = selectedDays.get(i);
			
			rule_data.setDay(day);
			
			// Get start time
			Calendar startTime = Calendar.getInstance();
			startTime.setTimeZone(TimeZone.getDefault());
			
			int alarmStartDay = 0;
			int curDay = startTime.get(Calendar.DAY_OF_WEEK);
			
			if(day == curDay){
				alarmStartDay = 0;				
			}else if(day < curDay){
				alarmStartDay = day + (7 - curDay); // how many days until Sunday
			}else{
				alarmStartDay = day - curDay;
			}
			
			rule_data.setStartDateTime(getDate(startTime, alarmStartDay, startTimePicker));
			
			//Get end time
			Calendar endTime = Calendar.getInstance();
			
			if(!isTimeCorrect()){
				Log.e("time greater than start time", "adding one "+alarmStartDay);
				alarmStartDay += 1;
			}
			
			rule_data.setEndDateTime(getDate(endTime, alarmStartDay, endTimePicker));
			
			Log.e("schedule", rule_data.getStartDateTime()	+ " , " + rule_data.getEndDateTime());
			
			ruleData.add(rule_data);
		}
		rule.setRuleData(ruleData);*/
		rule.setTimingsData(timingsData);
		
		SettingsDatabaseHandler dbHandler = new SettingsDatabaseHandler(SettingsActivity.this);
		int id = dbHandler.saveRule(rule, ruleId);
		
		if(id!=-1){
//			Rule savedRule = dbHandler.getRule(id);
			
//			Util.setRule(SettingsActivity.this, savedRule);
			Util.refreshAllAlarms(SettingsActivity.this);
			
			AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);
			alert.setTitle("Congratulations!!!");
			alert.setMessage("Mongo saved successfully");
			alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if(trigger.equalsIgnoreCase("calendar"))
					{
						Intent intent = new Intent(SettingsActivity.this,ListRulesActivity.class);
						startActivity(intent);
						finish();
						}
					else{
						
						finish();
						}
				}
			});
			alert.show();
		}
	}
	
	private boolean isTimeCorrect() {
		if(startTimePicker.getCurrentHour()>endTimePicker.getCurrentHour())
			return false;
		else if(startTimePicker.getCurrentHour()==endTimePicker.getCurrentHour() && startTimePicker.getCurrentMinute()>=endTimePicker.getCurrentMinute())
			return false;
		else
			return true;
	}
	
	private boolean hasConsecutiveDays(){
		boolean hasConsecutiveDays = false;
		
		for(int i = 0; i<days.size() - 1; i++){
			if(days.get(i) - days.get(i + 1) == 1){
				hasConsecutiveDays = true;
				break;
			}
		}
		
		return hasConsecutiveDays;
	}

	private String getSelectedMode() {
		selectedMode = "";
		
		for (int i = 0; i < tgModes.length; i++) {
			if (tgModes[i].isChecked()) {
				if(i == NORMAL)
					selectedMode = TaskMongoAlarmReceiver.NORMAL_MODE;
				else if(i == SILENT)
					selectedMode = TaskMongoAlarmReceiver.SILENT_MODE;
				else if(i == VIBRATE)
					selectedMode = TaskMongoAlarmReceiver.VIBRATE_MODE;
				
				break;
			}
		}
		return selectedMode;
	}

	private ArrayList<Integer> getSelectedDays() {

		days.clear();
		for (int i = 0; i < tgWeekDays.length; i++) {
			if (tgWeekDays[i].isChecked()) {
					days.add(Integer.parseInt(tgWeekDays[i].getTag().toString()));
			}
		}
		
		return days;
	}

	
}
