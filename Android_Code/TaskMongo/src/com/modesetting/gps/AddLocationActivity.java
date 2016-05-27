package com.modesetting.gps;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.modesettings.activity.R;
import com.modesettings.model.LocationDetails;
import com.modesettings.util.TaskMongoAlarmReceiver;

@SuppressWarnings("unused")
@SuppressLint("NewApi")
public class AddLocationActivity extends AppCompatActivity implements
		OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {
	private GoogleMap googleMap;
	private int flagCurrentPosition = 1;
	private double searchLat, searchLon, pickupLatitude, pickupLogitude;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private TextView txt_Save, btnBack;
	private float currentZoom = 17;
	private AutoCompleteTextView autotext_address;
	private boolean isFromAutoCompleteOnClick = true;
	private MapCameraChangeListener mCameraChangeListener = new MapCameraChangeListener();
	private AlertDialog levelDialog = null;
	private ImageView imag_cross;
	private int stopSeaching = 2;
	public ArrayList<LocationDetails> arrayLatLng;
	private String trigger = "",Edit=null;
	private int position=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_add_locatiom);

		getSupportActionBar().hide();

		setUI();
		mapInitialize();
		setAddressAdapter();
		setOnClick();

	}

	private void setUI() {
		// TODO Auto-generated method stub
		if (getIntent().getStringExtra("locationName") != null) {
			trigger = getIntent().getStringExtra("locationName");
		}
		if (getIntent().getStringExtra("edit") != null) {
			Edit = getIntent().getStringExtra("edit");
			
		}
		imag_cross = (ImageView) findViewById(R.id.imag_cross);
		autotext_address = (AutoCompleteTextView) findViewById(R.id.autotext_address);
		txt_Save = (TextView) findViewById(R.id.txt_go);
		btnBack = (TextView) findViewById(R.id.btnBack);
	}

	private void setAddressAdapter() {

		autotext_address.setThreshold(4);

		try {
			autotext_address.setAdapter(new PlacesAutoAdapter(this,
					R.layout.adapter_layout));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void setOnClick() {
		// TODO Auto-generated method stub
		autotext_address.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == 0) {
					stopSeaching = 1;
				}
			}
		});
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AddLocationActivity.this,ListLocationActivity.class);
				startActivity(intent);
				finish();
			}
		});

		txt_Save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				final CharSequence[] items = { " Normal ", " Slient ",
						" Vibrate " };

				AlertDialog.Builder builder = new AlertDialog.Builder(
						AddLocationActivity.this);
				builder.setTitle("Select mode");
				builder.setSingleChoiceItems(items, -1,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {

								switch (item) {
								case 0:
									saveLocation(TaskMongoAlarmReceiver.NORMAL_MODE);
									levelDialog.dismiss();
									break;
								case 1:
									saveLocation(TaskMongoAlarmReceiver.SILENT_MODE);
									levelDialog.dismiss();
									break;
								case 2:
									saveLocation(TaskMongoAlarmReceiver.VIBRATE_MODE);
									levelDialog.dismiss();
									break;

								}

							}
						});
				// builder.setPositiveButton("Cancel", null);
				levelDialog = builder.create();
				levelDialog.show();

			}
		});

		imag_cross.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				autotext_address.setText("");
				googleMap.clear();
				stopSeaching = 1;
			}
		});

		autotext_address
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						if (autotext_address.getText().toString().trim()
								.length() != 0) {
							searchLocation();
							hideKeyboard(2);
						} else {
							Toast.makeText(AddLocationActivity.this,
									"Please enter pickup location",
									Toast.LENGTH_SHORT).show();
						}
					}
				});

	}

	private void saveLocation(String mode) {
		
		if(Edit!=null)
		{
			position=getIntent().getIntExtra("position",0);
			GpsUtil.removeLocation(getApplicationContext(), position);
			}
		
		arrayLatLng = new ArrayList<LocationDetails>();
		LocationDetails latlog = new LocationDetails();
		latlog.setLatiDouble(pickupLatitude);
		latlog.setLogiDouble(pickupLogitude);
		String pickupLabel = getHalfAddress(getApplicationContext(),
				pickupLatitude, pickupLogitude);
		latlog.setAddressName(pickupLabel);
		latlog.setLocationName(trigger);
		latlog.setMode(mode);
		arrayLatLng.add(latlog);
		// add to SharedPreferences
		GpsUtil.addLocation(getApplicationContext(), latlog);
		// Toast.makeText(AddLocationActivity.this,
		// "Location save successfully..!", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(AddLocationActivity.this,ListLocationActivity.class);
		startActivity(intent);
		finish();
	}

	private class PlacesAutoAdapter extends ArrayAdapter<String> implements
			Filterable {
		private ArrayList<String> resultList;
		private ArrayList<String> recentPlaces = new ArrayList<String>();

		public PlacesAutoAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {

						if (stopSeaching == 1) {
							// Retrieve the autocomplete results.
							resultList = autocomplete(constraint.toString());
							for (int i = 0; i < recentPlaces.size(); i++) {
								if (recentPlaces.get(i).startsWith(
										constraint.toString()))
									resultList.add(recentPlaces.get(i));
							}
							// Assign the data to the FilterResults
							filterResults.values = resultList;
							filterResults.count = resultList.size();
						}

					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}

	/**
	 * auto complete search location function *
	 */
	// //
	private ArrayList<String> autocomplete(String input) {
		ArrayList<String> resultList = null;

		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {

			StringBuilder sb = new StringBuilder(
					"https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
							+ input.replace(" ", "%20") + "&sensor=true&key="
							+ GpsUtil.serverKey);
			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());

			// Load the results into a StringBuilder
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (Exception e) {
			Log.d("LOG_TAG", "Error processing Places API URL", e);
			return resultList;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		Log.e("prediction result", jsonResults.toString());

		try {
			// Create a JSON object hierarchy from the results
			JSONObject jsonObj = new JSONObject(jsonResults.toString());
			JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

			resultList = new ArrayList<String>();
			for (int i = 0; i < predsJsonArray.length(); i++) {
				resultList.add(predsJsonArray.getJSONObject(i).getString(
						"description"));
			}
		} catch (JSONException e) {
			Log.e("LOG_TAG", "Cannot process JSON results", e);
		}

		return resultList;
	}

	private void searchLocation() {
		String addressStr = null;

		addressStr = autotext_address.getText().toString();

		Geocoder geoCoder = new Geocoder(AddLocationActivity.this,
				Locale.getDefault());

		try {
			List<Address> addresses = geoCoder.getFromLocationName(addressStr,
					1);
			if (addresses.size() > 0) {
				searchLat = addresses.get(0).getLatitude();
				searchLon = addresses.get(0).getLongitude();
				Log.d("tag", "Latitude:" + searchLat);
				Log.d("tag", "Longitude:" + searchLon);
				pickupLatitude = searchLat;
				pickupLogitude = searchLon;
				// Showing the current location in Google Map
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(searchLat, searchLon), 17));
				// Zoom in the Google Map
				googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mapInitialize() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addConnectionCallbacks(this).addApi(LocationServices.API)
				// .addApi(Places.GEO_DATA_API)
				// .addApi(Places.PLACE_DETECTION_API)
				.build();
		mGoogleApiClient.connect();
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		googleMap.setOnCameraChangeListener(mCameraChangeListener);
		/*
		 * if (ActivityCompat.checkSelfPermission(this,
		 * Manifest.permission.ACCESS_FINE_LOCATION) !=
		 * PackageManager.PERMISSION_GRANTED &&
		 * ActivityCompat.checkSelfPermission(this,
		 * Manifest.permission.ACCESS_COARSE_LOCATION) !=
		 * PackageManager.PERMISSION_GRANTED) { return; }
		 */
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setZoomControlsEnabled(false);

		googleMap.animateCamera(CameraUpdateFactory.zoomIn());

		// Zoom out to zoom level 10, animating with a duration of 2 seconds.
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

		if (mLastLocation != null) {
			mLastLocation = LocationServices.FusedLocationApi
					.getLastLocation(mGoogleApiClient);

			setPickupLocation(mLastLocation);
			initCameraPosition(mLastLocation);
			System.err.println("" + mLastLocation.getLatitude()
					+ mLastLocation.getLongitude());
		}

	}

	private void setPickupLocation(Location location) {
		try {
			if (flagCurrentPosition == 1) {
				pickupLatitude = location.getLatitude();
				pickupLogitude = location.getLongitude();
			} else {
				LatLng center = googleMap.getCameraPosition().target;
				pickupLatitude = center.latitude;
				pickupLogitude = center.longitude;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initCameraPosition(Location location) {

		googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				location.getLatitude(), location.getLongitude()), 17));
		// Zoom in the Google Map
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
		System.err.println("" + location.getLatitude()
				+ location.getLongitude());
	}

	@Override
	public void onConnected(Bundle bundle) {
		/*
		 * if (ActivityCompat.checkSelfPermission(this,
		 * Manifest.permission.ACCESS_FINE_LOCATION) !=
		 * PackageManager.PERMISSION_GRANTED &&
		 * ActivityCompat.checkSelfPermission(this,
		 * Manifest.permission.ACCESS_COARSE_LOCATION) !=
		 * PackageManager.PERMISSION_GRANTED) { return; }
		 */
		mLastLocation = LocationServices.FusedLocationApi
				.getLastLocation(mGoogleApiClient);
		if (googleMap != null) {

			setPickupLocation(mLastLocation);
			initCameraPosition(mLastLocation);
		}

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("", "onStart fired ..............");
		mGoogleApiClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("", "onStop fired ..............");
		mGoogleApiClient.disconnect();
		Log.d("",
				"isConnected ...............: "
						+ mGoogleApiClient.isConnected());
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();
		if (mGoogleApiClient.isConnected()) {
			// startLocationUpdates();
			Log.d("", "Location update resumed .....................");
		}
	}

	private class MapCameraChangeListener implements
			GoogleMap.OnCameraChangeListener {

		private boolean isDisableListener = false;

		void disableListener(boolean isDisable) {
			isDisableListener = isDisable;
		}

		@Override
		public void onCameraChange(CameraPosition cameraPosition) {
			if (isFromAutoCompleteOnClick) {
				isFromAutoCompleteOnClick = false;
				return;
			}

			float value = cameraPosition.zoom;
			if (cameraPosition.zoom > currentZoom) {
				currentZoom = cameraPosition.zoom;
			} else if (cameraPosition.zoom < currentZoom) {
				currentZoom = cameraPosition.zoom;
			} else if (cameraPosition.zoom == currentZoom) {
				currentZoom = cameraPosition.zoom;
			}
			autotext_address.setText("Loading Location...");
			flagCurrentPosition = 2;
			pickupLatitude = cameraPosition.target.latitude;
			pickupLogitude = cameraPosition.target.longitude;

			stopSeaching = 2;
			Runnable mRunnable;
			Handler mHandler = new Handler();
			mRunnable = new Runnable() {
				@Override
				public void run() {
					// savePosition = new LatLng(pickupLatitude,
					// pickupLogitude);
					String pickupLabel = getHalfAddress(
							getApplicationContext(), pickupLatitude,
							pickupLogitude);
					autotext_address.setText(pickupLabel);

				}
			};
			mHandler.postDelayed(mRunnable, 200);
		}
	}

	public String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
		String strAdd = "";
		Geocoder geocoder = new Geocoder(AddLocationActivity.this,
				Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(LATITUDE,
					LONGITUDE, 1);
			if (addresses != null) {
				Address returnedAddress = addresses.get(0);
				StringBuilder strReturnedAddress = new StringBuilder("");

				for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
					strReturnedAddress
							.append(returnedAddress.getAddressLine(i)).append(
									"\n");
				}

				strAdd = strReturnedAddress.toString();

			} else {
				// Log.e("My Current loction address", "No Address returned!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// Log.e("My Current loction address", "Canont get Address!");
		}
		return strAdd;
	}

	public static String getHalfAddress(Context context, double LATITUDE,
			double LONGITUDE) {
		String strAdd = "", sub1 = null, sub2 = null, sub3 = null;
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		try {
			List<Address> addresses = geocoder.getFromLocation(LATITUDE,
					LONGITUDE, 1);
			if (addresses != null) {
				Address returnedAddress = addresses.get(0);

				sub1 = returnedAddress.getSubThoroughfare();
				sub2 = returnedAddress.getThoroughfare();
				sub3 = returnedAddress.getLocality();
				//sub3 = returnedAddress.getPostalCode();

				String address = sub1 + " " + sub2 + " " + sub3;

				try {
					strAdd = address.replace("null", "");
					if (sub1 == null && sub2 == null && sub3 == null) {
						strAdd = returnedAddress.getLocality();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return strAdd;
	}

	public void hideKeyboard(int input) {
		if (input == 1)// show keyboard
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		} else// hide keyboard
		{
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(autotext_address.getWindowToken(), 0);
		}
	}
}
