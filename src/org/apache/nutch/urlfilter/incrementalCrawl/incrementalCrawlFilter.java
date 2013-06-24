package org.apache.nutch.urlfilter.incrementalCrawl;


//Nutch imports
import java.io.File;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.net.URLFilterException;
import org.apache.nutch.net.URLFilter;

import com.bytepunditz.Desktop.crawlManager.CrawlConfig;

public class incrementalCrawlFilter implements  org.apache.nutch.net.URLFilter{

	@Override
	public Configuration getConf() {
		return null;
	}

	@Override
	public void setConf(Configuration arg0) {
		
	}

	@Override
	public String filter(String arg0) {
		
		String fileString = arg0.replace("file:", "");
		File f = null;
		try{
			f = new File(fileString);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		Date fileModifiedDate = new Date(f.lastModified());
		System.out.println("Nishant URL = " + arg0 +" modified date: " + fileModifiedDate + "epocTime = " + f.lastModified()); 

		Date cutoffDate = CrawlConfig.getCuttoffDate();
		if(fileModifiedDate.after(cutoffDate))
		{
			System.out.println("Nishant filtered in url :" + arg0);
			return arg0;
		}
		else
		{
//			System.out.println("Nishant filtered out url :" + arg0);
			return null;
		}
	}

	
//	@Override
//	public String filter(String arg0) throws URLFilterException {
////		String returnString = super.filter(arg0);
//		System.out.println("Nishant argument: " + arg0 + " and return :" + arg0);
//		return arg0;
//	}

}
