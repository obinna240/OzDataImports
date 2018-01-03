package com.pcg.australia.dataIndex.objects;

import java.util.Map;

public class CSVFileObject
{
	private Map<String, String> csvRow;
	String[] ndiscategories = {"",};

	public Map<String, String> getCsvRow() {
		return csvRow;
	}

	public void setCsvRow(Map<String, String> csvRow) {
		this.csvRow = csvRow;
	} 
}
