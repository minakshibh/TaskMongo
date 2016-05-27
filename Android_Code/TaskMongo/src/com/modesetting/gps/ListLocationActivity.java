package com.modesetting.gps;

import java.util.ArrayList;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.modesettings.activity.R;
import com.modesettings.model.LocationDetails;
import com.modesettings.util.TaskMongoAlarmReceiver;

public class ListLocationActivity extends Activity {
	public ArrayList<LocationDetails> arrayLatLng;
	private ListView listView;
	private TextView btnBack, btnAdd;
	private AlertDialog levelDialog = null;
	int getPosition = 0, getOtherPosition = 0;
	private String location = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_listlocation);

		setUI();
		setAddressAdapter();
		setOnClickListener();

	}

	private void setUI() {
		// TODO Auto-generated method stub
		listView = (ListView) findViewById(R.id.listView);
		btnBack = (TextView) findViewById(R.id.btnBack);
		btnAdd = (TextView) findViewById(R.id.btnAdd);

	}

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addLocation();
			}
		});
	}

	private void addLocation() {
		final CharSequence[] items = { " Home ", " Office ", " Other " };

		// Creating and Building the Dialog

		AlertDialog.Builder builder = new AlertDialog.Builder(
				ListLocationActivity.this);
		builder.setTitle("Select location name");
		builder.setSingleChoiceItems(items, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {

						switch (item) {
						case 0:
							gotoNext("home");
							levelDialog.dismiss();
							break;
						case 1:
							gotoNext("office");
							levelDialog.dismiss();
							break;
						case 2:
							gotoNext("other");
							levelDialog.dismiss();
							break;
						case 3:
							// Your code when 4th option seletced
							levelDialog.dismiss();
							break;

						}

					}
				});
		builder.setPositiveButton("Cancel", null);
		levelDialog = builder.create();
		levelDialog.show();
	}

	private void gotoNext(final String value) {
		boolean locationAlreadySave = false;
		getPosition = 0;
		arrayLatLng = GpsUtil.getLocations(getApplicationContext());
		if (arrayLatLng != null && arrayLatLng.size() > 0) {
			for (int i = 0; i < arrayLatLng.size(); i++) {

				if (value
						.equalsIgnoreCase(arrayLatLng.get(i).getLocationName())) {
					locationAlreadySave = true;
					getPosition = i;

				}
			}
			if (locationAlreadySave) {
				AlertDialog.Builder alert = new AlertDialog.Builder(
						ListLocationActivity.this);
				alert.setTitle("Location already saved for " + value);
				alert.setMessage("Do you want to change this location?");
				alert.setPositiveButton("No", null);
				alert.setNegativeButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,int which) {

								Intent mIntent = new Intent(ListLocationActivity.this,AddLocationActivity.class);
								mIntent.putExtra("locationName", arrayLatLng.get(getPosition).getLocationName());
								mIntent.putExtra("edit", "edit");
								mIntent.putExtra("position",getPosition );
								startActivity(mIntent);
								finish();
							}
						});
				alert.show();

			} else {

				if (value.equalsIgnoreCase("other")) {

					enterLocationType();
				} else {
					IntentNext(value);
				}
			}
		}

		else {
			if (value.equalsIgnoreCase("other")) {

				enterLocationType();
			} else {
				IntentNext(value);
			}
		}
	}

	private void enterLocationType() {
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(ListLocationActivity.this);
		View promptsView = li.inflate(R.layout.dailog_enterlocation, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				ListLocationActivity.this);

		alertDialogBuilder.setTitle("Enter loation name");
		// alertDialogBuilder.setMessage("Enter Password");
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInputpassword);

		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								location = userInput.getText().toString();

								if (location.equals("")) {
									GpsUtil.ToastMessage(
											ListLocationActivity.this,
											"Please enter location name");
								}

								else {

									boolean locationAlreadySave = false;
									getOtherPosition = 0;
									arrayLatLng = GpsUtil
											.getLocations(getApplicationContext());
									if (arrayLatLng != null
											&& arrayLatLng.size() > 0) {
										for (int i = 0; i < arrayLatLng.size(); i++) {

											if (location
													.equalsIgnoreCase(arrayLatLng
															.get(i)
															.getLocationName())) {
												locationAlreadySave = true;
												getOtherPosition = i;
											}
										}
										if (locationAlreadySave) {
											AlertDialog.Builder alert = new AlertDialog.Builder(
													ListLocationActivity.this);
											alert.setTitle("Location already saved for "
													+ location);
											alert.setMessage("Do you want to change this location?");
											alert.setPositiveButton("No", null);
											alert.setNegativeButton(
													"Yes",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {

														/*	GpsUtil.removeLocation(
																	getApplicationContext(),
																	getOtherPosition);
															IntentNext(location);*/
															Intent mIntent = new Intent(ListLocationActivity.this,AddLocationActivity.class);
															mIntent.putExtra("locationName", arrayLatLng.get(getOtherPosition).getLocationName());
															mIntent.putExtra("edit", "edit");
															mIntent.putExtra("position",getOtherPosition );
															startActivity(mIntent);
															finish();
														}
													});
											alert.show();

										} else {

											IntentNext(location);

										}
									} else {

										GpsUtil.hideKeyboard(ListLocationActivity.this);
										IntentNext(location);
									}
								}
							}
						}).setNegativeButton("Cancel", null);

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();

	}

	private void IntentNext(String value) {
		Intent mIntent = new Intent(ListLocationActivity.this,AddLocationActivity.class);
		mIntent.putExtra("locationName", value);
		startActivity(mIntent);
		finish();
	}

	private void setAddressAdapter() {
		// getting from SharedPreferences
		arrayLatLng = GpsUtil.getLocations(getApplicationContext());
		if (arrayLatLng != null) {
			ListAdapter adapter = new ListAdapter(getApplicationContext(),
					arrayLatLng);
			listView.setAdapter(adapter);
		}
	}

	public class ListAdapter extends BaseSwipeAdapter {
		private Context context;
		private TextView locationName, deleteLocation, latLng, locationType,
				edit_location;
		ArrayList<LocationDetails> arrayLatLng;
		private ImageView ruleMode;

		public ListAdapter(Context ctx, ArrayList<LocationDetails> arrayLatLng) {
			this.context = ctx;
			this.arrayLatLng = arrayLatLng;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return arrayLatLng.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return arrayLatLng.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public void fillValues(final int position, View convertView) {
			// TODO Auto-generated method stub

			LocationDetails location = arrayLatLng.get(position);

			locationName = (TextView) convertView.findViewById(R.id.ruleName);
			ruleMode = (ImageView) convertView.findViewById(R.id.mode);
			latLng = (TextView) convertView.findViewById(R.id.timings);
			deleteLocation = (TextView) convertView
					.findViewById(R.id.deleteRule);
			edit_location = (TextView) convertView.findViewById(R.id.editRule);
			locationType = (TextView) convertView.findViewById(R.id.days);
			/* enableRule = (TextView) convertView.findViewById(R.id.enable); */
			// transparentLayer = (RelativeLayout)
			// convertView.findViewById(R.id.transparentLayer);

			locationName.setText(location.getLocationName());
			latLng.setText(location.getLatiDouble() + " "
					+ location.getLogiDouble());
			latLng.setVisibility(View.GONE);
			locationType.setText(location.getAddressName());
			if (location.getMode() != null) {
				if (location.getMode().equals(
						TaskMongoAlarmReceiver.SILENT_MODE))
					ruleMode.setBackgroundResource(R.drawable.silent_icon);
				else if (location.getMode().equals(
						TaskMongoAlarmReceiver.NORMAL_MODE))
					ruleMode.setBackgroundResource(R.drawable.sound_icon);
				else if (location.getMode().equals(
						TaskMongoAlarmReceiver.VIBRATE_MODE))
					ruleMode.setBackgroundResource(R.drawable.vibrate_icon);
			}

			deleteLocation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					AlertDialog.Builder alert = new AlertDialog.Builder(
							ListLocationActivity.this);
					alert.setTitle("Delete this Location mongo?");
					alert.setMessage("Are you sure?");
					alert.setPositiveButton("No", null);
					alert.setNegativeButton("Yes",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									GpsUtil.removeLocation(getApplicationContext(), position);
									arrayLatLng.clear();
									arrayLatLng = GpsUtil
											.getLocations(getApplicationContext());
									if (arrayLatLng != null) {
										ListAdapter adapter = new ListAdapter(
												getApplicationContext(),
												arrayLatLng);
										listView.setAdapter(adapter);
									} else {
										finish();
									}

								}
							});
					alert.show();
				}
			});

			edit_location.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
				
					//IntentNext(arrayLatLng.get(position).getLocationName());
					
					Intent mIntent = new Intent(ListLocationActivity.this,AddLocationActivity.class);
					mIntent.putExtra("locationName", arrayLatLng.get(position).getLocationName());
					mIntent.putExtra("edit", "edit");
					mIntent.putExtra("position",position );
					startActivity(mIntent);
					finish();
					
				}
			});

			/*
			 * enableRule.setOnClickListener(new View.OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { // TODO Auto-generated
			 * method stub if(Boolean.valueOf(selectedRule.getIsEnabled())){
			 * selectedRule.setIsEnabled("false"); }else{
			 * selectedRule.setIsEnabled("true"); } notifyDataSetChanged();
			 * dbHandler.updateRule(selectedRule);
			 * Util.refreshAllAlarms(ListRulesActivity.this);
			 * 
			 * } });
			 */

		}

		@Override
		public View generateView(int arg0, ViewGroup arg1) {
			return LayoutInflater.from(ListLocationActivity.this).inflate(
					R.layout.location_row, null);

		}

		@Override
		public int getSwipeLayoutResourceId(int arg0) {
			// TODO Auto-generated method stub
			return R.id.swipe;
		}
	}

}