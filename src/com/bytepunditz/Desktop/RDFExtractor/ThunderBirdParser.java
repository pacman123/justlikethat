package com.bytepunditz.Desktop.RDFExtractor;
import java.util.List;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.addressbook.thunderbird.ThunderbirdAddressbookDataSource;
import org.semanticdesktop.aperture.addressbook.thunderbird.ThunderbirdCrawler;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.nepomuk.nrl.validator.ModelTester;
import org.semanticdesktop.nepomuk.nrl.validator.testers.DataObjectTreeModelTester;

import com.bytepunditz.Desktop.appertureEnablers.AbstractCrawler;
import com.bytepunditz.Desktop.crawlManager.CrawlConfig;


public class ThunderBirdParser extends AbstractCrawler{
	String thunderbirdAddressbookPath;


	/**
	 * @return Returns the thunderbirdAddressbookPath.
	 */
	public String getThunderbirdAddressbookPath() {
		return thunderbirdAddressbookPath;
	}


	/**
	 * @param thunderbirdAddressbookPath The thunderbirdAddressbookPath to set.
	 */
	public void setThunderbirdAddressbookPath(String thunderbirdAddressbookPath) {
		this.thunderbirdAddressbookPath = thunderbirdAddressbookPath;
	}

	/**
	 * The thunderbird crawler satisfies a more strict constraint. It produces a valid
	 * DataObject tree.
	 */
	@Override
	public ModelTester[] getAdditionalModelTesters() {
		return new ModelTester [] {new DataObjectTreeModelTester() };
	}

	public static void main(String[] args) throws Exception {
		// create a new ExampleFileCrawler instance
		//	        ThunderBirdParser crawler = new ThunderBirdParser();
		//	        
		//	        List<String> remainingOptions = crawler.processCommonOptions(args);
		//	        
		//	        if (remainingOptions.size() != 1) {
		//	            crawler.exitWithUsageMessage();
		//	        } else {
		//	            crawler.setThunderbirdAddressbookPath(remainingOptions.get(0));
		//	        }
		//
		//	        // start crawling and exit afterwards
		//	        crawler.crawl();
		ThunderBirdParser p = new ThunderBirdParser();
		p.crawlAndParse("/home/pacman/.thunderbird/eh05nvcc.default/impab.mab");
	}

	public RDFContainer crawlAndParse(String addressBookPath)
	{
		RDFContainer container = null;
		String[] args = new String[3];
		args[0] = addressBookPath;
		args[1] = "-o";
		String fileNameOutput = CrawlConfig.getRDFOutputFileName() + "_ThunderBird";
		args[2] = fileNameOutput;
		try {
			List<String> remainingOptions = this.processCommonOptions(args);
			// start crawling and exit afterwards
			if (remainingOptions.size() != 1) {
				this.exitWithUsageMessage();
			} else {
				this.setThunderbirdAddressbookPath(remainingOptions.get(0));
			}
			container = this.crawl();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return container;
	}
	public RDFContainer crawl() throws ModelException {
		// create a data source configuration
		RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
		RDFContainer configuration = factory.newInstance("source:testSource");

		// create the data source
		ThunderbirdAddressbookDataSource source = new ThunderbirdAddressbookDataSource();
		source.setConfiguration(configuration);
		source.setThunderbirdAddressbookPath(thunderbirdAddressbookPath);

		// setup a crawler that can handle this type of DataSource
		ThunderbirdCrawler crawler = new ThunderbirdCrawler();
		crawler.setDataSource(source);
		crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
		crawler.setCrawlerHandler(getHandler());
		crawler.setAccessData(getAccessData());

		// start crawling
		crawler.crawl();
		return configuration;
	}

	@Override
	protected String getSpecificExplanationPart() {
		return "   inputFile - file where your thunderbird addressbook is stored";
	}

	@Override
	protected String getSpecificSyntaxPart() {
		return "inputFile";
	}


}
