package com.bytepunditz.Desktop.crawlManager;

import java.util.Date;

public class CrawlConfig {
	static Date cuttoffDate;
	static String RDFOutputFileName;

	public static String getRDFOutputFileName() {
		return RDFOutputFileName;
	}

	public static void setRDFOutputFileName(String rDFOutputFileName) {
		RDFOutputFileName = rDFOutputFileName;
	}

	public static Date getCuttoffDate() {
		return cuttoffDate;
	}

	public static void setCuttoffDate(Date cuttoffDate) {
		CrawlConfig.cuttoffDate = cuttoffDate;
	}
	
	
	
}
