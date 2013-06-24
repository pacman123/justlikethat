package com.bytepunditz.Desktop.RDFExtractor;


import java.io.File;
import java.io.IOException;

import smiy.pmkb.extractor.audiodocuments.mp3.MP3FileExtractor;

import org.apache.tika.parser.mp3.Mp3Parser;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.semanticdesktop.aperture.extractor.mp3.bundle.Mp3ExtractorActivator;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;

public class MP3Parser {
	
	
	
	public RDFContainer parseAndCrawl(String fileName)
    {
		// create a data source configuration
        RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
        RDFContainer container = factory.newInstance("http://www.bigants.com/ontologies/2007/05/10/MP3Parser#Mp3File");
        
        //strip the file prefix of the string.
        if(fileName.startsWith("file:"));
        {
        	fileName = fileName.substring(5);
        }
        
        // start crawling and exit afterwards
        File mp3File = new File(fileName);
        try {
        	MP3FileExtractor extractor = new MP3FileExtractor();
        	//protected void performExtraction(URI id, File file, Charset charset,
//			String mimeType, RDFContainer result) throws ExtractorException
        	URI id = new URIImpl(fileName);
        	extractor.performExtraction(id, mp3File, null, "", container);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return container;
    }
	
	public static void main( String[] args)
	{
		MP3Parser p = new MP3Parser();
		RDFContainer container = p.parseAndCrawl("/home/pacman/Desktop/desktopCrawlSampleFolder/Banjara.mp3");
		try {
			System.out.println("Nishant output");
			container.getModel().writeTo(System.out, Syntax.RdfXml);
		} catch (ModelRuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
