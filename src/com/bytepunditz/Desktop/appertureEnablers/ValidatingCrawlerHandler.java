/*
 * Copyright (c) 2006 - 2008 Aduna and Deutsches Forschungszentrum fuer Kuenstliche Intelligenz DFKI GmbH.
 * All rights reserved.
 * 
 * Licensed under the Aperture BSD-style license.
 */
package com.bytepunditz.Desktop.appertureEnablers;

import java.io.File;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.exception.ModelException;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.ModelSet;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.semanticdesktop.aperture.crawler.Crawler;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.vocabulary.GEO;
import org.semanticdesktop.aperture.vocabulary.NCAL;
import org.semanticdesktop.aperture.vocabulary.NCO;
import org.semanticdesktop.aperture.vocabulary.NEXIF;
import org.semanticdesktop.aperture.vocabulary.NFO;
import org.semanticdesktop.aperture.vocabulary.NID3;
import org.semanticdesktop.aperture.vocabulary.NIE;
import org.semanticdesktop.aperture.vocabulary.NMO;
import org.semanticdesktop.aperture.vocabulary.TAGGING;
import org.semanticdesktop.nepomuk.nrl.validator.ModelTester;
import org.semanticdesktop.nepomuk.nrl.validator.StandaloneValidator;
import org.semanticdesktop.nepomuk.nrl.validator.ValidationMessage;
import org.semanticdesktop.nepomuk.nrl.validator.ValidationReport;
import org.semanticdesktop.nepomuk.nrl.validator.exception.StandaloneValidatorException;
import org.semanticdesktop.nepomuk.nrl.validator.impl.StandaloneValidatorImpl;
import org.semanticdesktop.nepomuk.nrl.validator.testers.NRLClosedWorldModelTester;

/**
 * An an extension of the SimpleCrawlerHandler that validates the data after the crawl
 */
public class ValidatingCrawlerHandler extends SimpleCrawlerHandler {

    private StandaloneValidator validator;
    private ModelTester[] additionalModelTesters;

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
    public ValidatingCrawlerHandler(boolean identifyingMimeType, boolean extractingContents, boolean verbose, 
            File outputFile, ModelTester [] additionalModelTesters) {
        super(identifyingMimeType, extractingContents, verbose, outputFile);
        try {
            initializeValidator();
            this.additionalModelTesters = additionalModelTesters;
        }
        catch (StandaloneValidatorException sve) {
            throw new RuntimeException(sve);
        }
    }

    /**
     * @see SimpleCrawlerHandler#crawlStopped(Crawler, ExitCode)
     */
    @Override
    public void crawlStopped(Crawler crawler, ExitCode code) {

        
        Model overallModel = RDF2Go.getModelFactory().createModel();
        overallModel.open();
        ModelSet modelSet = getModelSet();
        ClosableIterator<? extends Statement> iterator = modelSet.iterator();
        
        while (iterator.hasNext()) {
            Statement statement = iterator.next();
            overallModel.addStatement(statement);
        }
        
        /*
         * Add the datasource rdf:type nie:DataSource triple, otherwise the validator
         * will complain. The same trick is used in the tests.
         */
        
        overallModel.addStatement(
            crawler.getDataSource().getID(),
            RDF.type,
            NIE.DataSource);
        
        if (additionalModelTesters != null && additionalModelTesters.length > 0) {
            /*
             * If there are any additional model testers, please include them
             */
            ModelTester testers [] = new ModelTester[additionalModelTesters.length + 1];
            testers[0] = new NRLClosedWorldModelTester();
            for (int i = 0; i < additionalModelTesters.length; i++) {
                testers[i+1] = additionalModelTesters[i];
            }
            validator.setModelTesters(testers);
        } else {
            /*
             * If there are no additional model testers, fall back to the default one.
             */
            validator.setModelTesters(new NRLClosedWorldModelTester());
        }
        
        try {
            System.out.println("Performing validation");
            ValidationReport report = validator.validate(overallModel);
            if (report.getMessages().size() > 0) {
                System.out.println("Validation report:");
                printValidationReport(report);
            } else {
                System.out.println("No problems detected");
            }
        }
        catch (StandaloneValidatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        super.crawlStopped(crawler, code);
    }

    private void printValidationReport(ValidationReport report) {
        List<ValidationMessage> messages = report.getMessages();
        int i = 1;
        for (ValidationMessage msg : messages) {
            System.out.print("" + i + ": ");
            System.out.println(msg.getMessageType().toString() + " ");
            System.out.println("   " + msg.getMessageTitle() + " ");
            System.out.println("   " + msg.getMessage() + " ");
            for (Statement stmt : msg.getStatements()) {
                System.out.println("   {" + stmt.getSubject().toSPARQL() + ",");
                System.out.println("    " + stmt.getPredicate().toSPARQL() + ",");
                System.out.println("    " + stmt.getObject().toSPARQL() + "}");
            }
            i++;
        }
    }

    private void initializeValidator() throws StandaloneValidatorException {
        validator = new StandaloneValidatorImpl();
        Model tempModel = RDF2Go.getModelFactory().createModel();
        tempModel.open();

        NIE.getNIEOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NIE.NS_NIE));
        tempModel.removeAll();

        NCO.getNCOOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NCO.NS_NCO));
        tempModel.removeAll();

        NFO.getNFOOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NFO.NS_NFO));
        tempModel.removeAll();

        NMO.getNMOOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NMO.NS_NMO));
        tempModel.removeAll();

        NCAL.getNCALOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NCAL.NS_NCAL));
        tempModel.removeAll();

        NEXIF.getNEXIFOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NEXIF.NS_NEXIF));
        tempModel.removeAll();

        NID3.getNID3Ontology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(NID3.NS_NID3));
        tempModel.removeAll();

        TAGGING.getTAGGINGOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(TAGGING.NS_TAGGING));
        tempModel.removeAll();

        GEO.getGEOOntology(tempModel);
        validator.addOntology(tempModel, getOntUriFromNs(TAGGING.NS_TAGGING));
        tempModel.removeAll();

        tempModel.close();

        validator.setModelTesters(new NRLClosedWorldModelTester());
    }
    
    private String getOntUriFromNs(URI uri) {
        return uri.toString().substring(0, uri.toString().length() - 1);
    }
}
