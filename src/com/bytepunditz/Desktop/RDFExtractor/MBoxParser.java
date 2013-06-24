package com.bytepunditz.Desktop.RDFExtractor;
import java.io.File;
import java.util.List;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.crawler.mbox.MboxCrawler;
import org.semanticdesktop.aperture.datasource.mbox.MboxDataSource;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.nepomuk.nrl.validator.ModelTester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytepunditz.Desktop.appertureEnablers.AbstractCrawler;
import com.bytepunditz.Desktop.crawlManager.CrawlConfig;


public class MBoxParser extends AbstractCrawler {
	
	public static final Logger LOG = LoggerFactory.getLogger(MBoxParser.class);
	private static MBoxParser _instance = null;
	public static MBoxParser getInstance()
	{
		if( _instance == null)
		{
			_instance = new MBoxParser();
		}
		
		return _instance;
	}
    private File mboxFile;

    public File getMboxFile() {
        return mboxFile;
    }

    public void setMboxFile(File mboxFile) {
        this.mboxFile = mboxFile;
    }
    
    @Override
    public ModelTester[] getAdditionalModelTesters() {
        return new ModelTester [] { };
    }

    public static void main(String[] args) throws Exception {
        // create a new ExampleFileCrawler instance
        MBoxParser crawler = new MBoxParser();
        
        List<String> remainingOptions = crawler.processCommonOptions(args);

        // parse the command line options
        for (String arg : remainingOptions) {
            if (crawler.getMboxFile() == null) {
                crawler.setMboxFile(new File(arg));
            }
            else {
                crawler.exitWithUsageMessage();
            }
        }

        // check that all required fields are available
        if (crawler.getMboxFile() == null) {
            crawler.exitWithUsageMessage();
        }

        // start crawling and exit afterwards
        crawler.crawl();
    }

    public void parseAndCrawl(String fileName)
    {
    	//TODO Nishant 
    	/*
    	 * 1. need to figure out if by default accessdata is stored or not. Otherwise it will parse 
    	 * whole of mailbox everytime.
    	 */
    	String[] args = new String[2];
    	args[0] = "-o";
    	String fileNameOutput = CrawlConfig.getRDFOutputFileName() + "_Mbox";
    	args[1] = fileNameOutput;
    	
//        MBoxParser crawler = new MBoxParser();
        try {
			this.processCommonOptions(args);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        this.setMboxFile(new File(fileName));
        
     // check that all required fields are available
        if (this.getMboxFile() == null) {
            this.exitWithUsageMessage();
        }

     // start crawling and exit afterwards
        try {
			this.crawl();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
//        RDFContainer container = waitTillResult();
//         return container;
    }
   
    /* TODO implement this function for similar flow of RDFContainer for every crawl. For now PIMO
     * function will be called from SimpleCrawlerHandler's crawlStopped function.
    RDFContainer waitTillResult()
    {
    	try {
			this.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//This thread has been woke up means crawl is complete.
    	return (RDFContainer) handler.getModelSet();
    	 
    }
    */
    
    public void crawl() throws ModelException {
        if (mboxFile == null) {
            throw new IllegalArgumentException("root file cannot be null");
        }

        // create a data source configuration
        RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
        RDFContainer configuration = factory.newInstance("source:testSource");

        // create the data source
        MboxDataSource source = new MboxDataSource();
        source.setConfiguration(configuration);
        source.setMboxPath(mboxFile.getAbsolutePath());

        // setup a crawler that can handle this type of DataSource
        MboxCrawler crawler = new MboxCrawler();
        crawler.setDataSource(source);
        crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
        crawler.setCrawlerHandler(getHandler());
        
        crawler.setAccessData(getAccessData());
            
        // start crawling
        crawler.crawl();
    }

    @Override
    protected String getSpecificExplanationPart() {
        return "  mboxFile - the path to the mbox file to be crawled";
    }

    @Override
    protected String getSpecificSyntaxPart() {
        return "mboxFile";
    }

}
