package com.modesettings.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.EditText;

public class SplashActivity extends Activity {
	int value=1;
	SharedPreferences sharePreferences;
	Editor edit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);

		sharePreferences=getPreferences(1);
		
		
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(sharePreferences.getString("home", "").equals("")){
					
					edit=sharePreferences.edit();
					edit.putString("home", "yes");
					edit.commit();
					
					Intent mainIntent = new Intent(SplashActivity.this,HomeViewPagerActivity.class);
					SplashActivity.this.startActivity(mainIntent);
					SplashActivity.this.finish();
				}else{
					Intent mainIntent = new Intent(SplashActivity.this,ListRulesActivity.class);
					SplashActivity.this.startActivity(mainIntent);
					SplashActivity.this.finish();
				}
			}
		}, 2000);
	}

}
