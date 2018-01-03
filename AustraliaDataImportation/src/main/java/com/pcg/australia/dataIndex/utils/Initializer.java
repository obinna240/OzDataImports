package com.pcg.australia.dataIndex.utils;

public class Initializer
{
	private String csvLocation;
	
	private String smtpHost;
	private String smtpPort;
	private String smtpEmailFrom;
	private String smtpEmailTo;
	private String smtpEmailSubject;
	
	private String solrCore;
	private String solrHost;
	private String solrPort;
	
	//this is the row number where the data commences. 
	//row number does not commence at 0
	private String recordNumber;
	
	public Initializer(String csvLocation, String smtpHost, String smtpPort, String smtpEmailFrom,
			String smtpEmailTo, String smtpEmailSubject, String solrCore, String solrHost, 
			String solrPort, String recordNumber)
	{
		this.csvLocation = csvLocation;
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.smtpEmailFrom = smtpEmailFrom;
		this.smtpEmailTo = smtpEmailTo;
		this.smtpEmailSubject = smtpEmailSubject;
		
		this.solrCore = solrCore;
		this.solrHost = solrHost;
		this.solrPort = solrPort;
		
		this.recordNumber = recordNumber;
		
	}
	


	

	public String getSmtpEmailFrom() {
		return smtpEmailFrom;
	}





	public void setSmtpEmailFrom(String smtpEmailFrom) {
		this.smtpEmailFrom = smtpEmailFrom;
	}





	public String getSmtpEmailTo() {
		return smtpEmailTo;
	}





	public void setSmtpEmailTo(String smtpEmailTo) {
		this.smtpEmailTo = smtpEmailTo;
	}





	public String getSmtpEmailSubject() {
		return smtpEmailSubject;
	}





	public void setSmtpEmailSubject(String smtpEmailSubject) {
		this.smtpEmailSubject = smtpEmailSubject;
	}





	public String getCsvLocation() {
		return csvLocation;
	}

	public void setCsvLocation(String csvLocation) {
		this.csvLocation = csvLocation;
	}

	public String getSolrCore() {
		return solrCore;
	}

	public void setSolrCore(String solrCore) {
		this.solrCore = solrCore;
	}





	public String getSmtpPort() {
		return smtpPort;
	}





	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}





	public String getSmtpHost() {
		return smtpHost;
	}





	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}





	public String getSolrHost() {
		return solrHost;
	}





	public void setSolrHost(String solrHost) {
		this.solrHost = solrHost;
	}





	public String getSolrPort() {
		return solrPort;
	}





	public void setSolrPort(String solrPort) {
		this.solrPort = solrPort;
	}





	public String getRecordNumber() {
		return recordNumber;
	}





	public void setRecordNumber(String recordNumber) {
		this.recordNumber = recordNumber;
	}
	
	
}
