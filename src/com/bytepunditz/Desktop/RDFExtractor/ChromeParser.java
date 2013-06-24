package com.bytepunditz.Desktop.RDFExtractor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.sql.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.Syntax;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;
import org.semanticdesktop.aperture.rdf.util.ModelUtil;
import org.semanticdesktop.aperture.util.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bytepunditz.Desktop.crawlManager.CrawlConfig;
import com.bytepunditz.Desktop.vocabulary.*;


public class ChromeParser {
	public static final Logger LOG = LoggerFactory.getLogger(ChromeParser.class);

	RDFContainer container = null;
	RDFContainerFactoryImpl factory = null;
	Model model = null;
	Connection conn = null;
	String chromeBaseURL = "";

	//	map<int, String> idUrlMap;

	//	String dbFileName = "/home/pacman/Desktop/crome_backup/History";
	public ChromeParser(String chromeBaseUrl) {

		//Resource RDFcontext = new URIImpl(content.getBaseUrl());
		//Resource id = RDFcontext;

		factory = new RDFContainerFactoryImpl();
		container = factory.newInstance("http://www.bigants.com/ontologies/2007/05/10/Browser#");
		model = container.getModel();
		this.chromeBaseURL = chromeBaseUrl;
		if(model == null)
		{
			System.out.println("Nishant CODE RED!!! model null");
		}


	}


