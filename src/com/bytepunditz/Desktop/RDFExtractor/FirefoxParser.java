package com.bytepunditz.Desktop.RDFExtractor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.Literal;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.ValueFactory;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.aperture.rdf.util.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytepunditz.Desktop.vocabulary.BrowserOntology;

public class FirefoxParser {
	public static final Logger LOG = LoggerFactory.getLogger(ChromeParser.class);
	RDFContainer container = null;
	RDFContainerFactoryImpl factory = null;
	ValueFactory fac = null;
	Model model = null;
	Connection conn = null;
	
	//trasition variables
	public  static final int TRANSITION_LINK = 1;
	public static final int TRANSITION_TYPED = 2;	
	public static final int TRANSITION_BOOKMARK = 3;
	public static final int TRANSITION_EMBED = 4;
	public static final int TRANSITION_REDIRECT_PERMANENT = 5;
	public static final int TRANSITION_REDIRECT_TEMPORARY = 6;
	public static final int TRANSITION_DOWNLOAD = 7;	

	
	public FirefoxParser() {
		
		try {
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			conn = DriverManager.getConnection("jdbc:sqlite:/home/pacman/Desktop/firefox_bkp/places.sqlite");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        factory = new RDFContainerFactoryImpl();
        container = factory.newInstance("Browser");
        model = container.getModel();
        fac = container.getValueFactory();
        if(model == null)
        {
        	System.out.println("Nishant CODE RED!!! model null");
        }

	}
	
	RDFContainer crawlAndParse()
	{
		parseHistory();
		parseBookMark();
		parseDownloads();
		
		return container;
		
	}
	
	void parseHistory()
	{
		/*
		 *Here we have two option, either for every urlID, query the visits and search table or read once and maintain the cache.
		 *Implemented the first approach
		 *	Advantage: 
		 *		i)All the RDF related to single url will be inside same chunk and easy for any infrencing or provenance query.
		 *		ii) Simple design
		 *		iii) No need to maintain any cache for every url which can be big if history has old data. for 1 month, url count > 15k
		 *  diadvantage: 
		 *  	i) for every url, we are hitting the disc twice for visits and keyword  Need to investigate if sqlite use ample caching for this
		 *  		to not become very slow.	
		 *   */
		Map<Integer,String> idUrlMap = new HashMap<Integer,String>();
		
		//to convert visitID and placeID
		Map<Integer,Integer> visitPlaceIdMap = new HashMap<Integer,Integer>();
		
		try {
			
			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from moz_places");
			while (firstRes.next()) {
				Integer id =  firstRes.getInt("id");
				String url =  firstRes.getString("url");
				idUrlMap.put(id, url);
			}

			Statement secondStat = conn.createStatement();
			ResultSet secondRes = secondStat.executeQuery("select * from moz_historyvisits");
			while (secondRes.next()) {
				Integer id =  secondRes.getInt("id");
				Integer place_id =  secondRes.getInt("place_id");
				visitPlaceIdMap.put(id, place_id);
			}

			Statement stat = conn.createStatement();
			ResultSet res = stat.executeQuery("select * from moz_places");
			while (res.next()) {

				Integer id =  res.getInt("id");
				String url =  res.getString("url");
				URI urlURI = new URIImpl(url);
				Resource randomURI = ModelUtil.generateRandomURI(model);

				String urlTitle = res.getString("title");

				if(urlTitle != null)
				{
					container.add(fac.createStatement(randomURI, BrowserOntology.URLTITLE, fac.createLiteral(urlTitle)));
				}
				

				String lastVisitTime = res.getString("last_visit_date");
				if(lastVisitTime != null)
				{
					Literal lastvisitTimeLiteral = fac.createLiteral(lastVisitTime);
					container.add(fac.createStatement(randomURI, BrowserOntology.LASTVISITTIME, lastvisitTimeLiteral));
				}

				int visitcount = res.getInt("visit_count");
				Literal visitcountLiteral = fac.createLiteral(visitcount);

				container.add(fac.createStatement(randomURI, BrowserOntology.TYPE, BrowserOntology.HISTORY));
				container.add(fac.createStatement(randomURI, BrowserOntology.HASURL, urlURI));
				container.add(fac.createStatement(randomURI, BrowserOntology.VISITCOUNT, visitcountLiteral));
				
				//getting the info from historyvisits table
				Statement visitStat = conn.createStatement();
				ResultSet visitRes = visitStat.executeQuery("select * from moz_historyvisits where place_id = " + id);
				while (visitRes.next()) {
					
					//getting each visiting time
					int visit_date = visitRes.getInt("visit_date");
					Literal visitTimeLiteral = fac.createLiteral(visit_date);
					container.add(fac.createStatement(randomURI, BrowserOntology.EACHVISITTIME, visitTimeLiteral));
					
					//getting the from link 
					int from_visit = visitRes.getInt("from_visit");
					//get the URL for from _visit
					if(from_visit != 0)
					{
						int placeID = visitPlaceIdMap.get(from_visit);
						String fromUrl = idUrlMap.get(placeID);
						URI fromUrlURI = fac.createURI(fromUrl);
						container.add(fac.createStatement(randomURI, BrowserOntology.FROMURLVISIT, fromUrlURI));
					}
					
					//getting the visit type
					int transitionType = visitRes.getInt("visit_type");
					switch(transitionType)
					{
					case TRANSITION_LINK :
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_LINK));
						break;
					case TRANSITION_TYPED:
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_TYPED));
						break;

					case TRANSITION_BOOKMARK :
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_AUTO_BOOKMARK));
						break;
					case TRANSITION_EMBED:
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_AUTO_SUBFRAME));
						break;

					case TRANSITION_REDIRECT_PERMANENT:
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_REDIRECT_PERMANENT));
						break;
						
					case TRANSITION_REDIRECT_TEMPORARY:
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_REDIRECT_TEMPORORAY));
						break;
						
					case TRANSITION_DOWNLOAD:	
						container.add(fac.createStatement(randomURI, BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_DOWNLOAD));
						break;
					}//end of switch
					
					
					//recording the session information
					int session = visitRes.getInt("session");
					Literal sessionLiteral = fac.createLiteral(session);
					container.add(fac.createStatement(randomURI, BrowserOntology.SESSIONS, sessionLiteral));
					
					
				} //end of query from visits
			
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
	}

	void parseBookMark()
	{
		//This map holds the id and url from the places tab table
		Map<Integer,String> idUrlMap = new HashMap<Integer,String>();
		
		//map to hole the URI of the bookmark URI
		Map<Integer,URI> idURIMap = new HashMap<Integer,URI>();

		String idList = null;
		
		try{
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			Connection conn =
					DriverManager.getConnection("jdbc:sqlite:/home/pacman/Desktop/firefox_bkp/places.sqlite");

			//caching all the id vs url in a map.
			Statement stat = conn.createStatement();
			ResultSet res = stat.executeQuery("select fk from moz_bookmarks");
			while (res.next()) 
			{
				int id = res.getInt("fk");
				if(id != 0)
				{
					idList = idList + ", " + id;
				}
			}
			
			Statement idUrlStat = conn.createStatement();
			String query = "select id, url from moz_places where id in (" + idList + " )";
			ResultSet idUrlSet = idUrlStat.executeQuery(query);
			while(idUrlSet.next())
			{
				int id = idUrlSet.getInt("id");
				String url = idUrlSet.getString("url");
				idUrlMap.put(id, url);
			}
			
			
			//queryon the bookmar table
			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from moz_bookmarks");
			while (firstRes.next()) 
			{
				int id =  firstRes.getInt("id");
				String url = idUrlMap.get(id);
				if(url == null)
				{
					continue;
				}
				
				URI urlURI= fac.createURI(url);
				
				int type = firstRes.getInt("type");
				int parent = firstRes.getInt("parent");
				URI parentURI = null;
				if(parent != 0)
				{
					parentURI = idURIMap.get(id);
				}
				
				
				String title = firstRes.getString("title");
				if(title.isEmpty())
				{
					continue;
				}
				Literal folderName = fac.createLiteral(title);
				
				int dateAdded =  firstRes.getInt("dateAdded");
				int dateModified = firstRes.getInt("lastModified");
				Literal addDate = fac.createLiteral(dateAdded);
				Literal modifiedDate = fac.createLiteral(dateModified);
				
				Resource contextNew = ModelUtil.generateRandomResource(model);
				
				container.add(fac.createStatement(contextNew, BrowserOntology.TYPE, BrowserOntology.BOOKMARKS));
				container.add(fac.createStatement(contextNew, BrowserOntology.DATE_ADD, addDate));
				container.add(fac.createStatement(contextNew, BrowserOntology.DATE_MODIFIED, modifiedDate));
				
				if(parentURI != null)
				{
					container.add(fac.createStatement(contextNew, BrowserOntology.PARENTFOLDERNAME, parentURI));
				}

				
				if(type == 2)
				{
					container.add(fac.createStatement(contextNew, BrowserOntology.BOOKMARKTYPE, BrowserOntology.BOOKMARKTYPEFOLDER));
					container.add(fac.createStatement(contextNew, BrowserOntology.FOLDERNAME, folderName));
				}
				else if(type == 1)
				{
					container.add(fac.createStatement(contextNew, BrowserOntology.BOOKMARKTYPE, BrowserOntology.BOOKMARKTYPEURL));
					container.add(fac.createStatement(contextNew, BrowserOntology.HASURL, urlURI));
				}
			}
		}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}
	
	void parseDownloads()
	{
		try{
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			Connection conn =
					DriverManager.getConnection("jdbc:sqlite:/home/pacman/Desktop/firefox_bkp/downloads.sqlite");

			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from moz_downloads");
			while (firstRes.next()) 
			{
				String pathOnSystem =  firstRes.getString("target");
				String modifiedpathOnString = "file:";
				modifiedpathOnString = modifiedpathOnString + pathOnSystem;
//				System.out.println(modifiedpathOnString + "::" + pathOnSystem);
				URI pathOnSystemURI = fac.createURI(modifiedpathOnString);
				String pathOnWeb = firstRes.getString("source");
				URI pathOnWebURI = fac.createURI(pathOnWeb);
				int startTime = firstRes.getInt("startTime");
				Literal startTimeLiteral = fac.createLiteral(startTime);
				
				String fileName = firstRes.getString("name");
				Literal fileNameLiteral = fac.createLiteral(fileName);
				
				String mimeType = firstRes.getString("mimeType");
				Literal mimeTypeLiteral = fac.createLiteral(mimeType);
				
				String fromURL = firstRes.getString("referrer");
				URI fromURLURI = fac.createURI(fromURL);
				
				URI randomURI = ModelUtil.generateRandomURI(model);
				container.add(fac.createStatement(randomURI, BrowserOntology.TYPE, BrowserOntology.DOWNLOADS));
				container.add(fac.createStatement(randomURI, BrowserOntology.HASURL, pathOnWebURI));
				container.add(fac.createStatement(randomURI, BrowserOntology.DOWNLOADEDTO, pathOnSystemURI));
				container.add(fac.createStatement(randomURI, BrowserOntology.DATE_ADD, startTimeLiteral));
				container.add(fac.createStatement(randomURI, BrowserOntology.FILENAME, fileNameLiteral));
				container.add(fac.createStatement(randomURI, BrowserOntology.FILEMIMETYPE, mimeTypeLiteral));
				container.add(fac.createStatement(randomURI, BrowserOntology.FROMURLVISIT, fromURLURI));
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


}
