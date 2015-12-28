package com.modesettings.model;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;

public class Rule implements Parcelable{

	private String startTime, endTime, mode, description, selectedDays, isEnabled = "true";
	private int id;
	private ArrayList<TimingsData> timingsData;
	
	public Rule(){
		
	}
	 public Rule(Parcel source) {
		 startTime = source.readString();
		 endTime = source.readString();
		 mode = source.readString();
		 description = source.readString();
		 selectedDays = source.readString();
		 isEnabled = source.readString();
		 id = source.readInt();
	}
	 	 
	public String getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(String isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getStartTime() {
		return startTime;
	}
	public String getSelectedDays() {
		return selectedDays;
	}
	public void setSelectedDays(String selectedDays) {
		this.selectedDays = selectedDays;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public ArrayList<TimingsData> getTimingsData() {
		return timingsData;
	}
	public void setTimingsData(ArrayList<TimingsData> timingsData) {
		this.timingsData = timingsData;
	}
	
	public static final Parcelable.Creator<Rule> CREATOR
	  = new Parcelable.Creator<Rule>() 
	{
	       public Rule createFromParcel(Parcel in) 
	       {
	           return new Rule(in);
	       }

	       public Rule[] newArray (int size) 
	       {
	           return new Rule[size];
	       }
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return this.hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(startTime);
		dest.writeString(endTime);
		dest.writeString(mode);
		dest.writeString(description);
		dest.writeString(selectedDays);
		dest.writeString(isEnabled);
		dest.writeInt(id);
	}
}
