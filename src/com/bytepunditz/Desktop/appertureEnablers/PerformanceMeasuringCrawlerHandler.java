/*
 * Copyright (c) 2006 - 2008 Aduna and Deutsches Forschungszentrum fuer Kuenstliche Intelligenz DFKI GmbH.
 * All rights reserved.
 * 
 * Licensed under the Aperture BSD-style license.
 */
package com.bytepunditz.Desktop.appertureEnablers;

import java.io.File;
import java.text.DecimalFormat;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.FileDataObject;
import org.semanticdesktop.aperture.crawler.Crawler;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.vocabulary.NIE;

/**
 * An example of a simple crawler handler.
 */
public class PerformanceMeasuringCrawlerHandler extends SimpleCrawlerHandler {

    // the time which is needed for crawling the all files
    // uses the java System.nanoTime(), because we have only interest
    // in the elapsed time therefore a correct timestamp is not needed
    private long crawlingTime;

    // variables needed for statistic
    int nrFiles;

    long fileSize; // size of one file

    // the mean of the crawling time and file size for one file
    double timeMean, fileSizeMean;

    // variable needed for calculation the standard deviation after Knuth
    double timeS, fileSizeS;

    // the delta which is needed for the calculation of the standard Deviation in Knuth's algorithm
    double timeDelta, fileSizeDelta;

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
    public PerformanceMeasuringCrawlerHandler(boolean identifyingMimeType, boolean extractingContents, boolean verbose, File outputFile){
        super(identifyingMimeType,extractingContents,verbose,outputFile);
    }

    /**
     * This method gets called when the crawl has been started
     * 
     * @param crawler the crawler that started the crawl.
     */
    public void crawlStarted(Crawler crawler) {
        nrObjects = 0;
        nrFiles = 0;
        crawlingTime = 0;
        timeMean = 0.0;
        timeDelta = 0.0;
        timeS = 0.0;
        fileSizeMean = 0.0;
        fileSizeDelta = 0.0;
        fileSizeS = 0.0;
    }

    /**
     * This method gets called when the crawler has encountered a new DataObject
     * 
     * @param dataCrawler the crawler
     * @param object the DataObject
     */
    public void objectNew(Crawler dataCrawler, DataObject object) {
        nrObjects++;
        if (nrObjects % 300 == 0)
            // call garbage collector from time to time
            System.gc();

        // process the contents of an InputStream, if available
        if (object instanceof FileDataObject) {
            nrFiles++;
            long crawlingOneFile = -1 * System.nanoTime();
            String s = null;
            try {
                s = (object.getMetadata().getString(NIE.byteSize));
                if (s != null) {
                        fileSize = Long.parseLong(s);
                }
                process((FileDataObject) object, dataCrawler);
            }
            catch (Exception e) {
                System.err.println("Exception while processing file size (" + s + ") of " + object.getID());
                e.printStackTrace();
            }
            crawlingOneFile += System.nanoTime();
            crawlingTime += crawlingOneFile;

            // calculate the Standard Deviation after Knuth
            // time
            timeDelta = crawlingOneFile - timeMean;
            timeMean += timeDelta / nrFiles;
            timeS += timeDelta * (crawlingOneFile - timeMean);
            // file size
            if (s != null) {
                fileSizeDelta = fileSize - fileSizeMean;
                fileSizeMean += fileSizeDelta / nrFiles;
                fileSizeS += fileSizeDelta * (fileSize - fileSizeMean);
            }
        }
        // really dispose the RDFContainer when noOutput
        disposeDataObject(object);
    }

    /**
     * This method gets called when the crawler finishes crawling a data source
     * @param crawler the crawler
     * @param exitCode the exit code.
     */
    public void crawlStopped(Crawler crawler, ExitCode exitCode) {
        try {
            double standardDeviationTime = Math.sqrt(timeS / (nrFiles - 1));
            double standardDeviationTimeIn_ms = Math.round(standardDeviationTime / 1000000 * 100.) / 100.;
            double standardDeviationFileSize = Math.round(Math.sqrt(fileSizeS / (nrFiles - 1)));
            double timeMeanIn_ms = Math.round(timeMean / 1000000 * 100.) / 100.;
            DecimalFormat f = new DecimalFormat("0.##");

            System.out.println("Crawled " + nrObjects + " objects in " + crawlingTime / 1000000
                    + " ms (exit code: " + exitCode + ")");
            System.out.println("Statistics:");
            System.out.println(" mean crawling time: " + timeMeanIn_ms + " ms/file");
            System.out.println(" standard Deviation crawling time: " + standardDeviationTimeIn_ms + " ms");
            System.out.println(" mean file size in byte: " + f.format(fileSizeMean));
            System.out.println(" standard Deviation file size in byte: "
                    + f.format(standardDeviationFileSize));
            printAndCloseModelSet();
            System.out.println("Output discarded");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
