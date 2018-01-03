package com.pcg.australia.dataIndex;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pcg.australia.dataIndex.objects.IndexObject;

/**
 * 
 * @author oonyimadu
 *
 */
public class TestIndexerRunnable 
{
	static SolrClient solr;
	static IndexObject indexObject;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String solrHost = "localhost";
		String solrPort = "8983";
		String solrCore = "australiaCore";
							
		String solrUrl = "http://"+solrHost +":"+solrPort+"/solr/"+solrCore;//localhost:8984/solr/live";
		solr = new HttpSolrClient.Builder(solrUrl).build();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		solr.close();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		//solr.close();
	}
	
	@Test
	public final void testSolr()
	{
		SolrPingResponse pingResponse; 
		try 
		{
			pingResponse = solr.ping();
			assertNotNull(pingResponse);
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 */
	@Test
	public final void testIndex() {
		DataQuery q = new DataQuery();
		q.setEventId("D120A10F-2736-415F-99AC-A817018A17D6");
		QueryResponse qResp;
		try
		{
			qResp = solr.query(q._toQuery());
			SolrDocumentList docList = qResp.getResults();
			Assert.assertEquals(1, docList.size());
			
			SolrDocument doc1 = docList.get(0);
			String eventName = StringUtils.normalizeSpace((String) doc1.getFieldValue("eventName"));
            Assert.assertEquals("All Areas Speech Pathology", eventName);
		}
		catch(SolrServerException | IOException e)
		{
			e.printStackTrace();
		}
		//fail("Not yet implemented"); // TODO
	}
	
	

}
