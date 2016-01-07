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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.modesettings.model.Rule;
import com.modesettings.util.SettingsDatabaseHandler;
import com.modesettings.util.TaskMongoAlarmReceiver;
import com.modesettings.util.Util;

public class ListRulesActivity extends Activity {

	private TextView addRule;
	private ArrayList<Rule> rules;
	private ListView ruleListView;
	private SimpleDateFormat initialTimeFormat, desiredTimeFormat;
	
	private SettingsDatabaseHandler dbHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_listrules);
		dbHandler = new SettingsDatabaseHandler(ListRulesActivity.this);

		initialTimeFormat = new SimpleDateFormat("HH:mm");
		desiredTimeFormat = new SimpleDateFormat("hh:mm a");
		
		addRule = (TextView) findViewById(R.id.btnAdd);
		ruleListView = (ListView) findViewById(R.id.listRoles);

		addRule.setOnClickListener(clickListener);
		
		AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("5560E5DC05DF22FB254226E6DDFEE790").build();
        mAdView.loadAd(adRequest);
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
		private TextView editRule, enableRule;
		private ImageView ruleMode;
		private RelativeLayout transparentLayer;
		
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
			
			final Rule selectedRule = rules.get(position);
			
			ruleName = (TextView) convertView.findViewById(R.id.ruleName);
			ruleMode = (ImageView) convertView.findViewById(R.id.mode);
			ruleTimings = (TextView) convertView.findViewById(R.id.timings);
			deleteRule = (TextView) convertView.findViewById(R.id.deleteRule);
			editRule = (TextView) convertView.findViewById(R.id.editRule);
			days = (TextView) convertView.findViewById(R.id.days);
			enableRule = (TextView) convertView.findViewById(R.id.enable);
			transparentLayer = (RelativeLayout) convertView.findViewById(R.id.transparentLayer);
			
			if(Boolean.valueOf(selectedRule.getIsEnabled())){
				enableRule.setText("Disable");
				transparentLayer.setVisibility(View.GONE);
			}else{
				enableRule.setText("Enable");
				transparentLayer.setVisibility(View.VISIBLE);
			}
			
			ruleName.setText(selectedRule.getDescription());
			if (selectedRule.getMode()
					.equals(TaskMongoAlarmReceiver.SILENT_MODE))
				ruleMode.setBackgroundResource(R.drawable.silent_icon);
			else if (selectedRule.getMode()
					.equals(TaskMongoAlarmReceiver.NORMAL_MODE))
				ruleMode.setBackgroundResource(R.drawable.sound_icon);
			else if (selectedRule.getMode()
					.equals(TaskMongoAlarmReceiver.VIBRATE_MODE))
				ruleMode.setBackgroundResource(R.drawable.vibrate_icon);

			
	        Date testStartDate = null;
	        try {
	        	testStartDate = initialTimeFormat.parse(selectedRule.getStartTime());
	        }catch(Exception ex){
	            ex.printStackTrace();
	        }
	        String startTime = desiredTimeFormat.format(testStartDate);
			
	        Date testEndDate = null;
	        try {
	        	testEndDate = initialTimeFormat.parse(selectedRule.getEndTime());
	        }catch(Exception ex){
	            ex.printStackTrace();
	        }
	        String endTime = desiredTimeFormat.format(testEndDate);
	        
			ruleTimings.setText(startTime + " - " + endTime);
			
			days.setText(selectedRule.getSelectedDays());

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
									dbHandler.deleteRule(selectedRule
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
					mainIntent.putExtra("Rule", selectedRule);
					ListRulesActivity.this.startActivity(mainIntent);
				}
			});
			
			enableRule.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(Boolean.valueOf(selectedRule.getIsEnabled())){
						selectedRule.setIsEnabled("false");
					}else{
						selectedRule.setIsEnabled("true");
					}
					notifyDataSetChanged();
					dbHandler.updateRule(selectedRule);
					Util.refreshAllAlarms(ListRulesActivity.this);
					
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
