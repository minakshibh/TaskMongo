package com.modesetting.gps;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.modesettings.model.LocationDetails;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class GpsUtil {
	static Context context;
	static public String network_error = "Please check your internet connection, try again";
	static int statusCode;
	static String share_location = "location";
	public final static String serverKey = "AIzaSyB5PeH8UZgvq_oHdKmKQvb8WjlGchnTfO4";
	public static final String PREFS_NAME = "NKDROID_APP";
	public static final String Loation = "loation";

	public GpsUtil() {
		super();
	}

	static public void ToastMessage(Context ctx, String str) {
		context = ctx;
		Toast.makeText(ctx, str, 1000).show();
	}
	static public  void hideKeyboard(Context cxt) {
		context=cxt;
	    InputMethodManager inputManager = (InputMethodManager) cxt.getSystemService(Context.INPUT_METHOD_SERVICE);
	    
	    // check if no view has focus:
	    View view = ((Activity) cxt).getCurrentFocus();
	    if (view != null) {
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	// This four methods are used for maintaining favorites.
	public static void saveLocations(Context context,
			List<LocationDetails> locations) {
		SharedPreferences settings;
		Editor editor;

		settings = context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE);
		editor = settings.edit();

		Gson gson = new Gson();
		String jsonFavorites = gson.toJson(locations);
		editor.putString(Loation, jsonFavorites);
		editor.commit();
	}

	public static void addLocation(Context context, LocationDetails location) {
		List<LocationDetails> favorites = getLocations(context);
		if (favorites == null)
			favorites = new ArrayList<LocationDetails>();
		favorites.add(location);
		saveLocations(context, favorites);
	}

	public static void removeLocation(Context context, int position) {
		ArrayList<LocationDetails> loations = getLocations(context);
		if (loations != null) {
			// loations.remove(position ,loation);
			loations.remove(position);
			saveLocations(context, loations);
		}
	}

	public static ArrayList<LocationDetails> getLocations(Context context) {
		SharedPreferences settings;
		List<LocationDetails> loations;

		settings = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);

		if (settings.contains(Loation)) {
			String jsonFavorites = settings.getString(Loation, null);
			Gson gson = new Gson();
			LocationDetails[] favoriteItems = gson.fromJson(jsonFavorites,
					LocationDetails[].class);

			loations = Arrays.asList(favoriteItems);
			loations = new ArrayList<LocationDetails>(loations);
		} else
			return null;

		return (ArrayList<LocationDetails>) loations;
	}
	  public static void fontAwesomeApply(Context act,TextView...params)
	    {
	        Typeface typeFace_icon= Typeface.createFromAsset(act.getAssets(), "fontawesome-webfont.ttf");
	        for (TextView tv : params) {
	            tv.setTypeface(typeFace_icon);
	        }
	    }

}