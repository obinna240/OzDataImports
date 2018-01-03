package com.pcg.australia.dataIndex;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataQuery
{
	private final SolrQuery solrQuery;
	private static final Logger LOGGER = LoggerFactory.getLogger(DataQuery.class);
	
	//SolrQuery query = new SolrQuery();
	public DataQuery()
	{
		this.solrQuery = new SolrQuery();
		
	}
	
	public DataQuery setEventId(String eventId)
	{
		if (StringUtils.isNotBlank(eventId))
		{
			this.solrQuery.add("q","eventId:"+eventId);
		}
		return this;
	}
	
	/**
	 * 
	 * @return SolrQuery
	 */
	public SolrQuery _toQuery()
	{
		LOGGER.debug("query{} "+this.solrQuery.toQueryString());
		return this.solrQuery;
	}

}
