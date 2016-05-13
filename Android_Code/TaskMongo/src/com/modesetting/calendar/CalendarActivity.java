package com.modesetting.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.modesettings.activity.R;
import com.modesettings.activity.SettingsActivity;

@SuppressLint("NewApi")
public class CalendarActivity extends Activity {
	long getId = -1;
	SharedPreferences spref, sprefOnce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar);

		spref = getSharedPreferences("mongo", MODE_PRIVATE);
		sprefOnce = getSharedPreferences("sprefOnce", MODE_PRIVATE);
		getId = GetMaxID();
		if (sprefOnce.getString("once", "").equals("")) {
			Editor editor1 = sprefOnce.edit();
			editor1.putString("once", "" + getId);
			editor1.commit();

			Editor editor = spref.edit();
			editor.putLong("Id", getId);
			editor.commit();
		}
		System.err
				.println("ID=" + getId + " ShareID=" + spref.getLong("Id", 0));
		if (getId > spref.getLong("Id", 0)) {
			Editor editor = spref.edit();
			editor.putLong("Id", getId);
			editor.commit();
			customDailog("Do you want to save this event as a Mongo?");
		} else {
			finish();
		}

	}

	public void customDailog(String str) {
		final Dialog dialog = new Dialog(CalendarActivity.this);

		// setting custom layout to dialog
		dialog.setContentView(R.layout.custom_dialog);
		dialog.setCancelable(false);
		dialog.setTitle("Task Mongo");

		// adding text dynamically
		TextView txt = (TextView) dialog.findViewById(R.id.txt_dia);
		txt.setText(str);

		// adding button click event
		Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
		btn_no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
		btn_yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				try {

					// long id=GetMaxID();

					fetchEventData(getId);
					// System.err.println("ID="+getId);
				} catch (Exception e) {
					// customDailog(e.getMessage());
				}
			}
		});
		dialog.show();
	}

	/*
	 * private boolean mayRequestContacts() { if (Build.VERSION.SDK_INT <
	 * Build.VERSION_CODES.LOLLIPOP_MR1) { return true; } if
	 * (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
	 * { return true; } if (shouldShowRequestPermissionRationale(READ_CONTACTS))
	 * { Snackbar.make(mEmailView, R.string.permission_rationale,
	 * Snackbar.LENGTH_INDEFINITE) .setAction(android.R.string.ok, new
	 * View.OnClickListener() {
	 * 
	 * @Override
	 * 
	 * @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1) public void onClick(View v)
	 * { requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
	 * } }); } else { requestPermissions(new String[]{READ},
	 * REQUEST_READ_CONTACTS); } return false; }
	 */

	public long GetMaxID() {
		ContentResolver cr = getContentResolver();
		Uri caluri = CalendarContract.Events.CONTENT_URI;
		Uri local_uri = caluri;
		if (caluri == null) {
			local_uri = Uri.parse("content://com.android.calendar/events");
		}
		Cursor cursor = cr.query(local_uri,
				new String[] { "MAX(_id) as max_id" }, null, null, "_id");

		cursor.moveToFirst();
		long max_val = cursor.getLong(cursor.getColumnIndex("max_id"));
		return max_val + 1;

	}

	private void fetchEventData(long eventID) {

		String event_Title = null, event_Desc = null;
		Date event_Start = null, event_end = null;
		ContentResolver cr = getContentResolver();
		Uri caluri = CalendarContract.Events.CONTENT_URI;
		// Uri atteuri = CalendarContract.Attendees.CONTENT_URI;
		Cursor cur1;// , cur2;
		String all = null;
		String selection = "(" + Events._ID + " = ?)";
		String[] selectionArgs = new String[] { Long.toString(eventID - 1) };
		try {
			cur1 = cr.query(caluri, new String[] { Events.CALENDAR_ID,
					Events._ID, Events.TITLE, Events.DESCRIPTION,
					Events.DTSTART, Events.DTEND, Events.EVENT_LOCATION },
					selection, selectionArgs, null);

			if (cur1 != null) {
				while (cur1.moveToNext()) {
					event_Title = cur1.getString(cur1
							.getColumnIndex(Events.TITLE));
					event_Desc = cur1.getString(cur1
							.getColumnIndexOrThrow(Events.DESCRIPTION));
					event_Start = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTSTART)));
					event_end = new Date(cur1.getLong(cur1
							.getColumnIndex(Events.DTEND)));
					String event_loc = cur1.getString(cur1
							.getColumnIndex(Events.EVENT_LOCATION));
					/*
					 * String all_attendee = null; String all_Emails = null;
					 * 
					 * String cal_ID =
					 * cur1.getString(cur1.getColumnIndex(Events.CALENDAR_ID));
					 * String event_ID =
					 * cur1.getString(cur1.getColumnIndex(Events._ID));
					 */
					/*
					 * cur2 = cr.query(atteuri, new String[]{
					 * Attendees.ATTENDEE_NAME, Attendees.ATTENDEE_EMAIL } ,
					 * Attendees.EVENT_ID + "=" + eventID, null, null); if (cur2
					 * != null) { while (cur2.moveToNext()) { String
					 * attendee_name =
					 * cur2.getString(cur2.getColumnIndex(Attendees
					 * .ATTENDEE_NAME)); String attendee_Email =
					 * cur2.getString(cur2
					 * .getColumnIndex(Attendees.ATTENDEE_EMAIL));
					 * 
					 * all_attendee += "\n" + attendee_name; all_Emails += "\n"
					 * + attendee_Email; } cur2.close(); }
					 */
					all += "Event title: " + event_Title + "\n"
							+ "Event Description: " + event_Desc + "\n"
							+ "Event Start: " + event_Start + "\n"
							+ "Events End: " + event_end + "\n"
							+ "Event Location: " + event_loc;
					// + "\n" + "Attendees: " + "\n" + all_attendee + "\n"
					// + "Emails: " + "\n" + all_Emails + "\n";
				}
				cur1.close();

				goToNext("" + event_Title, "" + event_Desc, "" + event_Start,
						"" + event_end);
			}
			System.out.println("My log--------" + all);
			// Toast.makeText(DailogActivity.this, all,
			// Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void goToNext(String event_Title, String event_Desc,
			String event_Start, String event_end) {
		// TODO Auto-generated method stub
		String outStartDate = null;
		String outEndDate = null;
		String startDay = null;
		String inFormatter = "E MMM dd HH:mm:ss Z yyyy";// Event Start: Mon May
														// 02 05:30:00 IST 2016
		// String outFormatter= "yyyy-MM-dd hh:mm:ss a" ;
		String TimeFormatter = "hh:mm:ss";
		// String DateFormatter= "yyyy-MM-dd" ;
		String DayFormatter = "E";
		if (event_Start != null) {

			try {
				startDay = formateDateFromstring(inFormatter, DayFormatter, ""
						+ event_Start);
				outStartDate = formateDateFromstring(inFormatter,
						TimeFormatter, "" + event_Start);
				outEndDate = formateDateFromstring(inFormatter, TimeFormatter,
						"" + event_end);

				// getStartDate=formateDateFromstring(DateFormatter,TimeFormatter,""+outStartDate);
				// getEndDate=formateDateFromstring(DateFormatter,TimeFormatter,""+outEndDate);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		Intent mIntent = new Intent(CalendarActivity.this,
				SettingsActivity.class);
		mIntent.putExtra("trigger", "calendar");
		mIntent.putExtra("title", event_Title);
		mIntent.putExtra("start", outStartDate);
		mIntent.putExtra("end", outEndDate);
		mIntent.putExtra("startDay", startDay);
		startActivity(mIntent);
		finish();
	}

	public static String formateDateFromstring(String inputFormat,
			String outputFormat, String inputDate) {

		Date parsed = null;
		String outputDate = "";

		SimpleDateFormat df_input = new SimpleDateFormat(inputFormat,
				java.util.Locale.getDefault());
		SimpleDateFormat df_output = new SimpleDateFormat(outputFormat,
				java.util.Locale.getDefault());

		try {
			parsed = df_input.parse(inputDate);
			outputDate = df_output.format(parsed);

		} catch (Exception e) {
			// LOGE(TAG, "ParseException - dateFormat");
		}

		return outputDate;

	}
}
