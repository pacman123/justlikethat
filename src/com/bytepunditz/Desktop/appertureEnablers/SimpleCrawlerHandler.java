/*
 * Copyright (c) 2006 - 2008 Aduna and Deutsches Forschungszentrum fuer Kuenstliche Intelligenz DFKI GmbH.
 * All rights reserved.
 * 
 * Licensed under the Aperture BSD-style license.
 */
package com.bytepunditz.Desktop.appertureEnablers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.ontoware.rdf2go.ModelFactory;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.semanticdesktop.aperture.accessor.DataObject;
import org.semanticdesktop.aperture.accessor.FileDataObject;
import org.semanticdesktop.aperture.accessor.RDFContainerFactory;
import org.semanticdesktop.aperture.crawler.CrawlReport;
import org.semanticdesktop.aperture.crawler.Crawler;
import org.semanticdesktop.aperture.crawler.CrawlerHandler;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.extractor.Extractor;
import org.semanticdesktop.aperture.extractor.ExtractorException;
import org.semanticdesktop.aperture.extractor.ExtractorFactory;
import org.semanticdesktop.aperture.extractor.ExtractorRegistry;
import org.semanticdesktop.aperture.extractor.FileExtractor;
import org.semanticdesktop.aperture.extractor.FileExtractorFactory;
import org.semanticdesktop.aperture.extractor.impl.DefaultExtractorRegistry;
import org.semanticdesktop.aperture.extractor.util.ThreadedExtractorWrapper;
import org.semanticdesktop.aperture.extractor.xmp.XMPExtractorFactory;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerImpl;
import org.semanticdesktop.aperture.subcrawler.SubCrawler;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerException;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerFactory;
import org.semanticdesktop.aperture.subcrawler.SubCrawlerRegistry;
import org.semanticdesktop.aperture.subcrawler.impl.DefaultSubCrawlerRegistry;
import org.semanticdesktop.aperture.tika.TikaMimeTypeIdentifier;
import org.semanticdesktop.aperture.util.IOUtil;
import org.semanticdesktop.aperture.vocabulary.NFO;
import org.semanticdesktop.aperture.vocabulary.NID3;
import org.semanticdesktop.aperture.vocabulary.NIE;
import org.semanticdesktop.aperture.vocabulary.NMO;

import com.bytepunditz.Desktop.crawlManager.CrawlManager;

/**
 * An example of a simple (but non-trivial) crawler handler.
 */
public class SimpleCrawlerHandler implements CrawlerHandler, RDFContainerFactory {

    // The main model set, that will contain all data 
    private ModelSet modelSet;

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////// OBSERVABLE PROPERTIES (ELEMENTS OF THE CRAWL REPORT PRINTED AT THE END OF THE CRAWL) /////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /** Number of data objects encountered by the crawler */
    protected int nrObjects;

    private long startTime = 0L;

    private long finishTime = 0L;

    private String currentURL;

    private ExitCode exitCode;
    
    private Map<String,Integer> detectedMimeTypes;

    private int unidentifiedMimeTypes;
    
    private int numberOfObjectsWithFullText;
    
    private int totalFulltextLength;
    
    private int objectsWhereProcessingExceptionOccured;
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////// APERTURE REGISTRIES /////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private TikaMimeTypeIdentifier mimeTypeIdentifier;

    private ExtractorRegistry extractorRegistry;
    
    private XMPExtractorFactory xmpExtractorFactory;
    
    private SubCrawlerRegistry subCrawlerRegistry;

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////// CONFIGURATION FIELDS /////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private boolean identifyingMimeType;

    private boolean extractingContents;
    
    private boolean verbose;
    
    private File outputFile;

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////// INITIALIZATION //////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructor. It will store the data in the model set returned by RDF2Go.getModelFactory().createModelSet()
     * 
     * @param identifyingMimeType 'true' if the crawler is to use a MIME type identifier on each
     *            FileDataObject it gets, 'false' if not
     * @param extractingContents 'true' if the crawler is to use an extractor on each DataObject it gets
     *            'false' if not
     * @param verbose 'true' if the crawler is to print verbose messages on what it is doing, false otherwise
     * @param outputFile the file where the extracted RDF metadata is to be stored. This argument can also be
     *            set to 'null', in which case the RDF metadata will not be stored in a file. This setting is
     *            useful for performance measurements.
     * @param modelSet the model set used to store the data
     * @throws ModelException
     */
    public SimpleCrawlerHandler(boolean identifyingMimeType, boolean extractingContents, boolean verbose, File outputFile) {
        construct(identifyingMimeType, extractingContents, verbose, outputFile, null);
    }
    
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
     * @param newModelSet the model set used to store the data
     * @throws ModelException
     */
    public SimpleCrawlerHandler(boolean identifyingMimeType, boolean extractingContents, boolean verbose, File outputFile, ModelSet newModelSet) {
        construct(identifyingMimeType, extractingContents, verbose, outputFile, newModelSet);
    }
    