	public RDFContainer crawlAndParse()
	{
		parseHistory();
		parseBookmark();
		parseDownloads();
		parseFavourites();
		
		if(container != null)
		{
			String filename = CrawlConfig.getRDFOutputFileName();
			try {
				OutputStream stream = new BufferedOutputStream(new FileOutputStream(filename, true));
				container.getModel().writeTo(stream, Syntax.Ntriples);
				stream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return container;

	}

	void parseFavourites()
	{
		String dbFile = chromeBaseURL+ "/Top Sites";
		try {
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			conn = DriverManager
					.getConnection("jdbc:sqlite:" + dbFile);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try{

			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from thumbnails");
			while (firstRes.next()) 
			{
				String url = firstRes.getString("url");

				Resource urlURI = new URIImpl(url);
				int urlRank = firstRes.getInt("url_rank");
				String title = firstRes.getString("title");
				Double boringScore = firstRes.getDouble("boring_score");

				Resource randomURI =  ModelUtil.generateRandomURI(model);
				container.add(container.getModel().createStatement(randomURI, BrowserOntology.TYPE, BrowserOntology.FAVOURITES));
				container.add(BrowserOntology.HASURL, urlURI) ;
				container.add(BrowserOntology.URLTITLE, title);
				container.add(BrowserOntology.RANK, urlRank);
				container.add(BrowserOntology.BORINGSCORE, boringScore.toString());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}


	void parseDownloads()
	{
		String dbFile = chromeBaseURL+ "/History";
		try {
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			conn = DriverManager
					.getConnection("jdbc:sqlite:" + dbFile);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		try{

			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from downloads");
			while (firstRes.next()) 
			{
				String pathOnSystem =  firstRes.getString("full_path");
				String modifiedpathOnString = "file:";
				modifiedpathOnString = modifiedpathOnString + pathOnSystem;
				//				System.out.println(modifiedpathOnString + "::" + pathOnSystem);
				URI pathOnSystemURI = new URIImpl(HttpClientUtil.formUrlEncode(modifiedpathOnString, "/:%!?&+.="));
				String pathOnWeb = firstRes.getString("url");
				URI pathOnWebURI = new URIImpl(pathOnWeb);
				int startTime = firstRes.getInt("start_time");

				URI randomURI = ModelUtil.generateRandomURI(model);
				container.add(model.createStatement(randomURI, BrowserOntology.TYPE, BrowserOntology.DOWNLOADS));
				container.add(BrowserOntology.HASURL, pathOnWebURI);
				container.add(BrowserOntology.DOWNLOADEDTO, pathOnSystemURI);
				container.add(BrowserOntology.DATE_ADD, startTime);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
		 *   
		 *   Otimization scope TODO Assuming searches are low in number, we will cache for keywords in bracket of 1000's ID. Data we have to cache will roughly 
		 *   be 50. 
		 */
		String dbFile = chromeBaseURL+ "/History";
		try {
			Class.forName("org.sqlite.JDBC");
			//TODO get the local DB passedin the argument
			conn = DriverManager
					.getConnection("jdbc:sqlite:" + dbFile);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		Map<Integer,String> idUrlMap = new HashMap<Integer,String>();
		try {

			Statement firstStat = conn.createStatement();
			ResultSet firstRes = firstStat.executeQuery("select * from urls");
			while (firstRes.next()) {
				Integer id =  firstRes.getInt("id");
				String url =  firstRes.getString("url");
				idUrlMap.put(id, url);
			}


			Statement stat = conn.createStatement();
			ResultSet res = stat.executeQuery("select * from urls");
			while (res.next()) {

				Integer id =  res.getInt("id");
				String url =  res.getString("url");
				URI urlURI = new URIImpl(HttpClientUtil.formUrlEncode(url,"/:%!?&+.="));
				URI randomURI = ModelUtil.generateRandomURI(model);

				String urlTitle = res.getString("title");

				String lastVisitTime = res.getString("last_visit_time");

				int visitcount = res.getInt("visit_count");

				container.add(model.createStatement(randomURI, BrowserOntology.TYPE, BrowserOntology.HISTORY));
				try {
					container.add(BrowserOntology.HASURL, urlURI);
				} catch (Exception e) {
					System.out.println("Nishant url = " + urlURI);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				container.add(BrowserOntology.URLTITLE, urlTitle);
				container.add(BrowserOntology.LASTVISITTIME, lastVisitTime);
				container.add(BrowserOntology.VISITCOUNT, visitcount);

				//getting the info from visits table
				Statement visitStat = conn.createStatement();
				ResultSet visitRes = visitStat.executeQuery("select * from visits where url = " + id);
				while (visitRes.next()) {
					int visit_time = visitRes.getInt("visit_time");
					int from_visit = visitRes.getInt("from_visit");
					int transition = visitRes.getInt("transition");
					container.add(BrowserOntology.EACHVISITTIME, visit_time);
					if(from_visit != 0)
					{
						//System.out.println("fromvisit: " + from_visit + "::" +idUrlMap.get(from_visit));
						String fromUrl = idUrlMap.get(from_visit);
						if(fromUrl != null)
						{
							URI fromUrlURI = new URIImpl(fromUrl);
							container.add(BrowserOntology.FROMURLVISIT, fromUrlURI);
						}

					}
					switch(transition & 0XFF)
					{
					case ChromeEnums.PAGE_TRANSITION_LINK:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_LINK);
						break;
					case ChromeEnums.PAGE_TRANSITION_TYPED:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_TYPED);
						break;

					case ChromeEnums.PAGE_TRANSITION_AUTO_BOOKMARK:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_AUTO_BOOKMARK);
						break;

					case ChromeEnums.PAGE_TRANSITION_AUTO_SUBFRAME:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_AUTO_SUBFRAME);
						break;

					case ChromeEnums.PAGE_TRANSITION_MANUAL_SUBFRAME:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_MANUAL_SUBFRAME);
						break;

					case ChromeEnums.PAGE_TRANSITION_GENERATED:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_GENERATED);
						break;

					case ChromeEnums.PAGE_TRANSITION_START_PAGE:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_START_PAGE);
						break;

					case ChromeEnums.PAGE_TRANSITION_FORM_SUBMIT:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_FORM_SUBMIT);
						break;

					case ChromeEnums.PAGE_TRANSITION_RELOAD:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_RELOAD);
						break;

					case ChromeEnums.PAGE_TRANSITION_KEYWORD:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_KEYWORD);
						break;

					case ChromeEnums.PAGE_TRANSITION_KEYWORD_GENERATED:
						container.add(BrowserOntology.TRANSITIONTYPE, BrowserOntology.PAGE_TRANSITION_KEYWORD_GENERATED);
						break;
					}// End of switch

				} //end of query from visits


				//getting the info from the search 
				Statement keywordStat = conn.createStatement();
				ResultSet keywordRes = keywordStat.executeQuery("select * from keyword_search_terms where url_id = " + id);
				while (keywordRes.next()) {
					String keywordTitle = keywordRes.getString("term");
					container.add(BrowserOntology.SEARCHKEYWORD, keywordTitle);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
	}


	void parseBookmark() 
	{

		System.out.println("Nishant parseBookmark");
		FileInputStream stream;
		JsonNode root = null;
		try {
			stream = new FileInputStream(new File("/home/pacman/Desktop/chromeBookmarks.json"));

			ObjectMapper mapper = new ObjectMapper();
			//		  TwitterEntry entry = mapper.readValue(new File("input.json"));
			root = mapper.readTree(stream);
			JsonNode rootNode = root.path("roots");

			//parsing bookmark_bar
			JsonNode bookMarkNode = rootNode.path("bookmark_bar");
			JsonNode dateAdd =  bookMarkNode.path("date_added");
			System.out.println("Nishant date_added = "  + dateAdd.getTextValue());

			JsonNode childrenBookMark = bookMarkNode.get("children");
			if(childrenBookMark != null)
			{
				Resource context = ModelUtil.generateRandomResource(model);
				parseChildren(childrenBookMark, context, "bookmark_bar");
			}

			//parsing other 
			JsonNode otherNode = rootNode.path("other");
			JsonNode childrenOther = otherNode.get("children");
			if(childrenOther != null)
			{
				Resource context = ModelUtil.generateRandomResource(model);
				parseChildren(childrenOther, context, "other");
			}


			//parsing synced
			JsonNode syncedNode = rootNode.path("synced");
			JsonNode childrensynced = syncedNode.get("children");
			if(childrensynced != null)
			{
				Resource context = ModelUtil.generateRandomResource(model);
				parseChildren(childrensynced, context, "synced");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	void parseChildren(JsonNode children, Resource context,String parentFolderName)
	{
		Iterator<JsonNode> itr = children.getElements();

		while(itr.hasNext()){
			JsonNode node = itr.next();
			if(node.get("children") != null)
			{
				String addDate = node.get("date_added").toString();
				String modifiedDate = node.get("date_modified").toString();
				String folderName = node.get("name").toString();
				Resource contextNew = ModelUtil.generateRandomResource(model);

				try {
					model.addStatement(contextNew, BrowserOntology.TYPE, BrowserOntology.BOOKMARKS.toString(), context.toString());
					model.addStatement(contextNew, BrowserOntology.BOOKMARKTYPE, BrowserOntology.BOOKMARKTYPEFOLDER.toString(), context.toString());
					model.addStatement(contextNew, BrowserOntology.DATE_ADD, addDate, context.toString());
					model.addStatement(contextNew, BrowserOntology.DATE_MODIFIED, modifiedDate, context.toString());
					model.addStatement(contextNew, BrowserOntology.FOLDERNAME, folderName, context.toString());
					model.addStatement(contextNew, BrowserOntology.PARENTFOLDERNAME, parentFolderName, context.toString());


				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//treating it as folder
				LOG.debug("Nishant Bookmark: date_added::" + node.get("date_added"));
				LOG.debug("Nishant Bookmark: date_modified::" + node.get("date_modified"));
				LOG.debug("Nishant Bookmark: folder::" + node.get("name"));

				System.out.println("Recursion called!!");
				parseChildren(node.get("children"), contextNew, node.get("name").toString());
			}
			else
			{
				Resource urlID = ModelUtil.generateRandomResource(model);
				String addDate = node.get("date_added").toString();
				URI url = null;
				String encodedUrl = HttpClientUtil.formUrlEncode(node.get("url").toString().trim(),"/:%!?&+.=");
				//encoding  appends %22 in the begining. for temp workaround we are deleting those char.
				if(encodedUrl.startsWith("%22"))
				{
					encodedUrl = encodedUrl.substring(3);
				}
				
				try {
					url = new URIImpl(encodedUrl);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("Issue with the url:" +node.get("url").toString().trim() + "....skipping");
					continue;
				}

				String urlTitle = node.get("name").toString();


				try {
					model.addStatement(urlID, BrowserOntology.TYPE, BrowserOntology.BOOKMARKS.toString(), context.toString());
					model.addStatement(urlID, BrowserOntology.BOOKMARKTYPE, BrowserOntology.BOOKMARKTYPEURL.toString(), context.toString());
					model.addStatement(urlID, BrowserOntology.HASURL, url.toString(), context.toString());
					model.addStatement(urlID, BrowserOntology.URLTITLE, urlTitle, context.toString());
					model.addStatement(urlID, BrowserOntology.DATE_ADD, addDate, context.toString());
					model.addStatement(urlID, BrowserOntology.PARENTFOLDERNAME, parentFolderName, context.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				//TODO change the date to EPOC
				LOG.debug("Nishant Bookmark: date_added::" + node.get("date_added"));
				LOG.debug("Nishant Bookmark: url::" + node.get("url"));
				LOG.debug("Nishant Bookmark: name::" + node.get("name"));

				/*"date_added": "12985356554557876",
	            "id": "4",
	            "name": "GoldenOrb Scalability",
	            "type": "url",
	            "url": "http://wwwrel.ph.utexas.edu/Members/jon/golden_orb/"*/
				System.out.println(node.get("id").getTextValue()  + "::" +node.get("name").getTextValue()  );
			}

		}

	}


}

