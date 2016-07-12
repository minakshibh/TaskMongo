package com.modesetting.gps;

import java.util.ArrayList;
import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.modesettings.model.LocationDetails;
import com.modesettings.util.TaskMongoAlarmReceiver;
import com.modesettings.util.Util;

public class BackgroundLocationService extends Service implements
		GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, LocationListener {

	private final String TAG = "MyAwesomeApp";
	private GoogleApiClient mGoogleApiClient;
	private LocationRequest mLocationRequest;
	public static final String KEY_USER_LAT = "userLat";
	public static final String KEY_USER_LONG = "userLong";
	public float location_accuracy = 0f;
	private SharedPreferences prefs;
	public static final String LOCATION_BROADCAST_ACTION = "new location";
	public static final String LOCATION_UPDATE_BROADCAST = "location_update";

	public ArrayList<LocationDetails> arrayLatLng = new ArrayList<LocationDetails>();

	private int changeAllAlarmMode=0;
	private boolean refreshAlarm=true;

	@Override
	public void onCreate() {

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(LocationServices.API).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		mGoogleApiClient.connect();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		mGoogleApiClient.disconnect();
		super.onDestroy();
	}

	@Override
	public void onConnected(Bundle bundle) {

		mLocationRequest = LocationRequest.create();
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		mLocationRequest.setInterval(60 * 1000); // Update location every minute

		int permission_finelocation = ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION);
		int permission_coarse_location = ContextCompat.checkSelfPermission(
				this, Manifest.permission.ACCESS_COARSE_LOCATION);
		if (permission_finelocation == PackageManager.PERMISSION_GRANTED
				|| permission_coarse_location == PackageManager.PERMISSION_GRANTED) {
			LocationServices.FusedLocationApi.requestLocationUpdates(
					mGoogleApiClient, mLocationRequest, this);
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.i(TAG, "GoogleApiClient connection has been suspend");
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.i(TAG, "GoogleApiClient connection has failed");
	}

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
		boolean inLocation=false;
		String mode="";
		// Double lat= Double.parseDouble("30.711161373760145");
		// Double lng=Double.parseDouble("76.71124957501888");
		// LatLng loc=new LatLng(lat,lng);

		String msg = Double.toString(location.getLatitude()) + ","
				+ Double.toString(location.getLongitude());

	
		arrayLatLng = GpsUtil.getLocations(getApplicationContext());

		if (arrayLatLng != null && arrayLatLng.size() > 0) {
			refreshAlarm=true;
			System.err.println("size= " + arrayLatLng.size());
			for (int i = 0; i < arrayLatLng.size(); i++) {
				Double distance = DistanceCalculator.distance(
						location.getLatitude(), location.getLongitude(),
						arrayLatLng.get(i).latiDouble,
						arrayLatLng.get(i).logiDouble, "M");
				if (distance < 1) {
					changeAllAlarmMode=0;
					inLocation=true;
					mode=arrayLatLng.get(i).mode;
					}
				}
		
			if(inLocation){	
		
				if(changeAllAlarmMode==0){
					changeAllAlarmMode=1;
					changeMode(mode);
					Util.cancelAllAlarms(getApplicationContext());
				}
			}
			else{
			
				if(changeAllAlarmMode==1){
				Util.refreshAllAlarms(getApplicationContext());
				changeAllAlarmMode=0;
				}
			}

		}
		else{
			if(refreshAlarm){
				refreshAlarm=false;
				Util.refreshAllAlarms(getApplicationContext());
				}
		}

		System.err.println("current location=" + msg);

	}

	private void changeMode(String mode) {

	//	System.err.println("location name=" + locationName + " mode=" + mode);
		// Util.ToastMessage(getApplicationContext(),
		// locationName+" location in 1 M Circle, Mode="+mode);

		AudioManager am = (AudioManager) getBaseContext().getSystemService(
				Context.AUDIO_SERVICE);

		// Do some task
		if (mode.equalsIgnoreCase(TaskMongoAlarmReceiver.SILENT_MODE)) {
			am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			Log.e("ChangeAlarmModeService", mode + " set");

		} else if (mode.equalsIgnoreCase(TaskMongoAlarmReceiver.NORMAL_MODE)) {
			am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			Log.e("ChangeAlarmModeService", mode + " set");

		} else if (mode.equalsIgnoreCase(TaskMongoAlarmReceiver.VIBRATE_MODE)) {
			am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			Log.e("ChangeAlarmModeService", mode + " set");
		} else {
			Log.e("Task Mongo", "id not found");
		}

	}

}
