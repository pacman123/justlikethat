package com.bytepunditz.Desktop.RDFExtractor;
/*
 * Copyright (c) 2005 - 2008 Aduna and Deutsches Forschungszentrum fuer Kuenstliche Intelligenz DFKI GmbH.
 * All rights reserved.
 * 
 * Licensed under the Aperture BSD-style license.
 */


import java.io.File;
import java.util.List;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.impl.DefaultDataAccessorRegistry;
import org.semanticdesktop.aperture.crawler.ical.IcalCrawler;
import org.semanticdesktop.aperture.datasource.ical.IcalDataSource;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.nepomuk.nrl.validator.ModelTester;
import org.semanticdesktop.nepomuk.nrl.validator.testers.DataObjectTreeModelTester;

import com.bytepunditz.Desktop.appertureEnablers.AbstractCrawler;
/**
 * Example class demonstrating the usage of an IcalCrawler.
 */
public class IcalParser extends AbstractCrawler {

    private File icalFile;

    public File getIcalFile() {
        return icalFile;
    }

    public void setIcalFile(File icalFile) {
        this.icalFile = icalFile;
    }
    
    
    /**
     * The ical crawler satisfies a more strict constraint. It produces a valid
     * DataObject tree.
     */
    @Override
    public ModelTester[] getAdditionalModelTesters() {
        return new ModelTester [] {new DataObjectTreeModelTester() };
    }

//    public static void main(String[] args) throws Exception {
//        // create a new ExampleFileCrawler instance
//        IcalParser crawler = new IcalParser();
//        
//        List<String> remainingOptions = crawler.processCommonOptions(args);
//
//        // parse the command line options
//        for (String arg : remainingOptions) {
//            if (crawler.getIcalFile() == null) {
//                crawler.setIcalFile(new File(arg));
//            }
//            else {
//                crawler.exitWithUsageMessage();
//            }
//        }
//
//        // check that all required fields are available
//        if (crawler.getIcalFile() == null) {
//            crawler.exitWithUsageMessage();
//        }
//
//        // start crawling and exit afterwards
//        crawler.crawl();
//    }
    
    public void crawlAndParse(String[] args) throws Exception
    {
    	// create a new ExampleFileCrawler instance
        IcalParser crawler = new IcalParser();
        
        List<String> remainingOptions;
		remainingOptions = crawler.processCommonOptions(args);
		
        // parse the command line options
        for (String arg : remainingOptions) {
            if (crawler.getIcalFile() == null) {
                crawler.setIcalFile(new File(arg));
            }
            else {
                crawler.exitWithUsageMessage();
            }
        }

        // check that all required fields are available
        if (crawler.getIcalFile() == null) {
            crawler.exitWithUsageMessage();
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
        if (icalFile == null) {
            throw new IllegalArgumentException("root file cannot be null");
        }

        // create a data source configuration
        RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
        RDFContainer configuration = factory.newInstance("source:testSource");

        // create the data source
        IcalDataSource source = new IcalDataSource();
        source.setConfiguration(configuration);
        source.setRootUrl(icalFile.getAbsolutePath());

        // setup a crawler that can handle this type of DataSource
        IcalCrawler crawler = new IcalCrawler();
        crawler.setDataSource(source);
        crawler.setDataAccessorRegistry(new DefaultDataAccessorRegistry());
        crawler.setCrawlerHandler(getHandler());
        crawler.setAccessData(getAccessData());
        
        // start crawling
        crawler.crawl();
    }

    @Override
    protected String getSpecificExplanationPart() {
        return "  icalFile - the path to the ical file to be crawled";
    }

    @Override
    protected String getSpecificSyntaxPart() {
        return "icalFile";
    }
}
