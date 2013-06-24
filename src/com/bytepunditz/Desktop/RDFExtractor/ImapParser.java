package com.bytepunditz.Desktop.RDFExtractor;
import java.util.Iterator;
import java.util.List;

import org.ontoware.rdf2go.exception.ModelException;
import org.semanticdesktop.aperture.crawler.ExitCode;
import org.semanticdesktop.aperture.crawler.imap.ImapCrawler;
import org.semanticdesktop.aperture.datasource.imap.ImapDataSource;
import org.semanticdesktop.aperture.datasource.imap.ImapDataSource.ConnectionSecurity;
import org.semanticdesktop.aperture.rdf.RDFContainer;
import org.semanticdesktop.aperture.rdf.impl.RDFContainerFactoryImpl;

import com.bytepunditz.Desktop.appertureEnablers.AbstractCrawler;
import com.bytepunditz.Desktop.appertureEnablers.IMAPUrisValidatingCrawlerHandler;

public class ImapParser extends AbstractCrawler {
	  private static final String SERVER_OPTION = "--server";

	    private static final String USERNAME_OPTION = "--username";

	    private static final String PASSWORD_OPTION = "--password";

	    private static final String FOLDER_OPTION = "--folder";
	    
	    private static final String SSL_OPTION = "--ssl";

	    private static final String SSL_NOCERT_OPTION = "--sslnocert";
	    
	    private static final String PORT_OPTION = "--port";
	    
	    private static final String TEST_URIS_OPTION = "--testuris";
	    
	    private static final String IGNORE_UID_VALIDITY_OPTION = "--ignoreUidValidity";
	    
	    private static final String USE_HEADERS_HASH_OPTION = "--useHeadersHash";

	    // /////////////// Settable properties /////////////////

	    private String serverName;

	    private String portString;
	    
	    private String username;

	    private String password;

	    private String folder;
	    
	    private boolean testingUris;

	    private boolean ignoreUidValidity;
	    
	    private boolean useHeadersHash;
	    
	    /**
	     * Flag that indicates whether a secure connection should be used,
	     * and if yes, if it should be certificates or not.
	     * 
	     * <p>
	     * Note that this setting is not settable on the command-line. Correct handling of a secure connection
	     * requires a rather complex setup, depending on the Java version used, whether or not it's a GUI
	     * application, etc. See ImapCrawler.sessionProperties and the SSLNOTES.TXT file delivered with the
	     * Javamail package for more information.
	     * 
	     * <p>
	     * The GUI-based crawler makes use of this property and also ensures that all other requirements for
	     * secure operation are fulfilled.
	     */
	    private ConnectionSecurity connectionSecurity = ConnectionSecurity.PLAIN;

	    private ImapCrawler crawler;

	    

	    public String getFolder() {
	        return folder;
	    }

