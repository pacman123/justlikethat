package com.bytepunditz.Desktop.vocabulary;

import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;
;
/*
 * Test bRowser ontology. will map it to other available ontology when available.
 */
public class BrowserOntology {
	 
	public static final URI TEST = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#");
	
	
	//type
	public static final URI TYPE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Type");
	public static final URI BOOKMARKS = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Bookmark");
	public static final URI HISTORY = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#History");
	public static final URI DOWNLOADS = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Downloads");
	public static final URI FAVOURITES = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Favourites");
	
	//common
	public static final URI HASURL = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BookmarkUrl");
	public static final URI URLTITLE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BookmarkUrlName");
	public static final URI DATE_ADD = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#DateAdd");
	public static final URI DATE_MODIFIED = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#DateModified");
	
	
	
	//BookMark specifics
	public static final URI FOLDERNAME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Folder");
	public static final URI BOOKMARKTYPE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BookmarkType");
	public static final URI BOOKMARKTYPEFOLDER = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BookmarkTypeFolder");
	public static final URI BOOKMARKTYPEURL = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BookmarkTypeUrl");
	public static final URI PARENTFOLDERNAME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#ParentFolderName");
	
	
	//history related specifics.
	public static final URI VISITCOUNT = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#VisitCount");
	public static final URI LASTVISITTIME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#LastVisittime");
	public static final URI EACHVISITTIME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#EachVisitTime");
	public static final URI FROMURLVISIT = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#FromUrlVisit");
	public static final URI SESSIONS = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Sessions");
	
	//page transition types
	public static final URI TRANSITIONTYPE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#TransitionType");
	
	public static final URI PAGE_TRANSITION_LINK = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionLink");
	public static final URI PAGE_TRANSITION_TYPED = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionTyped");
	public static final URI PAGE_TRANSITION_AUTO_BOOKMARK = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionAutoBookmark");
	public static final URI PAGE_TRANSITION_AUTO_SUBFRAME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionAutoSubframe");
	public static final URI PAGE_TRANSITION_MANUAL_SUBFRAME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionManualsubframe");
	public static final URI PAGE_TRANSITION_GENERATED = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionGenerated");
	public static final URI PAGE_TRANSITION_START_PAGE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionStartPage");
	public static final URI PAGE_TRANSITION_FORM_SUBMIT = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionFormSubmit");
	public static final URI PAGE_TRANSITION_RELOAD = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionReload");
	public static final URI PAGE_TRANSITION_KEYWORD = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionKeyword");
	public static final URI PAGE_TRANSITION_KEYWORD_GENERATED = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionKeywordGenerated");
	public static final URI PAGE_TRANSITION_REDIRECT_PERMANENT = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionRedirectPermanent");
	public static final URI PAGE_TRANSITION_REDIRECT_TEMPORORAY = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionRedirectTempororay");
	public static final URI PAGE_TRANSITION_DOWNLOAD = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#PageTransitionDownload");
	
	
	public static final URI SEARCHKEYWORD = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#SearchKeyword");
	
	//Downloads
	public static final URI DOWNLOADEDTO = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#DownloadedTo");
	public static final URI FILENAME = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#FileName");
	public static final URI FILEMIMETYPE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#fileMimeType");
	
	
	//Favourites
	public static final URI RANK = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#Rank");
	public static final URI BORINGSCORE = new URIImpl("http://www.bigants.com/ontologies/2007/05/10/Browser#BoringScore");
	
	    

}
