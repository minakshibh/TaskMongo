package com.modesettings.util;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
 
/**
 * BroadCastReceiver for android.intent.action.BOOT_COMPLETED
 * passes all responsibility to TaskButlerService.
 *
 */

public class TaskMongoOnBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
    	
    	try{
    		Toast.makeText(context, "Starting Mongos...", Toast.LENGTH_LONG).show();
    	   Util.refreshAllAlarms(context);
    	
    	}catch(Exception e){
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Exception");
    		alert.setMessage(e.getMessage());
    		alert.setPositiveButton("OK", null);
    		alert.show();
    	}catch (Error e) {
    		AlertDialog.Builder alert = new AlertDialog.Builder(context);
    		alert.setTitle("Error");
    		alert.setMessage(e.getMessage());
    		alert.setPositiveButton("OK", null);
    		alert.show();
		}
    }
}
