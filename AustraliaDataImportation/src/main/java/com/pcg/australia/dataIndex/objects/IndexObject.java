package com.pcg.australia.dataIndex.objects;

import java.util.Date;
import java.util.List;

public class IndexObject 
{
	//A generic string field supporting textual search
	private List<String> generalString;
	
	private Integer countID;
	private String eventID;
	private String eventName;
	private String address;
	
	//additional fields
	private String description;
	
	private List<String> dailyLiving;
	private List<String> allied_health_sc;
	private List<String> consumables_assistive_tech;
	private List<String> communityAndGovernment;

	private List<String> disability_type;
	private List<String> sensorySupports;
	private List<String> specialist_models;
	private List<String> stateProvidedIn;
		
	private String pc;
	private String fullPc;
	private String state;
	private String locality;
	
	/**
	 * Location
	 */
	private String longLat; //lat+" "+long;
	private String longtidue;
	private String latitude;
	
	private List<String> ageGroup;
		
	private Date dateOfIndex;
	private String starRating;
	
	private boolean NDISRegistered;
	private boolean logoRequired;

	public boolean getNDISRegistered() {
		return NDISRegistered;
	}
	public void setNDISRegistered(boolean nDISRegistered) {
		NDISRegistered = nDISRegistered;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getAllied_health_sc() {
		return allied_health_sc;
	}
	public void setAllied_health_sc(List<String> allied_health_sc) {
		this.allied_health_sc = allied_health_sc;
	}
	public List<String> getConsumables_assistive_tech() {
		return consumables_assistive_tech;
	}
	public void setConsumables_assistive_tech(
			List<String> consumables_assistive_tech) {
		this.consumables_assistive_tech = consumables_assistive_tech;
	}

	public List<String> getDisability_type() {
		return disability_type;
	}
	public void setDisability_type(List<String> disability_type) {
		this.disability_type = disability_type;
	}

	public List<String> getSpecialist_models() {
		return specialist_models;
	}
	public void setSpecialist_models(List<String> specialist_models) {
		this.specialist_models = specialist_models;
	}
	public Integer getCountID() {
		return countID;
	}
	public void setCountID(Integer countID) {
		this.countID = countID;
	}
	public String getEventID() {
		return eventID;
	}
	public void setEventID(String eventID) {
		this.eventID = eventID;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPc() {
		return pc;
	}
	public void setPc(String pc) {
		this.pc = pc;
	}
	public String getFullPc() {
		return fullPc;
	}
	public void setFullPc(String fullPc) {
		this.fullPc = fullPc;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	public List<String> getAgeGroup() {
		return ageGroup;
	}
	public void setAgeGroup(List<String> ageGroup) {
		this.ageGroup = ageGroup;
	}

	public Date getDateOfIndex() {
		return dateOfIndex;
	}
	public void setDateOfIndex(Date dateOfIndex) {
		this.dateOfIndex = dateOfIndex;
	}
	public String getStarRating() {
		return starRating;
	}
	public void setStarRating(String starRating) {
		this.starRating = starRating;
	}
	public String getLongLat() {
		return longLat;
	}
	public void setLongLat(String longLat) {
		this.longLat = longLat;
	}
	public String getLongtidue() {
		return longtidue;
	}
	public void setLongtidue(String longtidue) {
		this.longtidue = longtidue;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public List<String> getGeneralString() {
		return generalString;
	}
	public void setGeneralString(List<String> generalString) {
		this.generalString = generalString;
	}
	public List<String> getStateProvidedIn() {
		return stateProvidedIn;
	}
	public void setStateProvidedIn(List<String> stateProvidedIn) {
		this.stateProvidedIn = stateProvidedIn;
	}
	public List<String> getDailyLiving() {
		return dailyLiving;
	}
	public void setDailyLiving(List<String> dailyLiving) {
		this.dailyLiving = dailyLiving;
	}
	public List<String> getSensorySupports() {
		return sensorySupports;
	}
	public void setSensorySupports(List<String> sensorySupports) {
		this.sensorySupports = sensorySupports;
	}
	public List<String> getCommunityAndGovernment() {
		return communityAndGovernment;
	}
	public void setCommunityAndGovernment(List<String> communityAndGovernment) {
		this.communityAndGovernment = communityAndGovernment;
	}
	public boolean isLogoRequired() {
		return logoRequired;
	}
	public void setLogoRequired(boolean logoRequired) {
		this.logoRequired = logoRequired;
	}
}
