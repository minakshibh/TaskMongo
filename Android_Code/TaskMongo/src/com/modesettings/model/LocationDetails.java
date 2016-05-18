package com.modesettings.model;

public class LocationDetails {

	public Double latiDouble,logiDouble;
	public String AddressName,LocationName,mode;



	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getLocationName() {
		return LocationName;
	}

	public void setLocationName(String locationName) {
		LocationName = locationName;
	}

	public String getAddressName() {
		return AddressName;
	}

	public void setAddressName(String addressName) {
		AddressName = addressName;
	}

	public Double getLatiDouble() {
		return latiDouble;
	}

	public void setLatiDouble(Double latiDouble) {
		this.latiDouble = latiDouble;
	}

	public Double getLogiDouble() {
		return logiDouble;
	}

	public void setLogiDouble(Double logiDouble) {
		this.logiDouble = logiDouble;
	}
	
	
}
