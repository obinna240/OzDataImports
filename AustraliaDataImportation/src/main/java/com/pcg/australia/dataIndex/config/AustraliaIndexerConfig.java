package com.pcg.australia.dataIndex.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import com.pcg.australia.dataIndex.impl.NoRefOne;
import com.pcg.australia.dataIndex.interfaces.NoRefTwo;
import com.pcg.australia.dataIndex.objects.NoRefThree;
import com.pcg.australia.dataIndex.utils.Initializer;

@Configuration
@ComponentScan(basePackageClasses={NoRefOne.class, NoRefTwo.class, NoRefThree.class})
@PropertySource(value="classpath:application.properties",ignoreResourceNotFound=true)
public class AustraliaIndexerConfig
{
	@Autowired
	private Environment env;
	
	
	@Bean
	public Initializer initializer()
	{
		String csvLocation = env.getProperty("csv.location");
		
		String smtpHost = env.getProperty("smtp.host");
		String smtpPort = env.getProperty("smtp.port");
		String smtpEmailFrom = env.getProperty("smtp.email.from"); 
		
		String smtpEmailTo = env.getProperty("smtp.email.to"); 
		String smtpEmailSubject = env.getProperty("smtp.email.subject"); 
		
		String solrCore= env.getProperty("solr.core"); 
		String solrHost= env.getProperty("solr.host"); 
		String solrPort= env.getProperty("solr.port"); 
		
		String recordNumber = env.getProperty("recordNumber");
		
		return new Initializer(csvLocation, smtpHost, smtpPort, smtpEmailFrom, 
				smtpEmailTo, smtpEmailSubject, solrCore, solrHost, solrPort, recordNumber);
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() 
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
	
}
