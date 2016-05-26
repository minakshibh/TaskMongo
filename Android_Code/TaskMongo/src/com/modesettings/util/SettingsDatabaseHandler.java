package com.modesettings.util;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.modesettings.model.Rule;
import com.modesettings.model.TimingsData;

public class SettingsDatabaseHandler extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	@SuppressLint("SdCardPath")
	private static String DB_PATH = "/data/data/com.modesettings.activity/databases/";

	private static String DB_NAME = "ModeSettings_db.sqlite";

	private SQLiteDatabase myDataBase;

	private final Context myContext;

	private String TABLE_RULES = "Rules";
	private String TABLE_TIMINGS_DATA = "TimingsData";
	private String TABLE_ALARM_DATA = "AlarmData";
	
	// field for rules table
	private String Start_Time = "StartTime";
	private String End_Time = "EndTime";
	private String Mode = "Mode";
	private String Description = "Description";
	private String Id = "Id";
//	private String Days = "Days";
	private String isEnabled = "isEnabled";
	private String Selected_Days = "SelectedDays";
	private String Event_Id = "EventId";

	// field for timings data table
	private String Timings_Data_Id = "TimingsDataId";
	private String Type = "Type";
	private String Rule_Id = "RuleId";
	private String Day = "Day";
	private String Start_Timings = "StartTimings";
	private String End_Timings = "EndTimings";
	
	//field for AlarmData table
	private String Alarm_Data_Id = "AlarmDataId";
	
	SQLiteCursor cursor;

	/**
	 * Constructor Takes and keeps a reference of the passed context in order to
	 * access to the application assets and resources.
	 * 
	 * @param context
	 */

	public SettingsDatabaseHandler(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_RULES_TABLE = "CREATE TABLE if NOT Exists " + TABLE_RULES
				+ "(" + Id + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Start_Time + " TEXT," + End_Time + " TEXT,"
				+  Mode + " TEXT," + Description + " TEXT,"+ Selected_Days+ " TEXT," + Event_Id + " TEXT,"+ isEnabled +" TEXT)";

		String CREATE_TIMINGS_DATA_TABLE = "CREATE TABLE if NOT Exists " + TABLE_TIMINGS_DATA
				+ "(" + Timings_Data_Id + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Rule_Id + " INTEGER," + Day + " INTEGER,"
				+  Type + " TEXT," + Mode + " TEXT,"+ Start_Timings + " TEXT," + End_Timings + " TEXT)";
		
		String CREATE_TABLE_ALARM_DATA = "CREATE TABLE if NOT Exists " + TABLE_ALARM_DATA
				+ "(" + Alarm_Data_Id + " INTEGER)";
		
		db.execSQL(CREATE_RULES_TABLE);
		db.execSQL(CREATE_TIMINGS_DATA_TABLE);
		db.execSQL(CREATE_TABLE_ALARM_DATA);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void deleteAllRules() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_RULES, null, null);
		db.close();
		
		deleteAllTimingsData();
	}

	public void deleteAlarmData() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ALARM_DATA, null, null);
		db.close();
	}
	
	public void deleteAllTimingsData(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_TIMINGS_DATA, null, null);
		db.close();
		
		deleteAlarmData();
	}
	
	public void deleteTimingsData(int id ,SQLiteDatabase db){
		db.delete(TABLE_TIMINGS_DATA, Rule_Id +" = ? ", new String[] { String.valueOf(id) });
	}
	
	// update/enable/disable rule
	public void updateRule(Rule rule) {
		SQLiteDatabase db = this.getWritableDatabase();
//			String selectQuery = "SELECT  * FROM " + TABLE_RULES + " where "
//					+ Id + "=" + rule.getId();

			try {
				ContentValues values = new ContentValues();
				/*values.put(Id, rule.getId());
				values.put(Start_Time, rule.getStartTime());
				values.put(End_Time, rule.getEndTime());
				values.put(Mode, rule.getMode());
				values.put(Description, rule.getDescription());
				values.put(Selected_Days, rule.getSelectedDays());*/
				values.put(isEnabled, rule.getIsEnabled());
				
				
//				cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
//				if (cursor.moveToFirst()) {

					// updating row
					int a = db.update(TABLE_RULES, values, Id + " = ?",
							new String[] { String.valueOf(rule.getId()) });

//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		db.close();
	}

	// Save/Update rule
	public int saveRule(Rule rule, int ruleId) {
		
		SQLiteDatabase db = this.getWritableDatabase();

			try {
				ContentValues values = new ContentValues();
				values.put(Start_Time, rule.getStartTime());
				values.put(End_Time, rule.getEndTime());
				values.put(Mode, rule.getMode());
				values.put(Description, rule.getDescription());
				values.put(Selected_Days, rule.getSelectedDays());
				values.put(isEnabled, rule.getIsEnabled());
				values.put(Event_Id, rule.getEventID());
				
				if(ruleId!=-1){
					db.update(TABLE_RULES, values, Id + " = ? ", new String[] { String.valueOf(ruleId)});
					deleteTimingsData(ruleId, db);
				}else{
					// Insert rule
					ruleId = (int) db.insert(TABLE_RULES, null, values);
				}
				
				for(int i = 0; i<rule.getTimingsData().size(); i++){
					TimingsData timingsData = rule.getTimingsData().get(i);
					
					ContentValues value = new ContentValues();
					
					value.put(Type, timingsData.getType());
					value.put(Rule_Id, ruleId);
					value.put(Day, timingsData.getDay());
					value.put(Mode, timingsData.getMode());
					value.put(End_Timings, timingsData.getEndTimings());
					value.put(Start_Timings, timingsData.getTimings());
					
					// Insert rule data
					int dataId = (int) db.insert(TABLE_TIMINGS_DATA, null, value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		db.close();
		return ruleId;
	}
	
	// save alarm data
		public int saveAlarmData(int alarmDataId) {
			int ruleId = -1;
			
			SQLiteDatabase db = this.getWritableDatabase();

				try {
					ContentValues values = new ContentValues();
					values.put(Alarm_Data_Id, alarmDataId);
					
					// Insert alarm data
					ruleId = (int) db.insert(TABLE_ALARM_DATA, null, values);
					
								
				} catch (Exception e) {
					e.printStackTrace();
				}
			db.close();
			
			return ruleId;
		}
		
		
	// delete rule
	public void deleteRule(int ruleId) {
		SQLiteDatabase db = this.getWritableDatabase();
		try {
			// delete row
			int b = db.delete(TABLE_RULES, Id + " = ?",
					new String[] { String.valueOf(ruleId) });
			
			b = db.delete(TABLE_TIMINGS_DATA, Rule_Id + " = ?",
					new String[] { String.valueOf(ruleId) });

		} catch (Exception e) {
			e.printStackTrace();
		}
		db.close();
	}

	// get rule 
	public Rule getRule(int ruleId) {

		Rule rule = new Rule();

		String selectQuery;

		selectQuery = "SELECT  * FROM " + TABLE_RULES + " where " + Id + " = " + ruleId;

		SQLiteDatabase db = this.getReadableDatabase();

		try {
			cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
				
				do {

					rule.setId(cursor.getInt(cursor.getColumnIndex(Id)));
					rule.setStartTime(cursor.getString(cursor
							.getColumnIndex(Start_Time)));
					rule.setEndTime(cursor.getString(cursor
							.getColumnIndex(End_Time)));
					rule.setMode(cursor.getString(cursor.getColumnIndex(Mode)));
					rule.setDescription(cursor.getString(cursor
							.getColumnIndex(Description)));
					rule.setSelectedDays(cursor.getString(cursor.getColumnIndex(Selected_Days)));
					rule.setIsEnabled(cursor.getString(cursor.getColumnIndex(isEnabled)));
					

					String selectRuleDataQuery = "SELECT  * FROM " + TABLE_TIMINGS_DATA+" where "+Rule_Id+" = "+rule.getId();
					SQLiteCursor subCursor = (SQLiteCursor) db.rawQuery(selectRuleDataQuery, null);
					
					ArrayList<TimingsData> timingsDataList = new ArrayList<TimingsData>();
					
					if (subCursor.moveToFirst()) {
						do {
							TimingsData timingsData = new TimingsData();
							
							timingsData.setRuleId(subCursor.getInt(subCursor.getColumnIndex(Rule_Id)));
							timingsData.setTimingsDataId(subCursor.getInt(subCursor.getColumnIndex(Timings_Data_Id)));
							timingsData.setDay(subCursor.getInt(subCursor.getColumnIndex(Day)));
							timingsData.setType(subCursor.getString(subCursor
									.getColumnIndex(Type)));
							timingsData.setTimings(subCursor.getString(subCursor
									.getColumnIndex(Start_Timings)));
							timingsData.setEndTimings(subCursor.getString(subCursor
									.getColumnIndex(End_Timings)));
							timingsData.setMode(subCursor.getString(subCursor
									.getColumnIndex(Mode)));
										
							timingsDataList.add(timingsData);
						} while (subCursor.moveToNext());
						
						subCursor.getWindow().clear();
						subCursor.close();
					}
					
					rule.setTimingsData(timingsDataList);
				} while (cursor.moveToNext());
			}

			cursor.getWindow().clear();
			cursor.close();
			// close inserting data from database
			db.close();
			// return rule list
			return rule;

		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.getWindow().clear();
				cursor.close();
			}

			db.close();
			return rule;
		}
	}
//get event id by rule

		public Rule getRuleId(String eventId) {

			Rule rule = new Rule();

			String selectQuery;

			selectQuery = "SELECT  * FROM " + TABLE_RULES + " WHERE " + Event_Id + "=" +"'" +eventId.trim()+"'";//+ eventId;

			SQLiteDatabase db = this.getReadableDatabase();

			try {
				cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
				if (cursor.moveToFirst()) {
					
					do {

						rule.setId(cursor.getInt(cursor.getColumnIndex(Id)));
						rule.setStartTime(cursor.getString(cursor
								.getColumnIndex(Start_Time)));
						rule.setEndTime(cursor.getString(cursor
								.getColumnIndex(End_Time)));
						rule.setMode(cursor.getString(cursor.getColumnIndex(Mode)));
						rule.setDescription(cursor.getString(cursor
								.getColumnIndex(Description)));
						rule.setSelectedDays(cursor.getString(cursor.getColumnIndex(Selected_Days)));
						rule.setEventID(cursor.getString(cursor.getColumnIndex(Event_Id)));
						rule.setIsEnabled(cursor.getString(cursor.getColumnIndex(isEnabled)));
						
						

						String selectRuleDataQuery = "SELECT  * FROM " + TABLE_TIMINGS_DATA+" where "+Rule_Id+" = "+rule.getId();
						SQLiteCursor subCursor = (SQLiteCursor) db.rawQuery(selectRuleDataQuery, null);
						
						ArrayList<TimingsData> timingsDataList = new ArrayList<TimingsData>();
						
						if (subCursor.moveToFirst()) {
							do {
								TimingsData timingsData = new TimingsData();
								
								timingsData.setRuleId(subCursor.getInt(subCursor.getColumnIndex(Rule_Id)));
								timingsData.setTimingsDataId(subCursor.getInt(subCursor.getColumnIndex(Timings_Data_Id)));
								timingsData.setDay(subCursor.getInt(subCursor.getColumnIndex(Day)));
								timingsData.setType(subCursor.getString(subCursor
										.getColumnIndex(Type)));
								timingsData.setTimings(subCursor.getString(subCursor
										.getColumnIndex(Start_Timings)));
								timingsData.setEndTimings(subCursor.getString(subCursor
										.getColumnIndex(End_Timings)));
								timingsData.setMode(subCursor.getString(subCursor
										.getColumnIndex(Mode)));
											
								timingsDataList.add(timingsData);
							} while (subCursor.moveToNext());
							
							subCursor.getWindow().clear();
							subCursor.close();
						}
						
						rule.setTimingsData(timingsDataList);
					} while (cursor.moveToNext());
				}

				cursor.getWindow().clear();
				cursor.close();
				// close inserting data from database
				db.close();
				// return rule list
				return rule;

			} catch (Exception e) {
				e.printStackTrace();
				if (cursor != null) {
					cursor.getWindow().clear();
					cursor.close();
				}

				db.close();
				return rule;
			}
		}

	
	//get timings data for specific day
	public ArrayList<TimingsData> getTimingsDataForSpecificDay(int day) {

		ArrayList<TimingsData> timingsDataList = new ArrayList<TimingsData>();
		
		String selectQuery;

//		select * from checklists  c left join people p on p.People_id = c.People_ID_CREATE where p.Is_Active=1
//				 and c.[start_date]='2015-06-18'  order by c.DATE_CREATE
				 
		selectQuery = "SELECT  * FROM " + TABLE_TIMINGS_DATA + " t LEFT JOIN "+ TABLE_RULES +" r ON "+ "r."+Id+
				"="+"t."+Rule_Id+" where r."+isEnabled+"= 'true' AND t." + Day + " = " + day +" ORDER BY t."+Start_Timings+" ASC ";

		SQLiteDatabase db = this.getReadableDatabase();

		try {
			cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
						do {
							TimingsData timingsData = new TimingsData();
							
							timingsData.setRuleId(cursor.getInt(cursor.getColumnIndex(Rule_Id)));
							timingsData.setTimingsDataId(cursor.getInt(cursor.getColumnIndex(Timings_Data_Id)));
							timingsData.setDay(cursor.getInt(cursor.getColumnIndex(Day)));
							timingsData.setType(cursor.getString(cursor
									.getColumnIndex(Type)));
							timingsData.setTimings(cursor.getString(cursor
									.getColumnIndex(Start_Timings)));
							timingsData.setEndTimings(cursor.getString(cursor
									.getColumnIndex(End_Timings)));
							timingsData.setMode(cursor.getString(cursor
									.getColumnIndex(Mode)));
							
							timingsDataList.add(timingsData);
						
				} while (cursor.moveToNext());
			}

			cursor.getWindow().clear();
			cursor.close();
			// close inserting data from database
			db.close();
			// return rule data list
			return timingsDataList;

		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.getWindow().clear();
				cursor.close();
			}

			db.close();
			return timingsDataList;
		}
	}

	
	private String getDay(int day){
    	switch(day){
    	case Calendar.SUNDAY:
    		return "Sun";
    	case Calendar.MONDAY:
    		return "Mon";
    	case Calendar.TUESDAY:
    		return "Tue";
    	case Calendar.WEDNESDAY:
    		return "Wed";
    	case Calendar.THURSDAY:
    		return "Thur";
    	case Calendar.FRIDAY:
    		return "Fri";
    	case Calendar.SATURDAY:
    		return "Sat ";
    	default:
    		return "";
    	}
    }
