package com.pcg.australia.dataIndex;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.pcg.australia.dataIndex.impl.TestIndexer;

@SpringBootApplication
public class AustraliaDataImportationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AustraliaDataImportationApplication.class, args);
	}
	
	
	 @Bean
	    CommandLineRunner init(TestIndexer tindexer) {

	        return args -> {
	        	
	        	/**
	           System.out.println(repo.count());
	           List<AustraliaData> vv = repo.findByFullPCAndLocality("NT810", "ALAWA");
	           System.out.println(vv.size());
	   			
	           */
	        	tindexer.index();
	        };
	 }
	
}
