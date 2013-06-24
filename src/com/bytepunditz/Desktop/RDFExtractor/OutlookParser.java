package com.bytepunditz.Desktop.RDFExtractor;
import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.outlook.OutlookDataSource;
import org.semanticdesktop.aperture.outlook.OutlookCrawler;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;

import com.bytepunditz.Desktop.appertureEnablers.AbstractCrawler;



public class OutlookParser extends AbstractCrawler {
	  /**
     * Pass a ROOT_URL that will be the prefix
     */
    public static final String ROOT_URL_OPTION = "-rooturl";
    
    OutlookDataSource source = new OutlookDataSource();
    
    public OutlookParser() {
        // create the data source
        RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
        RDFContainer configuration = factory.newInstance("source:testSource");
        source = new OutlookDataSource();
        source.setConfiguration(configuration);
    }
    
   
    public void crawlAndParse(String rootUrl )
    {
    	//default rootUrl should be "semdesk:outlook:";
    	OutlookParser crawler = new OutlookParser();
    	if (rootUrl != null) {
            crawler.source.setRootUrl(rootUrl);
        }

        // start crawling and exit afterwards
        try {
			crawler.crawl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
    	
    }

    public void crawl() throws ModelException {
        // setup a crawler that can handle this type of DataSource
        OutlookCrawler crawler = new OutlookCrawler();
        crawler.setDataSource(source);
        crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
        crawler.setCrawlerHandler(getHandler());
        crawler.setAccessData(getAccessData());
        
        // start crawling
        crawler.crawl();
    }

    @Override
    protected String getSpecificExplanationPart() {
        return "  -rooturl: define the prefix used for outlook resource URIs. Should begin with 'outlook:' (optional)";
    }

    @Override
    protected String getSpecificSyntaxPart() {
        return "[-rooturl uriprefix]";
    }


}
