package com.bytepunditz.Desktop.RDFExtractor;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import org.apache.nutch.protocol.Content;
import org.ontoware.rdf2go.model.Syntax;
import org.semanticdesktop.aperture.rdf.RDFContainer;

import com.bytepunditz.Desktop.crawlManager.CrawlConfig;


public class RDFExtractor {
	
	
	public void generateRDFQuad(Content content)
	{
		String mimeType = content.getContentType();
		String fileName = content.getBaseUrl();
		RDFContainer container = null;
		
		System.out.println("Nishant inside RDF Extractor with file = " + content.getBaseUrl()+ "and mime = " + content.getContentType());
		
		if( mimeType.equals("application/mbox"))
		{
			MBoxParser parser = new MBoxParser();
			parser.parseAndCrawl(fileName);
		}
		else if(mimeType.equals("audio/mpeg"))
		{
			MP3Parser parser = new MP3Parser();
			container = parser.parseAndCrawl(fileName);
		}
		else if(mimeType.endsWith("text/html"))
		{
			FolderParser parser = new FolderParser();
			container = parser.crawlAndParse(content);
		}
		else if (mimeType.equals("application/x-markaby"))
		{
			ThunderBirdParser parser = new ThunderBirdParser();
			container = parser.crawlAndParse(fileName);
		}
		else
		{
			System.out.println("Unhandled MimeType = " + mimeType);
		}
	
		//TODO Nishant call the PIMO module with the RDF container.
		//Right now dumping in a file called RDF result
		if(container != null)
		{
			String outputFilename = CrawlConfig.getRDFOutputFileName();
			try {
				OutputStream stream = new BufferedOutputStream(new FileOutputStream(outputFilename, true));
				container.getModel().writeTo(stream, Syntax.Ntriples);
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	

}