	    public String getServerName() {
	        return serverName;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public String getUsername() {
	        return username;
	    }
	    
	    public boolean getUserHeadersHash() {
	        return useHeadersHash;
	    }
	    
	    public ConnectionSecurity getConnectionSecurity() {
	        return connectionSecurity;
	    }

	    public String getCurrentURL() {
	        return getHandler() != null ? getHandler().getCurrentURL() : null;
	    }

	    public ExitCode getExitCode() {
	        return getHandler() != null ? getHandler().getExitCode() : null;
	    }

	    public int getNrObjects() {
	        return getHandler() != null ? getHandler().getNrObjects() : -1 ;
	    }

	    public long getStartTime() {
	        return getHandler() != null ? getHandler().getStartTime() : 0L ;
	    }

	    public long getFinishTime() {
	        return getHandler() != null ? getHandler().getFinishTime() : 0L ;
	    }

	    public void setFolder(String folder) {
	        this.folder = folder;
	    }

	    public void setServerName(String serverName) {
	        this.serverName = serverName;
	    }
	    
	    public void setPortString(String portString) {
	        this.portString = portString;
	    }
	    
	    public void setPassword(String password) {
	        this.password = password;
	    }
	    
	    public void setConnectionSecurity(ConnectionSecurity connectionSecurity) {
	        this.connectionSecurity = connectionSecurity;
	    }

	    public void setUsername(String username) {
	        this.username = username;
	    }
	    
	    public void setTestingUris(boolean testuris) {
	        this.testingUris = testuris;
	    }
	    
	    private void setIgnoreUidValidity(boolean b) {
	        this.ignoreUidValidity = b;
	    }
	    
	    private void setUseHeadersHash(boolean b) {
	        this.useHeadersHash = b;
	    }

	    public void crawl() throws ModelException {
	        if (serverName == null) {
	            throw new IllegalArgumentException("serverName cannot be null");
	        }
	        if (folder == null) {
	            throw new IllegalArgumentException("folder cannot be null");
	        }
	        
	        // create a data source configuration
	        RDFContainerFactoryImpl factory = new RDFContainerFactoryImpl();
	        RDFContainer config = factory.newInstance("urn:test:exampleimapsource");

	        ImapDataSource dataSource = new ImapDataSource();
	        dataSource.setConfiguration(config);
	        
	        dataSource.setHostname(serverName);
	        dataSource.setBasepath(folder);
	        dataSource.setIgnoreUidValidity(ignoreUidValidity);
	        
	        if (portString != null) {
	            dataSource.setPort(new Integer(portString));
	        }
	        
	        if (username != null) {
	            dataSource.setUsername(username);
	        }

	        if (password != null) {
	            dataSource.setPassword(password);
	        }

	        dataSource.setConnectionSecurity(getConnectionSecurity());
	        
	        dataSource.setUseHeadersHash(this.useHeadersHash);

	        // set up an IMAP crawler
	        crawler = new ImapCrawler();
	        crawler.setDataSource(dataSource);
	        crawler.setCrawlerHandler(getHandler());
	        crawler.setAccessData(getAccessData());
	        crawler.crawl();
	    }

	    public void stop() {
	        ImapCrawler crawler = this.crawler;
	        if (crawler != null) {
	            crawler.stop();
	        }
	    }

	    public boolean isStopRequested() {
	        ImapCrawler crawler = this.crawler;
	        return crawler == null ? false : crawler.isStopRequested();
	    }

	    /*
	    public static void main(String[] args) throws Exception {
	        // create a new ExampleImapCrawler instance
	        ImapParser crawler = new ImapParser();

	        List<String> remainingOptions = crawler.processCommonOptions(args);
	        
	        // parse the command line options
	        Iterator<String> iterator = remainingOptions.iterator();
	        while (iterator.hasNext()) {
	            // fetch the option name
	            String option = iterator.next();

	            // first the options that don't need any values
	            if (TEST_URIS_OPTION.equals(option)) {
	                crawler.setHandler(
	                    new IMAPUrisValidatingCrawlerHandler(
	                        crawler.isIdentifyingMimeType(),
	                        crawler.isExtractingContents(),
	                        crawler.isVerbose(),
	                        crawler.getOutputFile()));
	                continue;
	            } else if (SSL_NOCERT_OPTION.equals(option)) {
	                crawler.setConnectionSecurity(ConnectionSecurity.SSL_NO_CERT);
	                continue;
	            } else if (SSL_OPTION.equals(option)) {
	                crawler.setConnectionSecurity(ConnectionSecurity.SSL);
	                continue;
	            } else if (IGNORE_UID_VALIDITY_OPTION.equals(option)) {
	                crawler.setIgnoreUidValidity(true);
	                continue;
	            } else if (USE_HEADERS_HASH_OPTION.equals(option)) {
	                crawler.setUseHeadersHash(true);
	                continue;
	            }
	            
	            // and then the options that take values
	            
	            // fetch the option value
	            if (!iterator.hasNext()) {
	                System.err.println("missing value for option " + option);
	                crawler.exitWithUsageMessage();
	            }
	            
	            String value = iterator.next();

	            if (SERVER_OPTION.equals(option)) {
	                crawler.setServerName(value);
	            }
	            else if (USERNAME_OPTION.equals(option)) {
	                // crawler.setUsername(HttpClientUtil.formUrlEncode(value));
	                crawler.setUsername(value);
	            } else if (PORT_OPTION.equals(option)) {
	                crawler.setPortString(value);
	            }
	            else if (PASSWORD_OPTION.equals(option)) {
	                crawler.setPassword(value);
	            }
	            else if (FOLDER_OPTION.equals(option)) {
	                crawler.setFolder(value);
	            } else
	                throw new Exception("Unknown option: "+option);
	        }

	        // check whether the crawler has enough information
	        if (crawler.getServerName() == null) {
	            System.err.println("server name missing");
	            crawler.exitWithUsageMessage();
	        }
	        if (crawler.getFolder() == null) {
	            System.err.println("folder missing");
	            crawler.exitWithUsageMessage();
	        }

	        // start crawling and exit afterwards
	        crawler.crawl();
	    }
	    */
	    
	    public void crawlAndParse(String[] args) throws Exception
	    {
	    	ImapParser crawler = new ImapParser();

	        List<String> remainingOptions = crawler.processCommonOptions(args);
	        
	        // parse the command line options
	        Iterator<String> iterator = remainingOptions.iterator();
	        while (iterator.hasNext()) {
	            // fetch the option name
	            String option = iterator.next();

	            // first the options that don't need any values
	            if (TEST_URIS_OPTION.equals(option)) {
	                crawler.setHandler(
	                    new IMAPUrisValidatingCrawlerHandler(
	                        crawler.isIdentifyingMimeType(),
	                        crawler.isExtractingContents(),
	                        crawler.isVerbose(),
	                        crawler.getOutputFile()));
	                continue;
	            } else if (SSL_NOCERT_OPTION.equals(option)) {
	                crawler.setConnectionSecurity(ConnectionSecurity.SSL_NO_CERT);
	                continue;
	            } else if (SSL_OPTION.equals(option)) {
	                crawler.setConnectionSecurity(ConnectionSecurity.SSL);
	                continue;
	            } else if (IGNORE_UID_VALIDITY_OPTION.equals(option)) {
	                crawler.setIgnoreUidValidity(true);
	                continue;
	            } else if (USE_HEADERS_HASH_OPTION.equals(option)) {
	                crawler.setUseHeadersHash(true);
	                continue;
	            }
	            
	            // and then the options that take values
	            
	            // fetch the option value
	            if (!iterator.hasNext()) {
	                System.err.println("missing value for option " + option);
	                crawler.exitWithUsageMessage();
	            }
	            
	            String value = iterator.next();

	            if (SERVER_OPTION.equals(option)) {
	                crawler.setServerName(value);
	            }
	            else if (USERNAME_OPTION.equals(option)) {
	                // crawler.setUsername(HttpClientUtil.formUrlEncode(value));
	                crawler.setUsername(value);
	            } else if (PORT_OPTION.equals(option)) {
	                crawler.setPortString(value);
	            }
	            else if (PASSWORD_OPTION.equals(option)) {
	                crawler.setPassword(value);
	            }
	            else if (FOLDER_OPTION.equals(option)) {
	                crawler.setFolder(value);
	            } else
	                throw new Exception("Unknown option: "+option);
	        }

	        // check whether the crawler has enough information
	        if (crawler.getServerName() == null) {
	            System.err.println("server name missing");
	            crawler.exitWithUsageMessage();
	        }
	        if (crawler.getFolder() == null) {
	            System.err.println("folder missing");
	            crawler.exitWithUsageMessage();
	        }

	        // start crawling and exit afterwards
	        crawler.crawl();
	    }

	    @Override
	    protected String getSpecificExplanationPart() {
	        StringBuilder builder = new StringBuilder();
	        builder.append(getAlignedOption(SERVER_OPTION) + "specifies the hostname of the server\n");
	        builder.append(getAlignedOption(USERNAME_OPTION) + "the username\n");
	        builder.append(getAlignedOption(PORT_OPTION) + "the port\n");
	        builder.append(getAlignedOption(SSL_OPTION) + "if this option is present SSL will be used\n");
	        builder.append(getAlignedOption(SSL_NOCERT_OPTION) + "if this option is present SSL will be used, but certificates\n" +
	                       getAlignedOption("") + " will not be checked\n");
	        builder.append(getAlignedOption(PASSWORD_OPTION) + "the password\n");
	        builder.append(getAlignedOption(FOLDER_OPTION) + "the folder on the server where the crawling should start\n");
	        builder.append(getAlignedOption(TEST_URIS_OPTION) + "check if the uris generated by the crawler are compliant\n " +
	                       getAlignedOption("") + "with RFC 2192. this setting overrides " + AbstractCrawler.VALIDATE_OPTION + " and\n " + 
	                       getAlignedOption("") + AbstractCrawler.PERFORMANCE_OPTION + "\n");
	        builder.append(getAlignedOption(IGNORE_UID_VALIDITY_OPTION) + "unused, ignored\n");
	        builder.append(getAlignedOption(USE_HEADERS_HASH_OPTION) + "used only on IMAP folders that don't persist UIDs\n");
	        builder.append(getAlignedOption("") + "when set the crawler will use only the hash of selected headers\n");
	        builder.append(getAlignedOption("") + "when computing the URI. This makes incremental crawling faster\n");
	        builder.append(getAlignedOption("") + "but can cause the crawler to omit certain near-duplicate messages\n");
	        builder.append(getAlignedOption("") + "those with the same headers values but different content, this\n");
	        builder.append(getAlignedOption("") + "happens very rarely and is usually not a problem, but you have\n");
	        builder.append(getAlignedOption("") + "been warned\n");
	        return builder.toString();
	    }

	    @Override
	    protected String getSpecificSyntaxPart() {
	        StringBuilder builder = new StringBuilder();
	        append(SERVER_OPTION, "server", false, builder);
	        append(PORT_OPTION, "port", true, builder);
	        append(SSL_OPTION, null, true, builder);
	        append(USERNAME_OPTION, "username", true, builder);
	        append(PASSWORD_OPTION, "password", true, builder);
	        append(TEST_URIS_OPTION, null, true, builder);
	        append(IGNORE_UID_VALIDITY_OPTION, null, true, builder);
	        append(FOLDER_OPTION, "folder", false, builder);
	        return builder.toString();
	    }
	    
	    private void append(String option, String var, boolean optional, StringBuilder builder) {
	        builder.append(' ');
	        if (optional) {
	            builder.append('[');
	        }
	        builder.append(option);
	        if (var != null) {
	            builder.append(" <");
	            builder.append(var);
	            builder.append('>');
	        }
	        if (optional) {
	            builder.append(']');
	        }
	    }

}
