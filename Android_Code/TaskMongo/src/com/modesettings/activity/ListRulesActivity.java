package com.modesettings.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.modesettings.model.Rule;
import com.modesettings.util.SettingsDatabaseHandler;
import com.modesettings.util.TaskMongoAlarmReceiver;
import com.modesettings.util.Util;

public class ListRulesActivity extends Activity {

	private TextView addRule;
	private ArrayList<Rule> rules;
	private ListView ruleListView;
	private SimpleDateFormat initialTimeFormat, desiredTimeFormat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_listrules);

		initialTimeFormat = new SimpleDateFormat("HH:mm");
		desiredTimeFormat = new SimpleDateFormat("hh:mm a");
		
		addRule = (TextView) findViewById(R.id.btnAdd);
		ruleListView = (ListView) findViewById(R.id.listRoles);

		addRule.setOnClickListener(clickListener);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshListView();
	}

	private void getRules() {
		SettingsDatabaseHandler dbHandler = new SettingsDatabaseHandler(
				ListRulesActivity.this);
		rules = dbHandler.getRules();
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == addRule) {
				Intent intent = new Intent(ListRulesActivity.this,
						SettingsActivity.class);
				intent.putExtra("trigger", "new");
				startActivity(intent);
			} 
		}
	};

	public class RuleAdapter extends BaseSwipeAdapter {
		private Context context;
		private TextView ruleName, ruleTimings, deleteRule, days;
		private TextView editRule;
		private ImageView ruleMode;

		public RuleAdapter(Context ctx) {
			context = ctx;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return rules.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return rules.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public void fillValues(final int position, View convertView) {
			// TODO Auto-generated method stub
			ruleName = (TextView) convertView.findViewById(R.id.ruleName);
			ruleMode = (ImageView) convertView.findViewById(R.id.mode);
			ruleTimings = (TextView) convertView.findViewById(R.id.timings);
			deleteRule = (TextView) convertView.findViewById(R.id.deleteRule);
			editRule = (TextView) convertView.findViewById(R.id.editRule);
			
			days = (TextView) convertView.findViewById(R.id.days);

			ruleName.setText(rules.get(position).getDescription());
			if (rules.get(position).getMode()
					.equals(TaskMongoAlarmReceiver.SILENT_MODE))
				ruleMode.setBackgroundResource(R.drawable.silent_icon);
			else if (rules.get(position).getMode()
					.equals(TaskMongoAlarmReceiver.NORMAL_MODE))
				ruleMode.setBackgroundResource(R.drawable.sound_icon);
			else if (rules.get(position).getMode()
					.equals(TaskMongoAlarmReceiver.VIBRATE_MODE))
				ruleMode.setBackgroundResource(R.drawable.vibrate_icon);

			
	        Date testStartDate = null;
	        try {
	        	testStartDate = initialTimeFormat.parse(rules.get(position).getStartTime());
	        }catch(Exception ex){
	            ex.printStackTrace();
	        }
	        String startTime = desiredTimeFormat.format(testStartDate);
			
	        Date testEndDate = null;
	        try {
	        	testEndDate = initialTimeFormat.parse(rules.get(position).getEndTime());
	        }catch(Exception ex){
	            ex.printStackTrace();
	        }
	        String endTime = desiredTimeFormat.format(testEndDate);
	        
			ruleTimings.setText(startTime + " - " + endTime);
			
			days.setText(rules.get(position).getSelectedDays());

			deleteRule.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog.Builder alert = new AlertDialog.Builder(
							ListRulesActivity.this);
					alert.setTitle("Delete this rule?");
					alert.setMessage("Are you sure?");
					alert.setPositiveButton("No", null);
					alert.setNegativeButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									SettingsDatabaseHandler dbHandler = new SettingsDatabaseHandler(
											ListRulesActivity.this);
									dbHandler.deleteRule(rules.get(position)
											.getId());
									Util.refreshAllAlarms(ListRulesActivity.this);
									
									refreshListView();
								}
							});
					alert.show();
				}
			});
			
			editRule.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent mainIntent = new Intent(ListRulesActivity.this,
							SettingsActivity.class);
					mainIntent.putExtra("trigger", "edit");
					mainIntent.putExtra("Rule", rules.get(position));
					ListRulesActivity.this.startActivity(mainIntent);
				}
			});
		}

		@Override
		public View generateView(int arg0, ViewGroup arg1) {
			return LayoutInflater.from(ListRulesActivity.this).inflate(
					R.layout.rule_row, null);

		}

		@Override
		public int getSwipeLayoutResourceId(int arg0) {
			// TODO Auto-generated method stub
			return R.id.swipe;
		}
	}

	public void refreshListView() {
		getRules();
		ruleListView.setAdapter(new RuleAdapter(ListRulesActivity.this));
	}
}