//	  if(day.equals("Sunday")){ return Calendar.SUNDAY; }else
//	  if(day.equals("Monday")){ return Calendar.MONDAY; }else
//	  if(day.equals("Tuesday")){ return Calendar.TUESDAY; }else
//	  if(day.equals("Wednesday")){ return Calendar.WEDNESDAY; }else
//	  if(day.equals("Thursday")){ return Calendar.THURSDAY; }else
//	  if(day.equals("Friday")){ return Calendar.FRIDAY; }else { return
//	  Calendar.SATURDAY; }
	  	
	// get rule for specific day
	public ArrayList<Rule> getRulesForSpecificDay(int day){


		ArrayList<Rule> ruleList = new ArrayList<Rule>();
		
		String selectQuery;

		selectQuery = "SELECT  * FROM " + TABLE_RULES + " where " + isEnabled + "="+ "'true' AND " + Selected_Days + " LIKE '%" +getDay(day) +"%'";

		SQLiteDatabase db = this.getReadableDatabase();

		try {
			cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
						do {
							Rule rule = new Rule();
							
							rule.setId(cursor.getInt(cursor.getColumnIndex(Id)));
							rule.setStartTime(cursor.getString(cursor
									.getColumnIndex(Start_Time)));
							rule.setEndTime(cursor.getString(cursor
									.getColumnIndex(End_Time)));
							rule.setMode(cursor.getString(cursor.getColumnIndex(Mode)));
							rule.setDescription(cursor.getString(cursor
									.getColumnIndex(Description)));
							rule.setSelectedDays(cursor.getString(cursor.getColumnIndex(Selected_Days)));
							rule.setIsEnabled(cursor.getString(cursor.getColumnIndex(isEnabled)));
							
							ruleList.add(rule);
						
				} while (cursor.moveToNext());
			}

			cursor.getWindow().clear();
			cursor.close();
			// close inserting data from database
			db.close();
			// return rule data list
			return ruleList;

		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.getWindow().clear();
				cursor.close();
			}

			db.close();
			return ruleList;
		}
	}
	
	// get alarm data
		public ArrayList<Integer> getAlarmData(){
			ArrayList<Integer> alarmData = new ArrayList<Integer>();
			String selectQuery;
			selectQuery = "SELECT  * FROM " + TABLE_ALARM_DATA;

			SQLiteDatabase db = this.getReadableDatabase();

			try {
				cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
				if (cursor.moveToFirst()) {
							do {
								alarmData.add(cursor.getInt(cursor.getColumnIndex(Alarm_Data_Id)));
							} while (cursor.moveToNext());
				}

				cursor.getWindow().clear();
				cursor.close();
				// close inserting data from database
				db.close();
				// return rule data list
				return alarmData;

			} catch (Exception e) {
				e.printStackTrace();
				if (cursor != null) {
					cursor.getWindow().clear();
					cursor.close();
				}

				db.close();
				return alarmData;
			}
		}
		
		
	
	// get rule list
	public ArrayList<Rule> getRules() {

		ArrayList<Rule> ruleList = new ArrayList<Rule>();

		String selectQuery;

		selectQuery = "SELECT  * FROM " + TABLE_RULES;

		SQLiteDatabase db = this.getReadableDatabase();

		try {
			cursor = (SQLiteCursor) db.rawQuery(selectQuery, null);
			if (cursor.moveToFirst()) {
				
				do {
					Rule rule = new Rule();
					rule.setId(cursor.getInt(cursor.getColumnIndex(Id)));
					rule.setStartTime(cursor.getString(cursor
							.getColumnIndex(Start_Time)));
					rule.setEndTime(cursor.getString(cursor
							.getColumnIndex(End_Time)));
					rule.setMode(cursor.getString(cursor.getColumnIndex(Mode)));
					rule.setDescription(cursor.getString(cursor
							.getColumnIndex(Description)));
					rule.setSelectedDays(cursor.getString(cursor.getColumnIndex(Selected_Days)));
					rule.setEventID(cursor.getString(cursor.getColumnIndex(Event_Id)));
					rule.setIsEnabled(cursor.getString(cursor.getColumnIndex(isEnabled)));
					

					String selectRuleDataQuery = "SELECT  * FROM " + TABLE_TIMINGS_DATA+" where "+Rule_Id+" = "+rule.getId();
					SQLiteCursor subCursor = (SQLiteCursor) db.rawQuery(selectRuleDataQuery, null);
					
					ArrayList<TimingsData> timingsDataList = new ArrayList<TimingsData>();
					
					if (subCursor.moveToFirst()) {
						do {
							TimingsData timingsData = new TimingsData();
							
							timingsData.setRuleId(subCursor.getInt(subCursor.getColumnIndex(Rule_Id)));
							timingsData.setTimingsDataId(subCursor.getInt(subCursor.getColumnIndex(Timings_Data_Id)));
							timingsData.setDay(subCursor.getInt(subCursor.getColumnIndex(Day)));
							timingsData.setType(subCursor.getString(subCursor
									.getColumnIndex(Type)));
							timingsData.setTimings(subCursor.getString(subCursor
									.getColumnIndex(Start_Timings)));
							timingsData.setEndTimings(subCursor.getString(subCursor
									.getColumnIndex(End_Timings)));
							timingsData.setMode(subCursor.getString(subCursor
									.getColumnIndex(Mode)));
										
							timingsDataList.add(timingsData);
						} while (subCursor.moveToNext());
						
						subCursor.getWindow().clear();
						subCursor.close();
					}
					
					rule.setTimingsData(timingsDataList);
					ruleList.add(rule);
					
				} while (cursor.moveToNext());
			}

			cursor.getWindow().clear();
			cursor.close();
			// close inserting data from database
			db.close();
			// return rule list
			return ruleList;

		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) {
				cursor.getWindow().clear();
				cursor.close();
			}

			db.close();
			return ruleList;
		}
	}
		
	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READWRITE);

	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();

		super.close();
	}

}