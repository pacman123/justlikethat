package com.bytepunditz.Desktop.crawlManager;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Crawl;
import org.apache.nutch.util.NutchConfiguration;
import org.semanticdesktop.aperture.rdf.RDFContainer;

import com.bytepunditz.Desktop.RDFExtractor.ChromeParser;
import com.bytepunditz.Desktop.RDFExtractor.MBoxParser;

public class FirstTimeCrawl {
	
	 static String crawlArg = "urls -dir crawl -threads 5 ";

    
	 public static int firstTimeFileCrawl()
	 {
		 // Run Crawl tool

	     try {
	             ToolRunner.run(NutchConfiguration.create(), new Crawl(),
	                             CrawlManager.tokenize(crawlArg));
	     } catch (Exception e) {
	             e.printStackTrace();
	             return -1;
	     }
		 return 0;
	 }
	 
	 public static int firstTimeBrowserCrawl()
	 {
		 //TODO while filecrawling, figure out the different browser installed and
		 //and store their location
		 ChromeParser parser = new ChromeParser("/home/pacman/Desktop/desktopCrawlSampleFolder/crome_backup/");
		 RDFContainer container = parser.crawlAndParse();
		 //TODO pass the container to the PIMO module
		return 0;
	 }
	 
	 public static int firstTimeMailCrawl()
	 {
		 //TODO while fileCrawling, figure out different mail files are present and determined its
		 //mailbox client.
		 
		 //Currently hardcoding it to mbox.
		 String fileName = "/home/pacman/.thunderbird/eh05nvcc.default/";
		 
		 MBoxParser parser = new MBoxParser();
		 parser.parseAndCrawl(fileName);
		 return 0;
	 }

}
