package com.modesettings.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.modesetting.gps.BackgroundLocationService;
import com.modesetting.gps.ListLocationActivity;
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
	
	
	public ActionBarDrawerToggle mDrawerToggle;
	public DrawerLayout mDrawerLayout;
	public RelativeLayout flyoutDrawerRl;
	public AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.2F);
	TextView slider,sliderMenu;
	TextView Mainmenu;
	private LinearLayout lay_mylocation,lay_addLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_listrules);
		
		//satrt services
		startService(new Intent(ListRulesActivity.this,BackgroundLocationService.class));
		dbHandler = new SettingsDatabaseHandler(ListRulesActivity.this);

		initialTimeFormat = new SimpleDateFormat("HH:mm");
		desiredTimeFormat = new SimpleDateFormat("hh:mm a");
		
		
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setScrimColor(getResources().getColor(android.R.color.transparent));
		flyoutDrawerRl = (RelativeLayout) findViewById(R.id.left_drawer);
		lay_mylocation=(LinearLayout)findViewById(R.id.lay_mylocation);
		lay_addLocation=(LinearLayout)findViewById(R.id.lay_addLocation);
		drawble();
		
		addRule = (TextView) findViewById(R.id.btnAdd);
		addRule.setVisibility(View.GONE);
		ruleListView = (ListView) findViewById(R.id.listRoles);
		addRule.setOnClickListener(clickListener);
		
		AdView mAdView = (AdView) findViewById(R.id.adView);
		//mAdView.setAdSize(AdSize.BANNER);
		//mAdView.setAdUnitId("ca-app-pub-9728988682571889/1911358451");
//        AdRequest adRequest = new AdRequest.Builder().addTestDevice("5560E5DC05DF22FB254226E6DDFEE790").build();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        
        if(!getSharedPreferences(Util.APP_PREFERENCE, Activity.MODE_PRIVATE).getBoolean(Util.IS_ICON_CREATED, false)){
            addShortcut();
            getSharedPreferences(Util.APP_PREFERENCE, Activity.MODE_PRIVATE).edit().putBoolean(Util.IS_ICON_CREATED, true).commit();
        }
        
	}
private void drawble()
{
	Mainmenu=(TextView)findViewById(R.id.Mainmenu);
	Util.fontAwesomeApply(this, Mainmenu);
	Mainmenu.setOnClickListener(DrawerListener);
	sliderOnClickListener();
	setListenerOnDrawer();
	sliderMenu=(TextView)findViewById(R.id.Slidermenu);
	sliderMenu.setOnClickListener(new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (mDrawerLayout.isDrawerOpen(flyoutDrawerRl)) {
				mDrawerLayout.closeDrawers();
			}
		}
	});
	}
public void sliderOnClickListener() {

	lay_addLocation.setOnClickListener(clickListener);
	lay_mylocation.setOnClickListener(clickListener);
	

}
private void DrawerLayoutClose()
{
	//if (mDrawerLayout.isDrawerOpen(flyoutDrawerRl)) {
		mDrawerLayout.closeDrawers();
	//}
}

public View.OnClickListener DrawerListener = new View.OnClickListener() {

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (mDrawerLayout.isDrawerOpen(flyoutDrawerRl)) {
			mDrawerLayout.closeDrawers();
		} else {
			mDrawerLayout.openDrawer(flyoutDrawerRl);
		}
	}
};

private void setListenerOnDrawer() {
	mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
			R.drawable.ic_launcher, R.string.app_name, R.string.app_name) {
		/** Called when a drawer has settled in a completely closed state. */
		public void onDrawerClosed(View view) {
			super.onDrawerClosed(view);
		}

		/** Called when a drawer has settled in a completely open state. */
		public void onDrawerOpened(View drawerView) {
			super.onDrawerOpened(drawerView);
		}
	};
	// mDrawerLayout.setDrawerListener(mDrawerToggle);
}







	private void addShortcut() {
	    //Adding shortcut for MainActivity 
	    //on Home screen
	    Intent shortcutIntent = new Intent(getApplicationContext(),
	            SplashActivity.class);

	    shortcutIntent.setAction(Intent.ACTION_MAIN);

	    Intent addIntent = new Intent();
	    addIntent
	            .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
	            Intent.ShortcutIconResource.fromContext(getApplicationContext(),
	                    R.drawable.app_icon));

	    addIntent
	            .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    getApplicationContext().sendBroadcast(addIntent);
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
				Intent intent = new Intent(ListRulesActivity.this,SettingsActivity.class);
				intent.putExtra("trigger", "new");
				startActivity(intent);
				DrawerLayoutClose();
			} 
			else if (v == lay_addLocation) {
				Intent intent = new Intent(ListRulesActivity.this,SettingsActivity.class);
				intent.putExtra("trigger", "new");
				startActivity(intent);
				DrawerLayoutClose();
				
			} 
			else if (v == lay_mylocation) {
				Intent intent = new Intent(ListRulesActivity.this,ListLocationActivity.class);
				//mIntent.putExtra("locationName", value);
			    startActivity(intent);
				DrawerLayoutClose();
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
					alert.setTitle("Delete this mongo?");
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
