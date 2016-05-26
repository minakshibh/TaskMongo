package com.modesetting.calendar;

import java.util.Calendar;

import com.modesettings.util.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;



public class CalendarChangedReceiver extends BroadcastReceiver{

	  public void onReceive(Context context, Intent intent) {
	     
		 if(intent.getAction().equalsIgnoreCase("android.intent.action.PROVIDER_CHANGED")) 
		 {
			 

			 try{
	          Intent mIntent=new Intent(context,CalendarActivity.class);
	          mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	          context.startActivity(mIntent);
			 }catch(Exception e)
			 {
				Util.toast(e.getMessage(), context);
			 }
		 }else{
			 System.err.println("app not open");
		 }

	        }
	    
}
