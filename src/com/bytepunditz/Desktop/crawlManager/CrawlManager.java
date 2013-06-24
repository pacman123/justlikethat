package com.bytepunditz.Desktop.crawlManager;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Crawl;
import org.apache.nutch.segment.SegmentReader;
import org.apache.nutch.util.NutchConfiguration;
import org.semanticdesktop.aperture.rdf.RDFContainer;

import com.bytepunditz.Desktop.RDFExtractor.ChromeParser;
import com.bytepunditz.Desktop.RDFExtractor.MBoxParser;

public class CrawlManager {
	
	//TODO read the variable from the file
	static boolean isfirstTimeCrawl = true;
	static String crawlArg = "urls -dir crawl -threads 5 ";
	static boolean crawlStopped = false;
	
	 public static void main(String[] args) {
		 
		 if(isfirstTimeCrawl == false)
		 {
			 //setting the date at epoc
			 CrawlConfig.setCuttoffDate(new Date(0));
		 }
		 //TODO else get the last crawled date from the file
		 else
		 {
			 CrawlConfig.setCuttoffDate(new Date("2 August, 2012, 3:14 PM"));
		 }
		 
		 //setting the RDF file output.
		 CrawlConfig.setRDFOutputFileName("testRDF");
		 
		 //trigger the crawl and wait for three hours to trigger the next crawl
		 //TODO get this variable from the config file
//		 Timer timer = new Timer("Crawl");
//		 MyTask t = new MyTask();
//		 //3hours = 3*60*1000
//		 timer.schedule(t, 180000, 1);
		 CrawlManager.fileCrawl();
	    	//CrawlManager.waitForCrawlFinished();
	    	CrawlManager.browserCrawl("/home/pacman/Desktop/desktopCrawlSampleFolder/crome_backup/");
	    	CrawlManager.mailCrawl();
		 
	 }

	 public static int fileCrawl()
	 {
		 //TODO change in config to ignore system files.
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
	 
	 public static int browserCrawl(String chromeBaseUrl)
	 {
		 //TODO while filecrawling, figure out the different browser installed and
		 //and store their location
		 ChromeParser parser = new ChromeParser(chromeBaseUrl);
		 RDFContainer container = parser.crawlAndParse();
		 //TODO pass the container to the PIMO module
		return 0;
	 }
	 
	 public static int mailCrawl()
	 {
		 //TODO while fileCrawling, figure out different mail files are present and determined its
		 //mailbox client.
		 
		 //Currently hardcoding it to mbox.
		 String fileName = "/home/pacman/.thunderbird/eh05nvcc.default/";
		 
		 MBoxParser parser = new MBoxParser();
		 parser.parseAndCrawl(fileName);
		 return 0;
	 }
	 
        /**
         * Helper function to convert a string into an array of strings by
         * separating them using whitespace.
         *
         * @param str
         *            string to be tokenized
         * @return an array of strings that contain a each word each
         */
        public static String[] tokenize(String str) {
                StringTokenizer tok = new StringTokenizer(str);
                String tokens[] = new String[tok.countTokens()];
                int i = 0;
                while (tok.hasMoreTokens()) {
                        tokens[i] = tok.nextToken();
                        i++;
                }

                return tokens;

        }
        
        public static int crawlStopped()
        {
        	
        	//TODO make the variable thread safe access.
        	crawlStopped = true;
    		return 0;
        }
        
        public static void waitForCrawlFinished()
        {
        	//TODO make this operation thread safe
        	while(crawlStopped == false)
        	{
        		//polling every 10 sec
        		try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	
        	crawlStopped = true;
        }


}
 

class MyTask extends TimerTask {
    //times member represent calling times.
 
 
    public void run() {
    	
    	CrawlManager.fileCrawl();
    	CrawlManager.waitForCrawlFinished();
    	CrawlManager.browserCrawl("/home/pacman/Desktop/desktopCrawlSampleFolder/crome_backup/");
    	CrawlManager.mailCrawl();
    
    	//Stop Timer.
//            this.cancel();
        }
    }
    