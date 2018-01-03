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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pcg.australia.dataIndex.interfaces.IndexerInterface;
import com.pcg.australia.dataIndex.objects.IndexObject;
import com.pcg.australia.dataIndex.utils.Initializer;

@Component
public class AustraliaDataIndexer implements IndexerInterface
{
	@Autowired
	Initializer initializer;
	static Logger logger = Logger.getLogger(AustraliaDataIndexer.class);
	static String log4jConfig = "c:/australiaIndexer/log4jConfig.properties";
	
	@Override
	public void index() 
	{
		
		parseAndIndexCSV();
		
	}
	
	private void parseAndIndexCSV() 
	{
		String csvLocation = initializer.getCsvLocation();
		
		Integer recNums = 0;
		if(StringUtils.isBlank(csvLocation))
		{
			//Look in "c:/australiaIndexer/config" directory 
			System.out.println("ERROR ... \'csvLocation\' cannot be found");
			System.out.println("Looking in \'c:/australiaIndexer/config\' for the csv ...");
			logger.info("ERROR-- \'csvLocation\' cannot be found");
			logger.info("Looking in \'c:/australiaIndexer/config\' for the csv ...");
			csvLocation = "c:/australiaIndexer/config/australiaData.csv";
		}
		
		Integer recordNumber = null;
		String record = initializer.getRecordNumber();
		System.out.println("Identifying record number ...");
		logger.info("Identifying record number ...");
		if(StringUtils.isBlank(record))
		{
			System.out.println("ERROR-- record number is null");
			logger.info("ERROR-- record number is null");
			
			System.out.println("Setting record number to default, 4");
			logger.info("Setting record number to default, 4");
			recordNumber = 2;
		}
		else
		{
			recordNumber = Integer.parseInt(record);
			System.out.println("RecordNumber is ");
			logger.info("Setting record number to default, "+recordNumber);
		}
		
		try 
		{
			String solrHost = initializer.getSolrHost();
			System.out.println("Getting solr host (Note if solr Host is not found, localhost is used ... ");
			logger.info("Getting solr host (Note if solr Host is not found, localhost is used ... ");
			solrHost = StringUtils.isNotBlank(solrHost)?StringUtils.normalizeSpace(solrHost):"localhost";
			
			if(StringUtils.isBlank(solrHost))
			{
				System.out.println("solr host not found: Using localhost as solrHost  ");
				logger.info("solr host not found: Using localhost as solrHost ");
				solrHost = "localhost";
			}
			else{
				System.out.println("solr Host =  "+solrHost);
				logger.info("solr Host =  "+solrHost);
			}
			
			System.out.println("Getting solr Port (Note if solr Port is not found,8983 is used ... ");
			logger.info("Getting solr Port (Note if solr Port is not found, 8983 is used ... ");
			String solrPort = initializer.getSolrPort();
			solrPort = StringUtils.normalizeSpace(solrPort);
			
			if(!NumberUtils.isDigits(solrPort) == true)
			{
				System.out.println("Solr Port = 8983 ");
				
				solrPort = "8983"; //default solrPort
			}
			System.out.println("solr Port "+solrPort);
			logger.info("solr Port "+solrPort);
			
			System.out.println("Getting solrCore (Note if solr Core is not found, \'australiaCore\' is set as the default ... ");
			logger.info("Getting solrCore (Note if solr Core is not found, \'australiaCore\' is set as the default ... ");
			String solrCore = initializer.getSolrCore();
			solrCore = StringUtils.isBlank(solrCore)?StringUtils.normalizeSpace(solrCore):"australiaCore";
			System.out.println("solr Core "+solrCore);
			logger.info("solr Core "+solrCore);
			
			String solrUrl = "http://"+solrHost +":"+solrPort+"/solr/"+solrCore;//localhost:8984/solr/live";
			SolrClient solr = new HttpSolrClient.Builder(solrUrl).build();
			
			
			CSVParser parser = CSVParser.parse(new File(csvLocation), Charset.defaultCharset(), CSVFormat.EXCEL);
			for (CSVRecord csvRecord : parser)
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
						
						String address = csvRecord.get(2);
						address = StringUtils.isNotBlank(address)? StringUtils.normalizeSpace(address):"";
						indexObject.setAddress(address);
						_text_.add(address);
						sDoc.addField("address", address);
											
						String pc = csvRecord.get(3);
						String locality = csvRecord.get(45);
						String state= csvRecord.get(46);
						
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
						
						List<String> ageRange = new ArrayList<String>();
						String child = csvRecord.get(4);
						String adult = csvRecord.get(5);
						if(StringUtils.isNotBlank(child))
						{
							child = StringUtils.normalizeSpace(child);
							child = (StringUtils.equalsIgnoreCase(child, "X"))?"1":"";
							ageRange.add(child);
							_text_.add("children");
							_text_.add("child");
						}
						if(StringUtils.isNotBlank(adult))
						{
							adult = StringUtils.normalizeSpace(adult);
							adult = (StringUtils.equalsIgnoreCase(adult, "X"))?"2":"";
							ageRange.add(adult);
							_text_.add("adult");
							_text_.add("adults");
						}
						indexObject.setAgeGroup(ageRange);
						sDoc.addField("ageRange",ageRange);
						
						String autism = csvRecord.get(6);
						String devDelay = csvRecord.get(7);
						String downSyndrome = csvRecord.get(8);
						String intellectual = csvRecord.get(9);
						String neurological = csvRecord.get(10);
						String blind = csvRecord.get(11);
						String deaf = csvRecord.get(12);
						String physical = csvRecord.get(13);
						
						String cerebralPalsy = csvRecord.get(49);
						String earlyChildHoodIntervention = csvRecord.get(50);
						String multipleSclerosis = csvRecord.get(51);
						
						List<String> condition = new ArrayList<String>();
						
						
						if(StringUtils.isNotBlank(cerebralPalsy))
						{
							cerebralPalsy = StringUtils.normalizeSpace(cerebralPalsy);
							if(StringUtils.equalsIgnoreCase(cerebralPalsy, "X"))
							{
								condition.add("9");
								_text_.add("Cerebral Palsy");
							}
									
							
						}
						
						if(StringUtils.isNotBlank(earlyChildHoodIntervention))
						{
							earlyChildHoodIntervention = StringUtils.normalizeSpace(earlyChildHoodIntervention);
							if(StringUtils.equalsIgnoreCase(earlyChildHoodIntervention, "X"))
							{
								condition.add("10");
								_text_.add("Early Childhood Intervention");
							}
									
							
						}
						
						if(StringUtils.isNotBlank(multipleSclerosis))
						{
							multipleSclerosis = StringUtils.normalizeSpace(multipleSclerosis);
							if(StringUtils.equalsIgnoreCase(multipleSclerosis, "X"))
							{
								condition.add("11");
								_text_.add("Multiple Sclerosis");
							}
									
							
						}
						
						if(StringUtils.isNotBlank(autism))
						{
							autism = StringUtils.normalizeSpace(autism);
							if(StringUtils.equalsIgnoreCase(autism, "X"))
							{
								condition.add("1");
								_text_.add("autism");
							}
									
							
						}
						
						if(StringUtils.isNotBlank(devDelay))
						{
							devDelay = StringUtils.normalizeSpace(devDelay);
							if(StringUtils.equalsIgnoreCase(devDelay, "X"))
							{
								condition.add("2");
								_text_.add("development Delay");
							}
						}
						
						if(StringUtils.isNotBlank(downSyndrome))
						{
							downSyndrome = StringUtils.normalizeSpace(downSyndrome);
							if(StringUtils.equalsIgnoreCase(downSyndrome, "X"))
							{
								condition.add("3");
								_text_.add("down Syndrome");
							}
							
						}
						
						if(StringUtils.isNotBlank(intellectual))
						{
							intellectual = StringUtils.normalizeSpace(intellectual);
							if(StringUtils.equalsIgnoreCase(intellectual, "X"))
							{
								
								condition.add("4");
								_text_.add("intellectual");
							}
						}
						
						if(StringUtils.isNotBlank(neurological))
						{
							neurological = StringUtils.normalizeSpace(neurological);
							if(StringUtils.equalsIgnoreCase(neurological, "X"))
							{
								condition.add("5");
								_text_.add("neurological");
							}
						}
						
						if(StringUtils.isNotBlank(blind))
						{
							blind = StringUtils.normalizeSpace(blind);
							if(StringUtils.equalsIgnoreCase(blind, "X"))
							{
								
								
								condition.add("6");
								_text_.add("blind");
							}
						}
						
						if(StringUtils.isNotBlank(deaf))
						{
							deaf = StringUtils.normalizeSpace(deaf);
							if(StringUtils.equalsIgnoreCase(deaf, "X"))
							{
																
								condition.add("7");
								_text_.add("deaf");
							}
						}
					
						if(StringUtils.isNotBlank(physical))
						{
							physical = StringUtils.normalizeSpace(physical);
							if(StringUtils.equalsIgnoreCase(physical, "X"))
							{
								
								
								condition.add("8");
								_text_.add("physical");
							}
						}
					
						sDoc.addField("conditions",condition);
						
						List<String> NDISCategory = new ArrayList<String>();
						
						
						
						
						String assistance_with_Daily_Life  = csvRecord.get(22);
						if(StringUtils.isNotBlank(assistance_with_Daily_Life))
						{
							assistance_with_Daily_Life = StringUtils.normalizeSpace(assistance_with_Daily_Life);
							if(StringUtils.equalsIgnoreCase(assistance_with_Daily_Life, "X"))
							{
								NDISCategory.add("1");
								_text_.add("assistance with daily Life");
							}
						}
						
						String transport  = csvRecord.get(23);
						if(StringUtils.isNotBlank(transport))
						{
							transport = StringUtils.normalizeSpace(transport);
							if(StringUtils.equalsIgnoreCase(transport, "X"))
							{
								
								
								NDISCategory.add("2");
								_text_.add("transport");
							}
						}
						
						String consumables  = csvRecord.get(24);
						if(StringUtils.isNotBlank(consumables))
						{
							consumables = StringUtils.normalizeSpace(consumables);
							if(StringUtils.equalsIgnoreCase(consumables, "X"))
							{
								
								
								NDISCategory.add("3");
								_text_.add("consumables");
							}
						}
						
						String assistance_with_Social_Community_Participation   = csvRecord.get(25);
						if(StringUtils.isNotBlank(assistance_with_Social_Community_Participation))
						{
							assistance_with_Social_Community_Participation = StringUtils.normalizeSpace(assistance_with_Social_Community_Participation);
							if(StringUtils.equalsIgnoreCase(assistance_with_Social_Community_Participation, "X"))
							{
								
								
								NDISCategory.add("4");
								_text_.add("assistance with Social Community Participation");
							}
						}
						
						String coordination_of_supports    = csvRecord.get(26);
						if(StringUtils.isNotBlank(coordination_of_supports))
						{
							coordination_of_supports = StringUtils.normalizeSpace(coordination_of_supports);
							if(StringUtils.equalsIgnoreCase(coordination_of_supports, "X"))
							{
							
								
								NDISCategory.add("5");
								_text_.add("coordination of supports");
							}
						}
						
						String improved_living_arrangements     = csvRecord.get(27);
						if(StringUtils.isNotBlank(improved_living_arrangements ))
						{
							improved_living_arrangements  = StringUtils.normalizeSpace(improved_living_arrangements );
							if(StringUtils.equalsIgnoreCase(improved_living_arrangements , "X"))
							{
								
								
								NDISCategory.add("6");
								_text_.add("improved living arrangements");
							}
						}
						
						String increased_social_and_Community_Participation      = csvRecord.get(28);
						if(StringUtils.isNotBlank(increased_social_and_Community_Participation))
						{
							increased_social_and_Community_Participation  = StringUtils.normalizeSpace(increased_social_and_Community_Participation);
							if(StringUtils.equalsIgnoreCase(increased_social_and_Community_Participation , "X"))
							{
								
								
								NDISCategory.add("7");
								_text_.add("increased social and Community Participation");
							}
						}
						
						String finding_and_keeping_a_job = csvRecord.get(29);
						if(StringUtils.isNotBlank(finding_and_keeping_a_job))
						{
							finding_and_keeping_a_job  = StringUtils.normalizeSpace(finding_and_keeping_a_job);
							if(StringUtils.equalsIgnoreCase(finding_and_keeping_a_job , "X"))
							{
								
								
								NDISCategory.add("8");
								_text_.add("finding and keeping a job");
							}
						}
						
						String improved_relationships = csvRecord.get(30);
						if(StringUtils.isNotBlank(improved_relationships))
						{
							improved_relationships  = StringUtils.normalizeSpace(improved_relationships);
							if(StringUtils.equalsIgnoreCase(improved_relationships , "X"))
							{
								
								
								NDISCategory.add("9");
								_text_.add("improved relationships");
							}
						}
						
						String improved_health_and_wellbeing  = csvRecord.get(31);
						if(StringUtils.isNotBlank(improved_health_and_wellbeing ))
						{
							improved_health_and_wellbeing   = StringUtils.normalizeSpace(improved_health_and_wellbeing );
							if(StringUtils.equalsIgnoreCase(improved_health_and_wellbeing  , "X"))
							{
								
								
								NDISCategory.add("10");
								_text_.add("improved health and wellbeing");
							}
						}
						
						String improved_learning  = csvRecord.get(32);
						if(StringUtils.isNotBlank(improved_learning))
						{
							improved_learning   = StringUtils.normalizeSpace(improved_learning);
							if(StringUtils.equalsIgnoreCase(improved_learning, "X"))
							{
								
								
								NDISCategory.add("11");
								_text_.add("improved learning");
							}
						}
						
						String improved_life_choices  = csvRecord.get(33);
						if(StringUtils.isNotBlank(improved_life_choices))
						{
							improved_life_choices   = StringUtils.normalizeSpace(improved_life_choices);
							if(StringUtils.equalsIgnoreCase(improved_life_choices, "X"))
							{
								
								
								NDISCategory.add("12");
								_text_.add("improved life choices");
							}
						}
						
						String Improved_Daily_Living_Skills  = csvRecord.get(34);
						if(StringUtils.isNotBlank(improved_learning))
						{
							Improved_Daily_Living_Skills   = StringUtils.normalizeSpace(Improved_Daily_Living_Skills );
							if(StringUtils.equalsIgnoreCase(Improved_Daily_Living_Skills , "X"))
							{
								
								
								NDISCategory.add("13");
								_text_.add("Improved Daily Living Skills");
							}
						}
						
						String NDIS_registered  = csvRecord.get(35);
						if(StringUtils.isNotBlank(NDIS_registered))
						{
							NDIS_registered   = StringUtils.normalizeSpace(NDIS_registered);
							if(StringUtils.equalsIgnoreCase(NDIS_registered , "X"))
							{
								
								
								NDISCategory.add("14");
								_text_.add("NDIS registered");
							}
						}
						
					
						sDoc.addField("ndisCategory",NDISCategory);
						
						List<String> category = new ArrayList<String>();
												
						String Interpreting  = csvRecord.get(36);
						if(StringUtils.isNotBlank(Interpreting))
						{
							Interpreting  = StringUtils.normalizeSpace(Interpreting);
							if(StringUtils.equalsIgnoreCase(Interpreting , "X"))
							{
								
								
								category.add("1");
								_text_.add("Interpreting");
							}
						}
						
						String assistive_tech  = csvRecord.get(37);
						if(StringUtils.isNotBlank(assistive_tech))
						{
							assistive_tech  = StringUtils.normalizeSpace(assistive_tech);
							if(StringUtils.equalsIgnoreCase(assistive_tech , "X"))
							{
								
								
								category.add("2");
								_text_.add("Assistive Technology Equipment");
							}
						}
						
						String personal_care  = csvRecord.get(38);
						if(StringUtils.isNotBlank(personal_care ))
						{
							personal_care   = StringUtils.normalizeSpace(personal_care );
							if(StringUtils.equalsIgnoreCase(personal_care , "X"))
							{
								
								
								category.add("3");
								_text_.add("Personal Care");
							}
						}
						
						String employment_Support  = csvRecord.get(39);
						if(StringUtils.isNotBlank(employment_Support))
						{
							employment_Support   = StringUtils.normalizeSpace(employment_Support);
							if(StringUtils.equalsIgnoreCase(employment_Support, "X"))
							{
								
								
								category.add("4");
								_text_.add("Employment Support");
							}
						}
						
						String support_Coordination  = csvRecord.get(40);
						if(StringUtils.isNotBlank(support_Coordination))
						{
							support_Coordination  = StringUtils.normalizeSpace(support_Coordination);
							if(StringUtils.equalsIgnoreCase(support_Coordination, "X"))
							{
								
								
								category.add("5");
								_text_.add("Support Coordination");
							}
						}
						
						String general_Therapy = csvRecord.get(14);
						if(StringUtils.isNotBlank(general_Therapy))
						{
							general_Therapy  = StringUtils.normalizeSpace(general_Therapy);
							if(StringUtils.equalsIgnoreCase(general_Therapy, "X"))
							{
								
								
								category.add("6");
								_text_.add("General Therapy");
							}
						}
						
						String vehicle_Mods = csvRecord.get(15);
						if(StringUtils.isNotBlank(vehicle_Mods))
						{
							vehicle_Mods  = StringUtils.normalizeSpace(vehicle_Mods);
							if(StringUtils.equalsIgnoreCase(vehicle_Mods, "X"))
							{
								
								
								category.add("7");
								_text_.add("Vehicle Mods");
								_text_.add("Vehicle Modification");
							}
						}
						
						String home_Mods = csvRecord.get(16);
						if(StringUtils.isNotBlank(home_Mods ))
						{
							home_Mods   = StringUtils.normalizeSpace(home_Mods );
							if(StringUtils.equalsIgnoreCase(home_Mods , "X"))
							{
								
								
								category.add("8");
								_text_.add("home Mods");
								_text_.add("home Modification");
							}
						}
						
						String wheelchair = csvRecord.get(17);
						if(StringUtils.isNotBlank(wheelchair))
						{
							wheelchair   = StringUtils.normalizeSpace(wheelchair );
							if(StringUtils.equalsIgnoreCase(wheelchair  , "X"))
							{
								
								
								category.add("9");
								_text_.add("Wheelchair");
								_text_.add("Wheel chair");
								_text_.add("Wheel chair therapy");
							}
						}
						
						String SLT = csvRecord.get(18);
						if(StringUtils.isNotBlank(SLT))
						{
							SLT   = StringUtils.normalizeSpace(SLT);
							if(StringUtils.equalsIgnoreCase(SLT  , "X"))
							{
								
								
								category.add("10");
								_text_.add("Speech and language SLT");
								
							}
						}
						
						String Psychologist = csvRecord.get(19);
						if(StringUtils.isNotBlank(Psychologist))
						{
							Psychologist = StringUtils.normalizeSpace(Psychologist);
							if(StringUtils.equalsIgnoreCase(Psychologist , "X"))
							{
								
								
								category.add("11");
								_text_.add("Psychologist");
								_text_.add("therapist");
								
							}
						}
						
						String Physiotherapist = csvRecord.get(20);
						if(StringUtils.isNotBlank(Physiotherapist))
						{
							Physiotherapist = StringUtils.normalizeSpace(Physiotherapist);
							if(StringUtils.equalsIgnoreCase(Physiotherapist , "X"))
							{
								
								
								category.add("12");
								_text_.add("Physiotherapist");
								_text_.add("Physio");
							}
						}
						
						String music_Therapy = csvRecord.get(21);
						if(StringUtils.isNotBlank(music_Therapy))
						{
							music_Therapy = StringUtils.normalizeSpace(music_Therapy);
							if(StringUtils.equalsIgnoreCase(music_Therapy, "X"))
							{
								
								
								category.add("13");
								_text_.add("Music Therapy");
								
							}
						}
						
					
						sDoc.addField("category",category);
						
						String star_rating = csvRecord.get(41);
						if(StringUtils.isNotBlank(star_rating))
						{
							star_rating = StringUtils.normalizeSpace(star_rating);
							
							indexObject.setStarRating(star_rating);
						}
						else
						{
							star_rating="";
						}
						sDoc.addField("starRating",star_rating);
						
						List<String> priceRange = new ArrayList<String>();
						String pr0100 = csvRecord.get(42);
						if(StringUtils.isNotBlank(pr0100 ))
						{
							pr0100  = StringUtils.normalizeSpace(pr0100 );
							if(StringUtils.equalsIgnoreCase(pr0100 , "X"))
							{
								
								
								priceRange.add("1");
								
							}
						}
						
						String pr100150 = csvRecord.get(43);
						if(StringUtils.isNotBlank(pr100150))
						{
							pr100150  = StringUtils.normalizeSpace(pr100150);
							if(StringUtils.equalsIgnoreCase(pr100150, "X"))
							{
								
								
								priceRange.add("2");
								
							}
						}

						String pr150200 = csvRecord.get(44);
						if(StringUtils.isNotBlank(pr150200 ))
						{
							pr150200   = StringUtils.normalizeSpace(pr150200 );
							if(StringUtils.equalsIgnoreCase(pr150200 , "X"))
							{
								
								
								priceRange.add("3");
								
							}
						}
						
						
						
						String lat = csvRecord.get(47); //48
						System.out.println(lat);
						String longt = csvRecord.get(48); //49
						
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
							sDoc.addField("longtitude",longt);
							latlong = latlong+longt;
							indexObject.setLongLat(latlong);
							sDoc.addField("geoLocation",latlong);
							System.out.println(latlong+" gegegeg");
						}
						
						
						sDoc.addField("_text_", _text_);
						//try 
						//{
						sDoc.addField("dateOfIndex", new Date());
						
						solr.add(sDoc);
						
						solr.commit();
						//} catch (SolrServerException e) {
							// TODO Auto-generated catch block
						//	e.printStackTrace();
							
						//	logger.info(e.getMessage());
						//}
						
					}
				}
			}
		} 
		catch (IOException |SolrServerException e) 
		{
			logger.info("Cannot index row "+recNums);
			System.out.println("Cannot index row "+recNums);
			
			e.printStackTrace();
			
			logger.info(e.getMessage());
			
		}
	}
	
	

}
