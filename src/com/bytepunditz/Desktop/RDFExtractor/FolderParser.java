package com.bytepunditz.Desktop.RDFExtractor;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.nutch.protocol.Content;
import org.ontoware.rdf2go.exception.ModelRuntimeException;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.ontoware.rdf2go.vocabulary.RDF;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.aperture.vocabulary.NFO;
import org.semanticdesktop.aperture.vocabulary.NIE;


public class FolderParser {

	public RDFContainer crawlAndParse(Content content)
	{
		Resource RDFcontext = new URIImpl(content.getBaseUrl());

		RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
		RDFContainer container = factory.newInstance(RDFcontext.toString());

		//        Model model = container.getModel();



		try{

			File file = new File(content.getBaseUrl());
			//			model.addStatement(id, RDF.type, NFO.FileDataObject);
			container.add(RDF.type,NFO.FileDataObject);
			//con.add(fac.createStatement((Resource)id, RDF.TYPE, NFO.FileDataObject, (Resource)RDFcontext));

			//last modified date
			long lastModified = file.lastModified();
			if (lastModified != 0l) {
				container.add(NFO.fileLastModified, new Date(lastModified));
				//				con.add(fac.createStatement((Resource)id, NFO.fileLastModified, (Value)new Date(lastModified), (Resource)RDFcontext));

			}

			//filename
			String name = file.getName();
			if (name != null) {
				container.add(NFO.fileName, name);
				//				con.add(fac.createStatement((Resource)id, NFO.fileName, fac.createLiteral(name), (Resource)RDFcontext));

			}

			File parent = file.getParentFile();
			if (parent != null) {
				URI parentUri =new URIImpl(parent.toString());
				container.add(NFO.belongsToContainer, parentUri);
				container.add(NIE.isPartOf, parentUri);
				container.add(container.getModel().createStatement(parentUri, RDF.type, NFO.Folder));
			}

			//child files info
			File fileList[] = file.listFiles();
			if(fileList != null)
			{
				for (File child: fileList)
				{
					if(child != null)
					{
						URI childURI = null;
						try{
							childURI = new URIImpl(child.toString());
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
						container.add(NIE.hasPart, childURI);
						//						LOG.error("Nishant Triple7:" + id + " " + NIE.hasPart + " " + childURI);
						//						con.add(fac.createStatement((Resource)id, NIE.hasPart, (Value)childURI, (Resource)RDFcontext));
						container.add(RDF.type, NFO.FileDataObject);
						//						LOG.error("Nishant Triple8:" + childURI + " " + RDF.TYPE + " " + NFO.FileDataObject);
						//						con.add(fac.createStatement((Resource)childURI, RDF.TYPE, (Value)NFO.FileDataObject, (Resource)RDFcontext));

					}
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		try {
			System.out.println("Nishant output");
			container.getModel().writeTo(System.out, Syntax.RdfXml);
		} catch (ModelRuntimeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return container;

	}


}
