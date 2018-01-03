package com.pcg.australia.dataIndex.impl;


import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.opencsv.CSVReader;
import com.pcg.australia.dataIndex.interfaces.IndexerInterface;
import com.pcg.australia.dataIndex.objects.IndexObject;
import com.pcg.australia.dataIndex.utils.Initializer;


@Component
public class TestIndexer implements IndexerInterface
{
	@Autowired
	Initializer initializer;
	
	static Logger logger = LogManager.getLogger(TestIndexer.class);
	
	@Value("${csv.location}") 
	String csvLocation;
	@Value("${solr.host}")
	String solrHost;
	@Value("${solr.port}")
	String solrPort;
	@Value("${solr.core}")
	String solrCore = "australiaCore";
	
	@Override
	public void index() 
	{
		
		parseAndIndexCSV();
	}
	
	
	private void parseAndIndexCSV() 
	{
						
		Integer recNums = 0;
		if(StringUtils.isBlank(csvLocation))
		{
		
			System.out.println("ERROR ... \'csvLocation\' cannot be found");
			System.out.println("Looking in \'c:/australiaIndexer/config\' for the csv ...");
			logger.info("ERROR-- \'csvLocation\' cannot be found");
			logger.info("Looking in \'c:/australiaIndexer/config\' for the csv ...");
			csvLocation = "c:/australiaIndexer/config/AustraliaDatafile.csv";//"c:/australiaIndexer/config/AustraliaDataFile.csv"; 
			
		}
		
		Integer recordNumber = 2;
		CSVParser parser = null;;

		try 
		{
			//set these parameters if they are null on the 
			//assumption that these values are the same
			//in the properties file
			if(StringUtils.isBlank(solrHost))
			{
				solrHost = "localhost";
			}
			if(StringUtils.isBlank(solrPort))
			{
				solrPort = "8983";
			}
			if(StringUtils.isBlank(solrCore))
			{
				solrCore = "australiaCore";
			}
								
			String solrUrl = "http://"+solrHost +":"+solrPort+"/solr/"+solrCore;
			SolrClient solr = new HttpSolrClient.Builder(solrUrl).build();
			
			//String filePath = ;//new ClassPathResource(csvLocation).getFile().getAbsolutePath();
			
			parser = CSVParser.parse(new File(csvLocation), Charset.defaultCharset(), CSVFormat.EXCEL);
			List<CSVRecord> records = parser.getRecords();
			
			logger.info("Total number of records === "+ records.size()+" ===");
			

			
			for (CSVRecord csvRecord : records)
			{
				Long recNum = csvRecord.getRecordNumber();
				recNums = recNum.intValue();
				if(recNums>=recordNumber)
				{
					System.out.println("Getting CSV record "+recNums+" ...");
					System.out.println("Creating a new index object ...");
					logger.info("Creating a new index object ...");
					
										
					String eventId = csvRecord.get(0);
					System.out.println("Getting index id ..."+eventId);
					
					logger.info("Getting index id ..."+eventId);
					if(StringUtils.isBlank(eventId))
					{
						System.out.println("ERROR -- Skipping, Not indexing "+eventId);
						logger.info("ERROR -- Skipping, Not indexing "+eventId);
					}
					else
					{
						SolrInputDocument sDoc = new SolrInputDocument();
						
						//create solr document here
						IndexObject indexObject = new IndexObject();
						
						indexObject.setCountID(recNums);
						sDoc.addField("countId", recNums);
						
						indexObject.setEventID(eventId);
						sDoc.addField("eventId", eventId);
						sDoc.addField("id", eventId);
						
						List<String> _text_ = new ArrayList<String>();
						
						String eventName = csvRecord.get(1);
						eventName= StringUtils.isNotBlank(eventName)? StringUtils.normalizeSpace(eventName):"";
						indexObject.setEventName(eventName);
						sDoc.addField("eventName", eventName);
						_text_.add(eventName);
						
						String description = csvRecord.get(2);
						description = StringUtils.isNotBlank(description)? StringUtils.normalizeSpace(description):"";
						indexObject.setDescription(description);
						_text_.add(description);
						sDoc.addField("description", description);
						
						String address = csvRecord.get(3);
						address = StringUtils.isNotBlank(address)? StringUtils.normalizeSpace(address):"";
						indexObject.setAddress(address);
						_text_.add(address);
						sDoc.addField("address", address);
											
						String pc = csvRecord.get(4);
						String locality = csvRecord.get(7);
						String state= csvRecord.get(8);
						
						pc = StringUtils.isNotBlank(pc)? StringUtils.normalizeSpace(pc):"";
						indexObject.setPc(pc);
						_text_.add(pc);
						sDoc.addField("pc", pc);
						
						state = StringUtils.isNotBlank(state)? StringUtils.normalizeSpace(state):"";
						indexObject.setState(state);
						_text_.add(state);
						sDoc.addField("state", state);
						
						locality = StringUtils.isNotBlank(locality)? StringUtils.normalizeSpace(locality):"";
						indexObject.setLocality(locality);
						_text_.add(locality);
						sDoc.addField("locality", locality);
						
						String fullPC = (StringUtils.isNotBlank(pc) && StringUtils.isNotBlank(state))?state+pc:"";
						indexObject.setFullPc(fullPC);
						_text_.add(fullPC);
						sDoc.addField("fullPC",fullPC);
						
						String lat = csvRecord.get(5); 
						
						String longt = csvRecord.get(6); 
						
						String latlong = "";
						if(StringUtils.isNotBlank(lat))
						{
							lat = StringUtils.normalizeSpace(lat);
							indexObject.setLatitude(lat);
							sDoc.addField("latitude",lat);
							latlong = lat+", ";
						}
						
						if(StringUtils.isNotBlank(longt))
						{
							longt = StringUtils.normalizeSpace(longt);
							indexObject.setLongtidue(longt);
							sDoc.addField("longitude",longt);
							latlong = latlong+longt;
							indexObject.setLongLat(latlong);
							sDoc.addField("geoLocation",latlong);
							
						}
						

						
						List<String> ageRange = new ArrayList<String>();
						String AG_Adolescence_School_Leaver = csvRecord.get(9);
						String AG_adults = csvRecord.get(10);
						
						String AG_Early_Intervention_06 = csvRecord.get(11);
						String AG_school_aged = csvRecord.get(12);
						String AG_seniors = csvRecord.get(13);
						
						if(StringUtils.isNotBlank(AG_Adolescence_School_Leaver))
						{
							AG_Adolescence_School_Leaver = StringUtils.normalizeSpace(AG_Adolescence_School_Leaver);
							AG_Adolescence_School_Leaver = (StringUtils.equalsIgnoreCase(AG_Adolescence_School_Leaver, "X"))?"1":"";
							ageRange.add(AG_Adolescence_School_Leaver);
							_text_.add("Adolescence");
							_text_.add("School leaver");
						}
						if(StringUtils.isNotBlank(AG_adults))
						{
							AG_adults = StringUtils.normalizeSpace(AG_adults);
							AG_adults = (StringUtils.equalsIgnoreCase(AG_adults, "X"))?"2":"";
							ageRange.add(AG_adults);
							_text_.add("adult");
							_text_.add("adults");
						}
						
						if(StringUtils.isNotBlank(AG_Early_Intervention_06))
						{
							AG_Early_Intervention_06 = StringUtils.normalizeSpace(AG_Early_Intervention_06);
							AG_Early_Intervention_06 = (StringUtils.equalsIgnoreCase(AG_Early_Intervention_06, "X"))?"3":"";
							ageRange.add(AG_Early_Intervention_06);
							_text_.add("Early intervention");
							
						}
						
						if(StringUtils.isNotBlank(AG_school_aged))
						{
							AG_school_aged = StringUtils.normalizeSpace(AG_school_aged);
							AG_school_aged = (StringUtils.equalsIgnoreCase(AG_school_aged, "X"))?"4":"";
							ageRange.add(AG_school_aged);
							_text_.add("school aged");
							
						}
						
						if(StringUtils.isNotBlank(AG_seniors))
						{
							AG_seniors = StringUtils.normalizeSpace(AG_seniors);
							AG_seniors = (StringUtils.equalsIgnoreCase(AG_seniors, "X"))?"5":"";
							ageRange.add(AG_seniors);
							_text_.add("Seniors");
							
						}
						
						indexObject.setAgeGroup(ageRange);
						sDoc.addField("ageGroup",ageRange);
						
						//Allied Health
						List<String> alliedHealth = new ArrayList<String>();
						
						String AS_adl = csvRecord.get(14);
						if(StringUtils.isNotBlank(AS_adl))
						{
							AS_adl= StringUtils.normalizeSpace(AS_adl);
							
							AS_adl = (StringUtils.equalsIgnoreCase(AS_adl, "X"))?"1":"";
							alliedHealth.add(AS_adl);
							_text_.add("Assessment: Activities of Daily Living");
							_text_.add("Allied Health/Therapies");
							_text_.add("Activities of Daily Living");
						}
						
						String AS_assistive_tech = csvRecord.get(15);
						if(StringUtils.isNotBlank(AS_assistive_tech))
						{
							AS_assistive_tech= StringUtils.normalizeSpace(AS_assistive_tech);
							
							AS_assistive_tech = (StringUtils.equalsIgnoreCase(AS_assistive_tech, "X"))?"2":"";
							alliedHealth.add(AS_assistive_tech);
							_text_.add("Allied Health/Therapies");
							_text_.add("Assessment: Assistive Technology");
							_text_.add("Assistive Technology");
							
						}
						
						String AS_dietician = csvRecord.get(16);
						if(StringUtils.isNotBlank(AS_dietician))
						{
							AS_dietician = StringUtils.normalizeSpace(AS_dietician);
							
							AS_dietician = (StringUtils.equalsIgnoreCase(AS_dietician, "X"))?"3":"";
							alliedHealth.add(AS_dietician);
							_text_.add("Allied Health/Therapies");
							_text_.add("Assessment: Dietician");
							_text_.add("Dietician");
							
						}
						String AS_driving = csvRecord.get(17);
						if(StringUtils.isNotBlank(AS_driving))
						{
							AS_driving = StringUtils.normalizeSpace(AS_driving);
							
							AS_driving = (StringUtils.equalsIgnoreCase(AS_driving, "X"))?"4":"";
							alliedHealth.add(AS_driving);
							_text_.add("Allied Health/Therapies");
							_text_.add("Assessment: Driving");
							_text_.add("Driving");
							
						}
						String AS_home_mod = csvRecord.get(18);
						if(StringUtils.isNotBlank(AS_home_mod))
						{
							AS_home_mod = StringUtils.normalizeSpace(AS_home_mod);
							
							AS_home_mod = (StringUtils.equalsIgnoreCase(AS_home_mod, "X"))?"5":"";
							alliedHealth.add(AS_home_mod);
							_text_.add("Allied Health/Therapies");  
							_text_.add("Assessment: Home Modification");
							_text_.add("Home Modification");
							
						}
						String AS_vehicle_mod = csvRecord.get(19);
						if(StringUtils.isNotBlank(AS_vehicle_mod))  
						{
							AS_vehicle_mod = StringUtils.normalizeSpace(AS_vehicle_mod);
							
							AS_vehicle_mod = (StringUtils.equalsIgnoreCase(AS_vehicle_mod, "X"))?"6":"";
							alliedHealth.add(AS_vehicle_mod);
							_text_.add("Allied Health/Therapies");
							_text_.add("Assessment: Vehicle Modification");
							_text_.add("Vehicle Modification");
							
						}
						
						String AS_chiropractor = csvRecord.get(20);
						if(StringUtils.isNotBlank(AS_chiropractor))
						{
							AS_chiropractor = StringUtils.normalizeSpace(AS_chiropractor);
							
							AS_chiropractor = (StringUtils.equalsIgnoreCase(AS_chiropractor, "X"))?"7":"";
							alliedHealth.add(AS_chiropractor);
							_text_.add("Allied Health/Therapies");
							_text_.add("Chiropractor");
													
						}
						
						String AS_continence_nurse = csvRecord.get(21);
						if(StringUtils.isNotBlank(AS_continence_nurse))
						{
							AS_continence_nurse = StringUtils.normalizeSpace(AS_continence_nurse);
							AS_continence_nurse = (StringUtils.equalsIgnoreCase(AS_continence_nurse, "X"))?"8":"";
							alliedHealth.add(AS_continence_nurse);
							_text_.add("Allied Health/Therapies");
							_text_.add("Continence Nurse");
							
						}
						
						
						String AS_noAss_dietician = csvRecord.get(22);
						if(StringUtils.isNotBlank(AS_noAss_dietician))
						{
							AS_noAss_dietician = StringUtils.normalizeSpace(AS_noAss_dietician);
							
							AS_noAss_dietician = (StringUtils.equalsIgnoreCase(AS_noAss_dietician, "X"))?"9":"";
							alliedHealth.add(AS_noAss_dietician);
							_text_.add("Allied Health/Therapies");
							_text_.add("Dietician");
							
						}
													
						String AS_noAss_ex_physio = csvRecord.get(23);
						if(StringUtils.isNotBlank(AS_noAss_ex_physio))
						{
							AS_noAss_ex_physio = StringUtils.normalizeSpace(AS_noAss_ex_physio);
							
							AS_noAss_ex_physio = (StringUtils.equalsIgnoreCase(AS_noAss_ex_physio, "X"))?"10":"";
							alliedHealth.add(AS_noAss_ex_physio);
							_text_.add("Allied Health/Therapies");
							_text_.add("Allied Health/Therapies - Exercise Physiologist");
							_text_.add("Exercise Physiologist");
							
						}
						
						String AS_noAss_multi_disc = csvRecord.get(24);
						if(StringUtils.isNotBlank(AS_noAss_multi_disc))
						{
							AS_noAss_multi_disc = StringUtils.normalizeSpace(AS_noAss_multi_disc);
							
							AS_noAss_multi_disc = (StringUtils.equalsIgnoreCase(AS_noAss_multi_disc, "X"))?"11":"";
							alliedHealth.add(AS_noAss_multi_disc);
							_text_.add("Allied Health/Therapies - Multi-disciplinary");
							_text_.add("Allied Health/Therapies");
						
							
						}
						
						String AS_noAss_nursing = csvRecord.get(25);
						if(StringUtils.isNotBlank(AS_noAss_nursing))
						{
							AS_noAss_nursing = StringUtils.normalizeSpace(AS_noAss_nursing);
							
							AS_noAss_nursing = (StringUtils.equalsIgnoreCase(AS_noAss_nursing, "X"))?"12":"";
							alliedHealth.add(AS_noAss_nursing);
							_text_.add("Allied Health/Therapies - Nursing");
							_text_.add("Nursing");
							_text_.add("Allied Health/Therapies");
							
						}
						
						String AS_noAss_occ_therapy = csvRecord.get(26);
						if(StringUtils.isNotBlank(AS_noAss_occ_therapy))
						{
							AS_noAss_occ_therapy = StringUtils.normalizeSpace(AS_noAss_occ_therapy);
							
							AS_noAss_occ_therapy = (StringUtils.equalsIgnoreCase(AS_noAss_occ_therapy, "X"))?"13":"";
							alliedHealth.add(AS_noAss_occ_therapy);
							_text_.add("Occupational Therapy");
							_text_.add("Allied Health/Therapies - Occupational Therapy");
							_text_.add("Allied Health/Therapies");
							
						}
						
						String AS_noAss_physio = csvRecord.get(27);
						if(StringUtils.isNotBlank(AS_noAss_physio))
						{
							AS_noAss_physio= StringUtils.normalizeSpace(AS_noAss_physio);
							
							AS_noAss_physio = (StringUtils.equalsIgnoreCase(AS_noAss_physio, "X"))?"14":"";
							alliedHealth.add(AS_noAss_physio);
							_text_.add("Allied Health/Therapies - Physiotherapy");
							_text_.add("Allied Health/Therapies");
							_text_.add("Physiotherapy");
							
						}
						
						String AS_noAss_podiatry = csvRecord.get(28);
						if(StringUtils.isNotBlank(AS_noAss_podiatry))
						{
							AS_noAss_podiatry = StringUtils.normalizeSpace(AS_noAss_podiatry);
							
							AS_noAss_podiatry = (StringUtils.equalsIgnoreCase(AS_noAss_podiatry, "X"))?"15":"";
							alliedHealth.add(AS_noAss_podiatry);
							_text_.add("Allied Health/Therapies - Podiatry");
							_text_.add("Podiatry");
							_text_.add("Allied Health/Therapies");				
						}
						
						String AS_noAss_psych = csvRecord.get(29);
						if(StringUtils.isNotBlank(AS_noAss_psych))
						{
							AS_noAss_psych = StringUtils.normalizeSpace(AS_noAss_psych);
							
							AS_noAss_psych = (StringUtils.equalsIgnoreCase(AS_noAss_psych, "X"))?"16":"";
							alliedHealth.add(AS_noAss_psych);
							_text_.add("Psychologist");
							_text_.add("Allied Health/Therapies - Psychologist");
							_text_.add("Allied Health/Therapies");						
						}
						
						String AS_noAss_sp_path = csvRecord.get(30);
						if(StringUtils.isNotBlank(AS_noAss_sp_path))
						{
							AS_noAss_sp_path = StringUtils.normalizeSpace(AS_noAss_sp_path);
							
							AS_noAss_sp_path = (StringUtils.equalsIgnoreCase(AS_noAss_sp_path, "X"))?"17":"";
							alliedHealth.add(AS_noAss_sp_path);
							_text_.add("Allied Health/Therapies - Speech Pathology");
							_text_.add("Speech Pathology");
							_text_.add("Allied Health/Therapies");				
						}
						
						String AS_noAss_supp_cord = csvRecord.get(31);
						if(StringUtils.isNotBlank(AS_noAss_supp_cord))
						{
							AS_noAss_supp_cord = StringUtils.normalizeSpace(AS_noAss_supp_cord);
							
							AS_noAss_supp_cord = (StringUtils.equalsIgnoreCase(AS_noAss_supp_cord, "X"))?"18":"";
							alliedHealth.add(AS_noAss_supp_cord);
							_text_.add("Allied Health/Therapies - Support Coordination");
							_text_.add("Support Coordination");
							_text_.add("Allied Health/Therapies");	
													
						}
																		
						indexObject.setAllied_health_sc(alliedHealth);
						sDoc.addField("alliedHealth",alliedHealth); 
						
						List<String> consumables_assistive_tech = new ArrayList<String>();
						
						String astech = csvRecord.get(32);
						if(StringUtils.isNotBlank(astech))
						{
							astech = StringUtils.normalizeSpace(astech);
							
							astech = (StringUtils.equalsIgnoreCase(astech, "X"))?"1":"";
							consumables_assistive_tech.add(astech);
							_text_.add("Assistive Technology Technician");
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String cgiver = csvRecord.get(33);
						if(StringUtils.isNotBlank(cgiver))
						{
							cgiver = StringUtils.normalizeSpace(cgiver);
							
							cgiver = (StringUtils.equalsIgnoreCase(cgiver, "X"))?"2":"";
							consumables_assistive_tech.add(cgiver);
							_text_.add("Caregiver Personal Protective Equipment");
							_text_.add("PPE");
							_text_.add("Consumables/Assistive Technology");
										
						}
						
						String chairs = csvRecord.get(34);
						if(StringUtils.isNotBlank(cgiver))
						{
							chairs= StringUtils.normalizeSpace(chairs);
							
							chairs = (StringUtils.equalsIgnoreCase(chairs, "X"))?"3":"";
							consumables_assistive_tech.add(chairs);
							
							_text_.add("Chairs");
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String comaids = csvRecord.get(35);
						if(StringUtils.isNotBlank(comaids))
						{
							comaids= StringUtils.normalizeSpace(comaids);
							
							comaids = (StringUtils.equalsIgnoreCase(comaids, "X"))?"4":"";
							consumables_assistive_tech.add(comaids);
							_text_.add("Communication Aids");
							_text_.add("Communication Technology");
							_text_.add("Consumables/Assistive Technology");
							
						}
						
						String continence = csvRecord.get(36);
						if(StringUtils.isNotBlank(continence))
						{
							continence= StringUtils.normalizeSpace(continence);
							
							continence = (StringUtils.equalsIgnoreCase(continence, "X"))?"5":"";
							consumables_assistive_tech.add(continence);
							_text_.add("Continence Items");
							_text_.add("bowel");
							_text_.add("bladder");
							_text_.add("bleeding");
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String cons_diet = csvRecord.get(37);
						if(StringUtils.isNotBlank(cons_diet))
						{
							cons_diet= StringUtils.normalizeSpace(cons_diet);
							
							cons_diet= (StringUtils.equalsIgnoreCase(cons_diet, "X"))?"6":"";
							consumables_assistive_tech.add(cons_diet);
							_text_.add("Dietary Equipment");
							_text_.add("PEG");
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String cons_hoist_beds = csvRecord.get(38);
						if(StringUtils.isNotBlank(cons_hoist_beds))
						{
							cons_hoist_beds= StringUtils.normalizeSpace(cons_hoist_beds);
							
							cons_hoist_beds= (StringUtils.equalsIgnoreCase(cons_hoist_beds, "X"))?"7":"";
							consumables_assistive_tech.add(cons_hoist_beds);
							_text_.add("Hoists");
							_text_.add("Beds");
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String home_mod_tech = csvRecord.get(39);
						if(StringUtils.isNotBlank(home_mod_tech))
						{
							home_mod_tech= StringUtils.normalizeSpace(home_mod_tech);
							
							home_mod_tech= (StringUtils.equalsIgnoreCase(home_mod_tech, "X"))?"8":"";
							consumables_assistive_tech.add(home_mod_tech);
							_text_.add("Home Modification Technician");
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String house_mod_item = csvRecord.get(40);
						if(StringUtils.isNotBlank(house_mod_item))
						{
							house_mod_item= StringUtils.normalizeSpace(house_mod_item);
							
							house_mod_item= (StringUtils.equalsIgnoreCase(house_mod_item, "X"))?"9":"";
							consumables_assistive_tech.add(house_mod_item);
							_text_.add("Household modification items");
							_text_.add("rails");
							_text_.add("ramps");
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String mobAids = csvRecord.get(41);
						if(StringUtils.isNotBlank(mobAids))
						{
							mobAids= StringUtils.normalizeSpace(mobAids);
							
							mobAids= (StringUtils.equalsIgnoreCase(mobAids, "X"))?"10":"";
							consumables_assistive_tech.add(mobAids);
							_text_.add("Mobility Aids");
							_text_.add("wheelchairs");
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String orthotics = csvRecord.get(42);
						if(StringUtils.isNotBlank(orthotics))
						{
							orthotics= StringUtils.normalizeSpace(orthotics);
							
							orthotics= (StringUtils.equalsIgnoreCase(orthotics, "X"))?"11":"";
							consumables_assistive_tech.add(orthotics);
							_text_.add("orthotics");
							_text_.add("splints");
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String sensory = csvRecord.get(43);
						if(StringUtils.isNotBlank(sensory))
						{
							sensory= StringUtils.normalizeSpace(sensory);
							
							sensory= (StringUtils.equalsIgnoreCase(sensory, "X"))?"12":"";
							consumables_assistive_tech.add(sensory);
							_text_.add("sensory items");
							_text_.add("weighted blanket");
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String specClo = csvRecord.get(44);
						if(StringUtils.isNotBlank(specClo))
						{
							specClo= StringUtils.normalizeSpace(specClo);
							
							specClo= (StringUtils.equalsIgnoreCase(specClo, "X"))?"13":"";
							consumables_assistive_tech.add(specClo);
							_text_.add("specialized clothing");
													
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						String vechItems = csvRecord.get(45);
						if(StringUtils.isNotBlank(vechItems))
						{
							vechItems= StringUtils.normalizeSpace(vechItems);
							
							vechItems= (StringUtils.equalsIgnoreCase(vechItems, "X"))?"14":"";
							consumables_assistive_tech.add(vechItems);
							_text_.add("vehicle items");
													
							_text_.add("Consumables/Assistive Technology");
													
						}
						String vechMod = csvRecord.get(46);
						if(StringUtils.isNotBlank(vechMod))
						{
							vechMod= StringUtils.normalizeSpace(vechMod);
							
							vechMod= (StringUtils.equalsIgnoreCase(vechMod, "X"))?"15":"";
							consumables_assistive_tech.add(vechMod);
							_text_.add("vehicle Modification Technician");
							
							
							_text_.add("Consumables/Assistive Technology");
													
						}
						
						indexObject.setConsumables_assistive_tech(consumables_assistive_tech);
						sDoc.addField("consumables",consumables_assistive_tech);
						
						
						List<String> dailyLiving = new ArrayList<String>();
						
						String csa = csvRecord.get(47); //Daily Living / Community Access - Full Day Programs
						if(StringUtils.isNotBlank(csa))
						{
							csa= StringUtils.normalizeSpace(csa);
							
							csa= (StringUtils.equalsIgnoreCase(csa, "X"))?"1":"";
							dailyLiving.add(csa);
												
							_text_.add("Daily Living / Community Access");
							_text_.add("Full Day Programs");
													
						}
						
						String csart = csvRecord.get(49); //Daily Living / Community Access - Special Interest Activities - Sport
						if(StringUtils.isNotBlank(csart))
						{
							csart= StringUtils.normalizeSpace(csart);
							
							csart= (StringUtils.equalsIgnoreCase(csart, "X"))?"2":"";
							dailyLiving.add(csart);
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities - Sport");
							_text_.add("Sport");
																			
						}
						

						String s_participation = csvRecord.get(50); //Daily Living / Community Access - Special Interest Activities - Swimming/ Surfing
						if(StringUtils.isNotBlank(s_participation ))
						{
							s_participation = StringUtils.normalizeSpace(s_participation );
							
							s_participation = (StringUtils.equalsIgnoreCase(s_participation , "X"))?"3":"";
							dailyLiving.add(s_participation );
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities - Swimming/ Surfing");
							_text_.add("Swimming/ Surfing");
													
						}
						
						String csauthMed = csvRecord.get(51); //Daily Living / Community Access - Special Interest Activities - Art /Craft
						if(StringUtils.isNotBlank(csauthMed))
						{
							csauthMed = StringUtils.normalizeSpace(csauthMed);
							
							csauthMed = (StringUtils.equalsIgnoreCase(csauthMed , "X"))?"4":"";
							dailyLiving.add(csauthMed);
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities - Art /Craft");
							
							_text_.add("Art/Craft");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities - Pre- Employment (preparation)
						String csbathing = csvRecord.get(52);
						if(StringUtils.isNotBlank(csbathing))
						{
							csbathing = StringUtils.normalizeSpace(csbathing);
							
							csbathing = (StringUtils.equalsIgnoreCase(csbathing , "X"))?"5":"";
							dailyLiving.add(csbathing);
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities - Pre- Employment (preparation)");
							_text_.add("Pre- Employment (preparation)");					
						}
						
						
						//Daily Living / Community Access - Special Interest Activities - Music/ Drama Theatre
						String dlcas = csvRecord.get(53);
						if(StringUtils.isNotBlank(dlcas))
						{
							dlcas = StringUtils.normalizeSpace(dlcas);
							
							dlcas = (StringUtils.equalsIgnoreCase(dlcas , "X"))?"6":"";
							dailyLiving.add(dlcas);
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities - Music/ Drama Theatre");
							_text_.add("Music/ Drama Theatre");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities -  Independent Living (Skill development)
						String cscamps = csvRecord.get(54);
						if(StringUtils.isNotBlank(cscamps))
						{
							cscamps= StringUtils.normalizeSpace(cscamps);
							
							cscamps= (StringUtils.equalsIgnoreCase(cscamps , "X"))?"7":"";
							dailyLiving.add(cscamps);
							_text_.add("Daily Living / Community Access");
							_text_.add("Independent Living (Skill development)");
							_text_.add("Special Interest Activities -  Independent Living (Skill development)");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities  - Animals / Riding for the Disabled
						String cscleaning = csvRecord.get(55);
						if(StringUtils.isNotBlank(cscleaning ))
						{
							cscleaning = StringUtils.normalizeSpace(cscleaning );
							
							cscleaning = (StringUtils.equalsIgnoreCase(cscleaning  , "X"))?"8":"";
							dailyLiving.add(cscleaning);
							_text_.add("Daily Living / Community Access");
							_text_.add("Animals / Riding for the Disabled");
							_text_.add("Special Interest Activities  - Animals / Riding for the Disabled");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities  - Computers
						String cscomputers = csvRecord.get(56);
						if(StringUtils.isNotBlank(cscomputers))
						{
							cscomputers = StringUtils.normalizeSpace(cscomputers);
							
							cscomputers = (StringUtils.equalsIgnoreCase(cscomputers , "X"))?"9":"";
							dailyLiving.add(cscomputers);
							_text_.add("Computers");
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities  - Fishing
						String csconsumables = csvRecord.get(57);
						if(StringUtils.isNotBlank(csconsumables))
						{
							csconsumables = StringUtils.normalizeSpace(csconsumables);
							
							csconsumables= (StringUtils.equalsIgnoreCase(csconsumables, "X"))?"10":"";
							dailyLiving.add(csconsumables);
							_text_.add("Fishing");
							_text_.add("Daily Living / Community Access");
							_text_.add("Special Interest Activities");
													
						}
						
						//Daily Living / Community Access - Special Interest Activities  - Service Clubs
						String cscooking = csvRecord.get(58);
						if(StringUtils.isNotBlank(cscooking))
						{
							cscooking = StringUtils.normalizeSpace(cscooking);
							
							cscooking= (StringUtils.equalsIgnoreCase(cscooking, "X"))?"11":"";
							dailyLiving.add(cscooking);
							_text_.add("Service Clubs");
							_text_.add("Special Interest Activities");
							_text_.add("Daily Living / Community Access");
													
						}
						
						
						//Daily Living / Community Access - Special Interest Activities  - Volunteering
						String cscordsupp = csvRecord.get(59);
						if(StringUtils.isNotBlank(cscordsupp))
						{
							cscordsupp = StringUtils.normalizeSpace(cscordsupp);
							
							cscordsupp= (StringUtils.equalsIgnoreCase(cscordsupp, "X"))?"12":"";
							dailyLiving.add(cscordsupp);
							_text_.add("Volunteering");
							_text_.add("Special Interest Activities");
							_text_.add("Daily Living / Community Access");
													
						}
						
						String csdp = csvRecord.get(60);
						if(StringUtils.isNotBlank(csdp))
						{
							csdp = StringUtils.normalizeSpace(csdp);
							
							csdp= (StringUtils.equalsIgnoreCase(csdp, "X"))?"13":"";
							dailyLiving.add(csdp);
							_text_.add("Camps");
							_text_.add("Special Interest Activities");
							_text_.add("Daily Living / Community Access");
													
						}
						
						//Daily Living / Community Access - Home and Personal Care - General care and home support
						String csfindJob = csvRecord.get(62);
						if(StringUtils.isNotBlank(csfindJob))
						{
							csfindJob= StringUtils.normalizeSpace(csfindJob);
							
							csfindJob= (StringUtils.equalsIgnoreCase(csfindJob, "X"))?"14":"";
							dailyLiving.add(csfindJob);
							_text_.add("Home and Personal Care");
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
													
						}
					
						//Daily Living / Community Access - Home and Personal Care - PEG
						String gdlc = csvRecord.get(63);
						if(StringUtils.isNotBlank(gdlc))
						{
							gdlc= StringUtils.normalizeSpace(gdlc);
							
							gdlc= (StringUtils.equalsIgnoreCase(gdlc, "X"))?"15":"";
							dailyLiving.add(gdlc);
							_text_.add("PEG");
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
													
						}
						
						//Daily Living / Community Access - Home and Personal Care - Wound Skin Care Dressings
						String hh = csvRecord.get(64);
						if(StringUtils.isNotBlank(hh))
						{
							hh= StringUtils.normalizeSpace(hh);
							
							hh= (StringUtils.equalsIgnoreCase(hh, "X"))?"16":"";
							dailyLiving.add(hh);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Wound Skin Care Dressings");
						}
						
						//Daily Living / Community Access - Home and Personal Care - Catheter
						String idlSki = csvRecord.get(65);
						if(StringUtils.isNotBlank(idlSki))
						{
							idlSki= StringUtils.normalizeSpace(idlSki);
							
							idlSki= (StringUtils.equalsIgnoreCase(idlSki, "X"))?"17":"";
							dailyLiving.add(idlSki);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Catheter");
													
						}
						
						String ihw = csvRecord.get(66);
						if(StringUtils.isNotBlank(ihw))
						{
							ihw= StringUtils.normalizeSpace(ihw);
							
							ihw= (StringUtils.equalsIgnoreCase(ihw, "X"))?"18":"";
							dailyLiving.add(ihw);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Skin Integrity");
													
						}
						
						String ilea = csvRecord.get(67);
						if(StringUtils.isNotBlank(ilea))
						{
							ilea= StringUtils.normalizeSpace(ilea);
							
							ilea= (StringUtils.equalsIgnoreCase(ilea, "X"))?"19":"";
							dailyLiving.add(ilea);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Dressings");
													
						}
						
						String ilc = csvRecord.get(68);
						if(StringUtils.isNotBlank(ilc))
						{
							ilc= StringUtils.normalizeSpace(ilc);
							
							ilc= (StringUtils.equalsIgnoreCase(ilc, "X"))?"20":"";
							dailyLiving.add(ilc);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Use of Hoists");					
						}
						
						//Daily Living / Community Access - Home and Personal Care - Administration of Medication
						String ilarr = csvRecord.get(69);
						if(StringUtils.isNotBlank(ilarr ))
						{
							ilarr = StringUtils.normalizeSpace(ilarr );
							
							ilarr = (StringUtils.equalsIgnoreCase(ilarr , "X"))?"21":"";
							dailyLiving.add(ilarr);
							_text_.add("General care and home support");
							_text_.add("Daily Living / Community Access");
							_text_.add("Administration of Medication");
													
						}
						
						//Daily Living / Community Access - Domestic Assistance - Meal Preparation
						String irelationships= csvRecord.get(71);
						if(StringUtils.isNotBlank(irelationships))
						{
							irelationships = StringUtils.normalizeSpace(irelationships);
							
							irelationships= (StringUtils.equalsIgnoreCase(irelationships , "X"))?"22":"";
							dailyLiving.add(irelationships);
							_text_.add("Domestic Assistance");
							_text_.add("Daily Living / Community Access");
							_text_.add("Meal preparation");
													
						}
						
						String isocart = csvRecord.get(72);
						if(StringUtils.isNotBlank(isocart))
						{
							isocart = StringUtils.normalizeSpace(isocart);
							
							isocart= (StringUtils.equalsIgnoreCase(isocart, "X"))?"23":"";
							dailyLiving.add(isocart);
							_text_.add("Domestic Assistance");
							_text_.add("Daily Living / Community Access");
							_text_.add("House Duties");
													
						}
						
						String ilifeSkills = csvRecord.get(73);
						if(StringUtils.isNotBlank(ilifeSkills))
						{
							ilifeSkills = StringUtils.normalizeSpace(ilifeSkills);
							
							ilifeSkills= (StringUtils.equalsIgnoreCase(ilifeSkills, "X"))?"24":"";
							dailyLiving.add(ilifeSkills);
							_text_.add("Domestic Assistance");
							_text_.add("Daily Living / Community Access");
							_text_.add("Gardening Assistance");
													
						}
						
						indexObject.setDailyLiving(dailyLiving);
						sDoc.addField("dailyLiving",dailyLiving);
		
									
						
						//Disability Type - Challenging Behaviours 
						List<String> disabilityType = new ArrayList<String>();
						String aus = csvRecord.get(74);
						if(StringUtils.isNotBlank(aus))
						{
							aus = StringUtils.normalizeSpace(aus);
							if(StringUtils.equalsIgnoreCase(aus, "X"))
							{
								disabilityType.add("1");
								_text_.add("Challenging Behaviours");
								_text_.add("Disability");
							}
									
							
						}
						
						String binjury = csvRecord.get(75);
						if(StringUtils.isNotBlank(binjury))
						{
							binjury = StringUtils.normalizeSpace(binjury);
							if(StringUtils.equalsIgnoreCase(binjury, "X"))
							{
								disabilityType.add("2");
								_text_.add("Feeding");
								_text_.add("Disability");
								_text_.add("Swallowing");
								_text_.add("Cleft Palate");
							}
									
							
						}
						String cpalsy= csvRecord.get(76);
						if(StringUtils.isNotBlank(cpalsy))
						{
							cpalsy = StringUtils.normalizeSpace(cpalsy);
							if(StringUtils.equalsIgnoreCase(cpalsy, "X"))
							{
								disabilityType.add("3");
								_text_.add("Hearing Impairment");
								_text_.add("Disability");
							}
									
							
						}
						String cbehv = csvRecord.get(77);
						if(StringUtils.isNotBlank(cbehv))
						{
							cbehv= StringUtils.normalizeSpace(cbehv);
							if(StringUtils.equalsIgnoreCase(cbehv, "X"))
							{
								disabilityType.add("4");
								_text_.add("Intellectual Disability");
								_text_.add("Developmental Delay");
								_text_.add("Disability");
							}
									
							
						}
						String fswal = csvRecord.get(78);
						if(StringUtils.isNotBlank(fswal))
						{
							fswal= StringUtils.normalizeSpace(fswal);
							if(StringUtils.equalsIgnoreCase(fswal, "X"))
							{
								disabilityType.add("5");
								_text_.add("Orthopaedic");
								_text_.add("Trauma");
								
								_text_.add("Disability");
							}
									
							
						}
						String hearingImpairment = csvRecord.get(79);
						if(StringUtils.isNotBlank(hearingImpairment))
						{
							hearingImpairment= StringUtils.normalizeSpace(hearingImpairment);
							if(StringUtils.equalsIgnoreCase(hearingImpairment, "X"))
							{
								disabilityType.add("6");
								_text_.add("Other");
								_text_.add("Disability");
								
							}
							
							
						}
						
						String intDisa = csvRecord.get(80);
						if(StringUtils.isNotBlank(intDisa))
						{
							intDisa= StringUtils.normalizeSpace(intDisa);
							if(StringUtils.equalsIgnoreCase(intDisa, "X"))
							{
								disabilityType.add("7");
								_text_.add("MND");
								_text_.add("MS");
								_text_.add("Motor neurone disease");
								_text_.add("Multiple Sclerosis");
								_text_.add("Parkinsons");
								_text_.add("Progressive Disorders");
								_text_.add("Disability");
							}
									
							
						}
						
						String otrauma = csvRecord.get(81);
						if(StringUtils.isNotBlank(otrauma))
						{
							otrauma= StringUtils.normalizeSpace(otrauma);
							if(StringUtils.equalsIgnoreCase(otrauma, "X"))
							{
								disabilityType.add("8");
								_text_.add("Mental Health");
								_text_.add("Psychological");
								_text_.add("Disability");
							}
									
							
						}
						String other = csvRecord.get(82);//Spinal Cord Injury
						if(StringUtils.isNotBlank(other))
						{
							other= StringUtils.normalizeSpace(other);
							if(StringUtils.equalsIgnoreCase(other, "X"))
							{
								disabilityType.add("9");
								_text_.add("Spinal Cord Injury");
								_text_.add("Disability");
							}
									
							
						}
						String progdis = csvRecord.get(83);
						if(StringUtils.isNotBlank(progdis))
						{
							progdis= StringUtils.normalizeSpace(progdis);
							if(StringUtils.equalsIgnoreCase(progdis, "X"))
							{
								disabilityType.add("10");
								_text_.add("Vision");
								_text_.add("Disability");
							}
									
							
						}
						indexObject.setDisability_type(disabilityType);
						sDoc.addField("disabilityType",disabilityType);
						
						
						List<String> sensorySupports = new ArrayList<String>();
												
						String ideaf= csvRecord.get(85);
						if(StringUtils.isNotBlank(ideaf))
						{
							ideaf = StringUtils.normalizeSpace(ideaf);
							if(StringUtils.equalsIgnoreCase(ideaf, "X"))
							{
								sensorySupports.add("1");
								_text_.add("Sensory Supports / Intermediaries - Interpreter: Auslan");
								
								_text_.add("Interpreter: Auslan");
							}
													
						}
						
						
						String ideafB= csvRecord.get(86);
						if(StringUtils.isNotBlank(ideafB))
						{
							ideafB= StringUtils.normalizeSpace(ideafB);
							if(StringUtils.equalsIgnoreCase(ideafB, "X"))
							{
								sensorySupports.add("2");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Signed English");
							}
									
							
						}
						
						String vie= csvRecord.get(87);
						if(StringUtils.isNotBlank(vie))
						{
							vie= StringUtils.normalizeSpace(vie);
							if(StringUtils.equalsIgnoreCase(vie, "X"))
							{
								sensorySupports.add("3");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Vietnamese");
							}
									
							
						}
						
						String vie2= csvRecord.get(88);
						if(StringUtils.isNotBlank(vie2))
						{
							vie2= StringUtils.normalizeSpace(vie2);
							if(StringUtils.equalsIgnoreCase(vie2, "X"))
							{
								sensorySupports.add("4");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Mandarin");
							}
									
							
						}
						
						String vie3= csvRecord.get(89);
						if(StringUtils.isNotBlank(vie3))
						{
							vie3= StringUtils.normalizeSpace(vie3);
							if(StringUtils.equalsIgnoreCase(vie3, "X"))
							{
								sensorySupports.add("5");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Arabic");
							}
									
							
						}
						
						String vie4= csvRecord.get(90);
						if(StringUtils.isNotBlank(vie4))
						{
							vie4= StringUtils.normalizeSpace(vie4);
							if(StringUtils.equalsIgnoreCase(vie4, "X"))
							{
								sensorySupports.add("6");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Greek");
							}
													
						}
						
						
						String vie5= csvRecord.get(91);
						if(StringUtils.isNotBlank(vie5))
						{
							vie5= StringUtils.normalizeSpace(vie5);
							if(StringUtils.equalsIgnoreCase(vie5, "X"))
							{
								sensorySupports.add("7");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Hindi");
							}
													
						}
						
						String vie6= csvRecord.get(92);
						if(StringUtils.isNotBlank(vie6))
						{
							vie6= StringUtils.normalizeSpace(vie6);
							if(StringUtils.equalsIgnoreCase(vie6, "X"))
							{
								sensorySupports.add("8");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Italian");
							}
													
						}
					
						String s= csvRecord.get(93);
						if(StringUtils.isNotBlank(s))
						{
							s= StringUtils.normalizeSpace(s);
							if(StringUtils.equalsIgnoreCase(s, "X"))
							{
								sensorySupports.add("9");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Spanish");
							}
													
						}
						
						String k= csvRecord.get(94);
						if(StringUtils.isNotBlank(k))
						{
							k= StringUtils.normalizeSpace(k);
							if(StringUtils.equalsIgnoreCase(k, "X"))
							{
								sensorySupports.add("10");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Korean");
							}
													
						}
						
						String kk= csvRecord.get(95);
						if(StringUtils.isNotBlank(kk))
						{
							kk= StringUtils.normalizeSpace(kk);
							if(StringUtils.equalsIgnoreCase(kk, "X"))
							{
								sensorySupports.add("11");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Interpreter: Tagalog");
							}
													
						}
						
						String ll= csvRecord.get(96);
						if(StringUtils.isNotBlank(ll))
						{
							ll= StringUtils.normalizeSpace(ll);
							if(StringUtils.equalsIgnoreCase(ll, "X"))
							{
								sensorySupports.add("12");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Transcribe: Audio (spoken / braille)");
							}
													
						}
						
						//Sensory Supports / Intermediaries - Transcribe: Visual (captioning)
						String cc= csvRecord.get(97);
						if(StringUtils.isNotBlank(cc))
						{
							cc= StringUtils.normalizeSpace(cc);
							if(StringUtils.equalsIgnoreCase(cc, "X"))
							{
								sensorySupports.add("13");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Transcribe: Visual (captioning)");
							}
													
						}
						
						String sc= csvRecord.get(98);
						if(StringUtils.isNotBlank(sc))
						{
							sc= StringUtils.normalizeSpace(sc);
							if(StringUtils.equalsIgnoreCase(sc, "X"))
							{
								sensorySupports.add("14");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Support Coordination");
							}
													
						}
						
						String pm= csvRecord.get(99);
						if(StringUtils.isNotBlank(pm))
						{
							pm= StringUtils.normalizeSpace(pm);
							if(StringUtils.equalsIgnoreCase(pm, "X"))
							{
								sensorySupports.add("15");
								
								_text_.add("Sensory Supports / Intermediaries");
								_text_.add("Plan Management");
							}
													
						}
						
						indexObject.setSensorySupports(sensorySupports);
						sDoc.addField("sensorySupports",sensorySupports);
						
						
						List<String> communityAndGovernment = new ArrayList<String>();
						String cgov= csvRecord.get(100);
						if(StringUtils.isNotBlank(cgov))
						{
							cgov= StringUtils.normalizeSpace(cgov);
							if(StringUtils.equalsIgnoreCase(cgov, "X"))
							{
								communityAndGovernment.add("1");
								_text_.add("Advocacy");
								_text_.add("Community and Government");
								
							}
									
							
						}
			
						String csel= csvRecord.get(101);
								if(StringUtils.isNotBlank(csel))
								{
									csel= StringUtils.normalizeSpace(csel);
									if(StringUtils.equalsIgnoreCase(csel, "X"))
									{
										communityAndGovernment.add("2");
										_text_.add("Counselling");
										_text_.add("Community and Government");
										
									}
											
									
								}
								
								String fia= csvRecord.get(102);
								if(StringUtils.isNotBlank(fia))
								{
									fia= StringUtils.normalizeSpace(fia);
									if(StringUtils.equalsIgnoreCase(fia, "X"))
									{
										communityAndGovernment.add("3");
										_text_.add("Financial Assistance");
										_text_.add("Community and Government");
										
									}
											
									
								}
								
								String leg= csvRecord.get(103);
								if(StringUtils.isNotBlank(leg))
								{
									leg= StringUtils.normalizeSpace(leg);
									if(StringUtils.equalsIgnoreCase(leg, "X"))
									{
										communityAndGovernment.add("4");
										_text_.add("Legal");
										_text_.add("Community and Government");
										
									}
											
									
								}
								
						indexObject.setCommunityAndGovernment(communityAndGovernment);
						sDoc.addField("communityAndGovernment",communityAndGovernment);
								
						List<String> specialistModels = new ArrayList<String>();
						String sabi= csvRecord.get(104);
						if(StringUtils.isNotBlank(sabi))
						{
							sabi= StringUtils.normalizeSpace(sabi);
							if(StringUtils.equalsIgnoreCase(sabi, "X"))
							{
								specialistModels.add("1");
								_text_.add("Applied Behavioural Intervention");
								_text_.add("specialist Models");
								
							}
									
							
						}
						
						String esdm= csvRecord.get(105);
						if(StringUtils.isNotBlank(esdm))
						{
							esdm= StringUtils.normalizeSpace(esdm);
							if(StringUtils.equalsIgnoreCase(esdm, "X"))
							{
								specialistModels.add("2");
								_text_.add("Early Start Denver Model");
								_text_.add("specialist Models");
								
							}
									
							
						}
						
						indexObject.setSpecialist_models(specialistModels);
						sDoc.addField("specialistModels",specialistModels);
						
						/**
						
						String starRating= csvRecord.get(109);
						if(StringUtils.isNotBlank(starRating))
						{
							starRating = StringUtils.normalizeSpace(starRating);
							if(starRating.equals("1") || starRating.equals("2") || starRating.equals("3") || starRating.equals("4") || starRating.equals("5"))
							{
								indexObject.setStarRating(starRating);
								sDoc.addField("starRating", starRating);
							}
							
						}
						*/
						
						String ndisRegistered= csvRecord.get(106);
						if(StringUtils.isNotBlank(ndisRegistered))
						{
							ndisRegistered = StringUtils.normalizeSpace(ndisRegistered);
							if(ndisRegistered.equalsIgnoreCase("T"))
							{
								indexObject.setNDISRegistered(true);
								_text_.add("NDIS Registered");
								sDoc.addField("ndisRegisteredBool", true);
								sDoc.addField("ndisRegistered", "1");
							}
							else 
							{
								indexObject.setNDISRegistered(false);
								sDoc.addField("ndisRegisteredBool", false);
								sDoc.addField("ndisRegistered", "0");
							}
								
							
						}
						else
						{
							indexObject.setNDISRegistered(false);
							sDoc.addField("ndisRegisteredBool", false);
							sDoc.addField("ndisRegistered", "0");
						}
						
											
						List<String> stateProvidedIn = new ArrayList<String>();
						
						
						String stateProvidedInAct = csvRecord.get(107);
						if(StringUtils.isNotBlank(stateProvidedInAct))
						{
							stateProvidedInAct = StringUtils.normalizeSpace(stateProvidedInAct);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInAct , "X"))
							{
								stateProvidedInAct= "1";
								stateProvidedIn.add(stateProvidedInAct);
							}
								
											
						}
						
								

						
						String stateProvidedInNSW = csvRecord.get(108);
						if(StringUtils.isNotBlank(stateProvidedInNSW))
						{
							stateProvidedInNSW = StringUtils.normalizeSpace(stateProvidedInNSW);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInNSW , "X"))
							{
								stateProvidedInNSW= "2";
								stateProvidedIn.add(stateProvidedInNSW);
							}
								
											
						}
					
						String stateProvidedInNT = csvRecord.get(109);
						if(StringUtils.isNotBlank(stateProvidedInNT))
						{
							stateProvidedInNT = StringUtils.normalizeSpace(stateProvidedInNT);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInNT, "X"))
							{
								stateProvidedInNT = "3";
								stateProvidedIn.add(stateProvidedInNT);
							}
								
											
						}
						
						
						String stateProvidedInQLD = csvRecord.get(110);
						if(StringUtils.isNotBlank(stateProvidedInQLD))
						{
							stateProvidedInQLD = StringUtils.normalizeSpace(stateProvidedInQLD);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInQLD, "X"))
							{
								stateProvidedInQLD = "4";
								stateProvidedIn.add(stateProvidedInQLD);
							}
								
											
						}
						
						
						String stateProvidedInSA = csvRecord.get(111);
						if(StringUtils.isNotBlank(stateProvidedInSA))
						{
							stateProvidedInSA = StringUtils.normalizeSpace(stateProvidedInSA);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInSA, "X"))
							{
								stateProvidedInSA = "5";
								stateProvidedIn.add(stateProvidedInSA);
							}
								
											
						}
						
						
						String stateProvidedInTAS = csvRecord.get(112);
						if(StringUtils.isNotBlank(stateProvidedInTAS))
						{
							stateProvidedInTAS = StringUtils.normalizeSpace(stateProvidedInTAS);
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInTAS, "X"))
							{
								stateProvidedInTAS = "6";
								stateProvidedIn.add(stateProvidedInTAS);
							}
								
											
						}
						
						
						String stateProvidedInVIC = csvRecord.get(113);
						if(StringUtils.isNotBlank(stateProvidedInVIC ))
						{
							stateProvidedInVIC  = StringUtils.normalizeSpace(stateProvidedInVIC );
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInVIC , "X"))
							{
								stateProvidedInVIC  = "7";
								stateProvidedIn.add(stateProvidedInVIC );
							}
								
											
						}
						
						
						String stateProvidedInWA = csvRecord.get(114);
						if(StringUtils.isNotBlank(stateProvidedInWA ))
						{
							stateProvidedInWA   = StringUtils.normalizeSpace(stateProvidedInWA );
							
							
							if(StringUtils.equalsIgnoreCase(stateProvidedInWA  , "X"))
							{
								stateProvidedInWA   = "8";
								stateProvidedIn.add(stateProvidedInWA );
							}
								
											
						}
						
						
						
						sDoc.addField("stateProvidedIn", stateProvidedIn);
						indexObject.setStateProvidedIn(stateProvidedIn);
						
						String logoRequired = csvRecord.get(115);
						if(StringUtils.isNotBlank(logoRequired))
						{
							logoRequired = StringUtils.normalizeSpace(logoRequired);
							if(ndisRegistered.equalsIgnoreCase("T"))
							{
								indexObject.setLogoRequired(true);
								
								sDoc.addField("logoRequiredBool", true);
								sDoc.addField("logoRequired", "1");
							}
							else 
							{
								indexObject.setLogoRequired(false);
								sDoc.addField("logoRequiredBool", false);
								sDoc.addField("logoRequired", "0");
							}
								
							
						}
						else
						{
							indexObject.setLogoRequired(false);
							sDoc.addField("logoRequiredBool", false);
							sDoc.addField("logoRequired", "0");
						}
						
						sDoc.addField("_text_", _text_);
				
						sDoc.addField("dateOfIndex", new Date());
						
						solr.add(sDoc);
						
						solr.commit();
						logger.info("COMMITTED DATA RECORD "+ recNums);
					
						
					}
					
				}
			}
		} 
		
		catch (SolrServerException | IOException e) 
		{
			logger.info("Cannot index row "+recNums);
			System.out.println("Cannot index row "+recNums);
			
			e.printStackTrace();
			
			logger.info(e.getMessage());
			
		}
		finally
		{
			try {
				parser.close();
			} 
			catch (IOException e)
			{
				
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args)
	{
		TestIndexer testIndexer = new TestIndexer();
		testIndexer.index();
	}

}