    /**
     * Initializes the fields, called by constructors
     * @param l_identifyingMimeType
     * @param l_extractingContents
     * @param l_verbose
     * @param l_outputFile
     * @param newModelSet
     */
    private void construct(boolean l_identifyingMimeType, boolean l_extractingContents, boolean l_verbose, File l_outputFile, ModelSet newModelSet) {        
        if (newModelSet == null && l_outputFile == null) {
            // this may happen when measuring performance
            this.modelSet = null;
            this.outputFile = null;
        } else if (newModelSet == null && l_outputFile != null) {
            // a common case, we simply want to dump the output to a file
            ModelFactory factory = RDF2Go.getModelFactory();
            this.modelSet = factory.createModelSet();
            this.modelSet.open();
            this.outputFile = l_outputFile;
        } else if (newModelSet != null && l_outputFile == null){
            // this may happen, e.g. if we want to add data to an existing store
            this.modelSet = newModelSet;
            this.outputFile = null;
        }
        else if (newModelSet != null && l_outputFile != null) {
            // this means that we want to add data to an existing store, and get a serialization of the whole
            // store at the end of the crawl
            this.modelSet = newModelSet;
            this.outputFile = l_outputFile;
        }
        
        // set some flags
        this.identifyingMimeType = l_identifyingMimeType;
        this.extractingContents = l_extractingContents;
        this.verbose = l_verbose;
        
        // create some identification and extraction components
        if (l_identifyingMimeType) {
            mimeTypeIdentifier = new TikaMimeTypeIdentifier();
        }
        if (l_extractingContents) {
            extractorRegistry = new DefaultExtractorRegistry();
            xmpExtractorFactory = new XMPExtractorFactory();
            subCrawlerRegistry = new DefaultSubCrawlerRegistry();
        }
        
        this.detectedMimeTypes = new TreeMap<String, Integer>();
        this.unidentifiedMimeTypes = 0;
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// CrawlerHandler METHODS ////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This method gets called when the crawl has been started
     * 
     * @param crawler the crawler that started the crawl.
     */
    public void crawlStarted(Crawler crawler) {
        nrObjects = 0;
        startTime = System.currentTimeMillis();
    }

    /**
     * This method gets called when the crawler has began accessing an object.
     * 
     * @param crawler the crawler
     * @param url the URI of the object
     */
    public void accessingObject(Crawler crawler, String url) {
        this.currentURL = url;
    }

    /**
     * This method gets called when the crawler has encountered a new DataObject
     * 
     * @param dataCrawler the crawler
     * @param object the DataObject
     */
    public void objectNew(Crawler dataCrawler, DataObject object) {
        processDataObject(dataCrawler, object, "N");
    }
    
    /**
     * This method gets called when the crawler has encountered an object that has been modified
     * 
     * @param dataCrawler the crawler
     * @param object the DataObject
     */
    public void objectChanged(Crawler dataCrawler, DataObject object) {
        processDataObject(dataCrawler, object, "C");
    }
    
    /**
     * This method gets called when the crawler has encountered an object that has not been modified.
     * 
     * @param crawler the crawler
     * @param url the URI of the object.
     */
    public void objectNotModified(Crawler crawler, String url) {
        this.currentURL = url;
        printlnIfVerbose("U," + System.currentTimeMillis() + "," + currentURL);
    }

    /**
     * This method gets called when the crawler has encountered an object that has been removed from the data
     * source
     * 
     * @param dataCrawler the crawler
     * @param url the URI of the DataObject
     */
    public void objectRemoved(Crawler dataCrawler, String url) {
        this.currentURL = url;
        printlnIfVerbose("D," + System.currentTimeMillis() + "," + currentURL);
    }
    
    /**
     * This method gets called when the crawler started clearing the data source.
     * @param crawler the crawler
     */
    public void clearStarted(Crawler crawler) {
        // we don't call clear, this should not happen
        printUnexpectedEventWarning("clearStarted");
    }

    /**
     * This method gets called when the crawler clears an object
     * @param crawler the crawler
     * @param url the URI of the data object
     */
    public void clearingObject(Crawler crawler, String url) {
        // we don't call clear, this should not happen
        printUnexpectedEventWarning("clearingObject");
    }

    /**
     * This method gets called when the crawler has finished clearing
     * @param crawler the crawler
     * @param exitCode the exitCode
     */
    public void clearFinished(Crawler crawler, ExitCode l_exitCode) {
        // we don't call clear, this should not happen
        printUnexpectedEventWarning("clear finished");
    }
    
    /**
     * This method gets called when the crawler finishes crawling a data source
     * @param crawler the crawler
     * @param code the exit code.
     */
    public void crawlStopped(Crawler crawler, ExitCode code) {
        printAndCloseModelSet();
        printCrawlReport(crawler.getCrawlReport());
        this.finishTime = System.currentTimeMillis();
        this.exitCode = code;
        CrawlManager.crawlStopped();
    }

    /**
     * Returns an RDFContainerFactory
     * @param crawler the Crawler that requests and RDFContainer
     * @param url the URL 
     * @return an RDFContainerFactory
     */
    public RDFContainerFactory getRDFContainerFactory(Crawler crawler, String url) {
        return this;
    }

    /**
     * Returns an RDFContainer for a particular uri
     * @param uri the URIs
     * @return an RDFContainer for a particular URI
     */
    public RDFContainer getRDFContainer(URI uri) {
        // note: by using ModelSet.getModel, all statements added to this Model are added to the ModelSet
        // automatically, unlike ModelFactory.createModel, which creates stand-alone models.

        // when running performance tests, we dump the dataobjects,
        // otherwise we channel the triples into the modelSet
        Model model = null;
        if (modelSet == null) {
            model = RDF2Go.getModelFactory().createModel().open(); 
        } else {
            model = modelSet.getModel(uri);
        }
        return new RDFContainerImpl(model, uri);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// DATA OBJECT PROCESSING METHODS //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void processDataObject(Crawler dataCrawler, DataObject object, String verbosePrefix) {
        nrObjects++;
        this.currentURL = object.getID().toString();
        
        printIfVerbose(verbosePrefix + "," + System.currentTimeMillis() + "," + currentURL);

        if (nrObjects % 300 == 0)
            // call garbage collector from time to time
            System.gc();

        // process the contents of an InputStream, if available
        if (object instanceof FileDataObject) {
            String s = null;
            try {
                process((FileDataObject) object, dataCrawler);
            }
            catch (Exception e) {
                objectsWhereProcessingExceptionOccured++;
                System.err.println("Exception while processing file size (" + s + ") of " + object.getID());
                e.printStackTrace();
            }
        } else {
            printIfVerbose("|no stream");
        }
        
        reportDetectedMimeType(object);
        reportDetectedFullText(object);
        disposeDataObject(object);
        printlnIfVerbose("");
    }

    /**
     * Disposes the data object.
     * @param object the data object to dispose
     */
    protected void disposeDataObject(DataObject object) {
        object.dispose();
    }

    /**
     * Processes the FileDataObject - tries to identify the mime type and to apply an appropriate extraction
     * component - an Extractor, a FileExtractor or a SubCrawler.
     * 
     * @param object the object to process
     * @param crawler the crawler that submitted the object for processing
     * @throws IOException
     * @throws ExtractorException
     * @throws ModelException
     * @throws SubCrawlerException
     */
    @SuppressWarnings("unchecked")
    protected void process(FileDataObject object, Crawler crawler) 
        throws IOException, ExtractorException, ModelException, SubCrawlerException {
        // we cannot do anything when MIME type identification is disabled
        if (!identifyingMimeType) {
            return;
        }

        URI id = object.getID();

        // Create a buffer around the object's stream large enough to be able to reset the stream
        // after MIME type identification has taken place. Add some extra to the minimum array
        // length required by the MimeTypeIdentifier for safety.
        int minimumArrayLength = mimeTypeIdentifier.getMinArrayLength();
        // we don't specify our own buffer size anymore, I commented this out (Antoni Mylka)
        //int bufferSize = Math.max(minimumArrayLength, 8192);
        InputStream contentStream = object.getContent();
        contentStream.mark(minimumArrayLength + 10); // add some for safety

        // apply the MimeTypeIdentifier
        byte[] bytes = IOUtil.readBytes(contentStream, minimumArrayLength);
        String mimeType = mimeTypeIdentifier.identify(bytes, object.getMetadata().getString(NFO.fileName), id);
        if (mimeType != null) {
            // add the MIME type to the metadata
            RDFContainer metadata = object.getMetadata();
            metadata.add(NIE.mimeType, mimeType);

            if (extractingContents) {
                contentStream.reset();
                           
                // apply an Extractor if available
                boolean done = applyExtractor(id, contentStream, mimeType, metadata);
                if (done) {return;}
                
                // else try to apply a FileExtractor
                done = applyFileExtractor(object, id, mimeType, metadata);
                if (done) {return;}
                
                // or maybe apply a SubCrawler
                done = applySubCrawler(id, contentStream, mimeType, object, crawler);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void reportDetectedMimeType(DataObject object) {
        Collection mimeTypes = object.getMetadata().getAll(NIE.mimeType);
        if (mimeTypes == null || mimeTypes.size() == 0) {
            unidentifiedMimeTypes++;
            return;
        } else {
            for (Object mimeTypeObject : mimeTypes) {
                String mimeType = mimeTypeObject.toString();
                printIfVerbose("|mime:" + mimeType);
                Integer oldValue = detectedMimeTypes.get(mimeType);
                if (oldValue == null) {
                    detectedMimeTypes.put(mimeType, Integer.valueOf(1));
                } else {
                    Integer newValue = oldValue + 1;
                    detectedMimeTypes.put(mimeType, newValue);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void reportDetectedFullText(DataObject object) {
        Collection fullTexts = object.getMetadata().getAll(NIE.plainTextContent);
        fullTexts.addAll(object.getMetadata().getAll(NMO.plainTextMessageContent));
        fullTexts.addAll(object.getMetadata().getAll(NID3.unsynchronizedTextContent));
        if (!fullTexts.isEmpty()) {
            numberOfObjectsWithFullText++;
            for (Object fullTextObject : fullTexts) {
                String fullText = fullTextObject.toString();
                totalFulltextLength += fullText.length();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean applyExtractor(URI id, InputStream contentStream, String mimeType, RDFContainer metadata)
            throws ExtractorException, IOException {
        Set extractors = extractorRegistry.getExtractorFactories(mimeType);
        boolean supportedByXmp = xmpExtractorFactory.getSupportedMimeTypes().contains(mimeType);
        boolean result = false;
        byte [] buffer = null;
        
        if (!extractors.isEmpty() && supportedByXmp) {
            buffer = IOUtil.readBytes(contentStream);
        }
        
        if (!extractors.isEmpty()) {
            ExtractorFactory factory = (ExtractorFactory) extractors.iterator().next();
            Extractor extractor = factory.get();
            ThreadedExtractorWrapper wrapper = new ThreadedExtractorWrapper(extractor);
            if (verbose) {
                System.out.print("|ex:" + extractor.getClass().getName());
            }
            if (buffer != null) {
                contentStream = new BufferedInputStream(new ByteArrayInputStream(buffer));
            }
            try {
                wrapper.extract(id, contentStream, null, mimeType, metadata);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (supportedByXmp) {
            Extractor extractor = xmpExtractorFactory.get();
            ThreadedExtractorWrapper wrapper = new ThreadedExtractorWrapper(extractor);
            if (verbose) {
                System.out.print("|ex:" + extractor.getClass().getName());
            }
            if (buffer != null) {
                contentStream = new BufferedInputStream(new ByteArrayInputStream(buffer));
            }
            try {
                wrapper.extract(id, contentStream, null, mimeType, metadata);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean applyFileExtractor(FileDataObject object, URI id, String mimeType, RDFContainer metadata)
            throws ExtractorException, IOException {
        Set fileextractors = extractorRegistry.getFileExtractorFactories(mimeType);
        if (!fileextractors.isEmpty()) {
            FileExtractorFactory factory = (FileExtractorFactory) fileextractors.iterator().next();
            FileExtractor extractor = factory.get();
            File originalFile = object.getFile();
            if (originalFile != null) {
                System.out.print("|fex:" + extractor.getClass().getName());
                extractor.extract(id, originalFile, null, mimeType, metadata);
                return true;
            }
            else {
                File tempFile = object.downloadContent();
                try {
                    System.out.print("|fexd:" + extractor.getClass().getName());
                    extractor.extract(id, tempFile, null, mimeType, metadata);
                    return true;
                }
                finally {
                    if (tempFile != null) {
                        tempFile.delete();
                    }
                }
            }
        }
        else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean applySubCrawler(URI id, InputStream contentStream, String mimeType, DataObject object,
            Crawler crawler) throws SubCrawlerException {
        Set subCrawlers = subCrawlerRegistry.get(mimeType);
        if (!subCrawlers.isEmpty()) {
            SubCrawlerFactory factory = (SubCrawlerFactory) subCrawlers.iterator().next();
            SubCrawler subCrawler = factory.get();
            System.out.print("|sc:" + subCrawler.getClass().getName());
            crawler.runSubCrawler(subCrawler, object, contentStream, null, mimeType);
            return true;
        }
        else {
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////// HELPERS ////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private void printUnexpectedEventWarning(String event) {
        System.err.println("encountered unexpected event (" + event + ") with non-incremental crawler");
    }
    
    private void printIfVerbose(String string) {
        if (verbose) {
            System.out.print(string);
        }
    }
    
    private void printlnIfVerbose(String string) {
        if (verbose) {
            System.out.println(string);
        }
    }
    
    private void printCrawlReport(CrawlReport crawlReport) {
        System.out.println("Crawl report");
        System.out.println("Crawl started: " + new java.util.Date(crawlReport.getCrawlStarted()));
        System.out.println("Crawl stopped: " + new Date(crawlReport.getCrawlStopped()));
        System.out.println("Crawl time: " + (crawlReport.getCrawlStopped() - crawlReport.getCrawlStarted()) + "ms");
        System.out.println("Exit code: " + crawlReport.getExitCode());
        if (crawlReport.getFatalErrorCause() != null) {
            System.out.println("Fatal error cause:");
            crawlReport.getFatalErrorCause().printStackTrace();
        }
        System.out.println("New objects: " + crawlReport.getNewCount());
        System.out.println("Modified objects: " + crawlReport.getChangedCount());
        System.out.println("Unmodified objects: " + crawlReport.getUnchangedCount());
        System.out.println("Deleted objects: " + crawlReport.getRemovedCount());
        System.out.println("New or modified objects with full text: " + numberOfObjectsWithFullText);
        System.out.println("Total length of the extracted full text: " + totalFulltextLength);
        System.out.println("Exceptions while processing objects: " + objectsWhereProcessingExceptionOccured);
        
        if (identifyingMimeType) {
            System.out.println("Objects with unidentified mime types: " + unidentifiedMimeTypes);
            System.out.println("Detected mime types:");
            for (Map.Entry<String, Integer> pair: detectedMimeTypes.entrySet()) {
                System.out.println("  " + pair.getKey() + " : " + pair.getValue());
            }   
        }
    }

    /**
     * Prints the model set to the output file (if necessary) and closes it.
     */
    protected void printAndCloseModelSet() {
        try {
            if (outputFile != null) {
                OutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFile));
                OutputStream stream2 = new BufferedOutputStream(new FileOutputStream("/home/pacman/Desktop/testOutputNTriple"));
                OutputStream stream3 = new BufferedOutputStream(new FileOutputStream("/home/pacman/Desktop/testOutputTrix"));
                
                modelSet.writeTo(stream, Syntax.RdfXml);
                stream.close();
                
                modelSet.writeTo(stream2, Syntax.Ntriples);
                stream2.close();
                
                modelSet.writeTo(stream3, Syntax.Trix);
                stream3.close();
                
                
                System.out.println("Saved RDF model to " + outputFile);
            } else {
                System.out.println("Output discarded");
            }
            if (modelSet != null) {
                modelSet.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the model set with the crawl results
     * @return the model set with the crawl results
     */
    public ModelSet getModelSet() {
        return modelSet;
    }
    
    /**
     * Sets the model set where the crawl results will be stored
     * @param modelSet the model set where the crawl results are to be stored
     */
    public void setModelSet(ModelSet modelSet) {
        this.modelSet = modelSet;
    }

    
    /**
     * @return Returns the startTime.
     */
    public long getStartTime() {
        return startTime;
    }

    
    /**
     * @return Returns the finishTime.
     */
    public long getFinishTime() {
        return finishTime;
    }

    
    /**
     * @return Returns the currentURL.
     */
    public String getCurrentURL() {
        return currentURL;
    }

    
    /**
     * @return Returns the exitCode.
     */
    public ExitCode getExitCode() {
        return exitCode;
    }

    
    /**
     * @return Returns the nrObjects.
     */
    public int getNrObjects() {
        return nrObjects;
    }
    
    public SubCrawlerRegistry getSubCrawlerRegistry() {
        return subCrawlerRegistry;
    }
    
    public TikaMimeTypeIdentifier getMimeTypeIdentifier() {
        return mimeTypeIdentifier;
    }
}
