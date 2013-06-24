/*
 * Copyright (c) 2006 - 2008 Aduna and Deutsches Forschungszentrum fuer Kuenstliche Intelligenz DFKI GmbH.
 * All rights reserved.
 * 
 * Licensed under the Aperture BSD-style license.
 */
package com.bytepunditz.Desktop.appertureEnablers;

import java.io.File;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.crawler.Crawler;

/**
 * An an extension of the SimpleCrawlerHandler that validates IMAP uris. It is used
 * to test the solution to the sourceforge issue number [1565163].
 */
public class IMAPUrisValidatingCrawlerHandler extends SimpleCrawlerHandler {

    /**
     * Constructor.
     * 
     * @param identifyingMimeType 'true' if the crawler is to use a MIME type identifier on each
     *            FileDataObject it gets, 'false' if not
     * @param extractingContents 'true' if the crawler is to use an extractor on each DataObject it gets
     *            'false' if not
     * @param verbose 'true' if the crawler is to print verbose messages on what it is doing, false otherwise
     * @param outputFile the file where the extracted RDF metadata is to be stored. This argument can also be
     *            set to 'null', in which case the RDF metadata will not be stored in a file. This setting is
     *            useful for performance measurements.
     * @throws ModelException
     */
    public IMAPUrisValidatingCrawlerHandler(boolean identifyingMimeType, boolean extractingContents, boolean verbose, File outputFile) {
        super(identifyingMimeType, extractingContents, verbose, outputFile);
       
    }

    /**
     * This method gets called when the crawler has encountered a new DataObject
     * 
     * @param dataCrawler the crawler
     * @param object the DataObject
     */
    public void objectNew(Crawler dataCrawler, DataObject object) {
        super.objectNew(dataCrawler, object);
        System.out.print(object.getID().toString() + ": ");
        System.out.println("URI validation not implemented yet!");
    }


}
