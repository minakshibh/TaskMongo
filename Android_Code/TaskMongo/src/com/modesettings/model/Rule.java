package com.modesettings.model;

import java.util.ArrayList;

public class Rule {

	private String startTime, endTime, mode, description, selectedDays;
	private int id;
	private ArrayList<TimingsData> timingsData;
	
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
}
