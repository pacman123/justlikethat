/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.parse.tika;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.HTMLMetaTags;
import org.apache.nutch.parse.HtmlParseFilters;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.OutlinkExtractor;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.protocol.Content;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DocumentFragment;

import com.bytepunditz.Desktop.RDFExtractor.RDFExtractor;
import com.bytepunditz.Desktop.nutchPluginsEnablers.DOMBuilder;
import com.bytepunditz.Desktop.nutchPluginsEnablers.DOMContentUtils;
import com.bytepunditz.Desktop.nutchPluginsEnablers.HTMLMetaProcessor;


/**
 * Wrapper for Tika parsers. Mimics the HTMLParser but using the XHTML
 * representation returned by Tika as SAX events
 ***/

public class TikaParser implements org.apache.nutch.parse.Parser {

	public static final Logger LOG = LoggerFactory.getLogger(TikaParser.class);

	private Configuration conf;
	private TikaConfig tikaConfig = null;
	private DOMContentUtils utils;
	private HtmlParseFilters htmlParseFilters;
	private String cachingPolicy;

	public ParseResult getParse(Content content) {
		
		String mimeType = content.getContentType();
		LOG.error("Nishant MIMEType = " + mimeType + "fileName = " + content.getUrl());
		System.out.println("TikaParser :: Nishant MIMEType = " + mimeType);
		
		RDFExtractor extract = new RDFExtractor();
		extract.generateRDFQuad( content);
		return new ParseStatus(ParseStatus.NOTPARSED, "skipping parser as we dont need to parse the file")
		.getEmptyParseResult(content.getUrl(), getConf());
		
		/* Nishant changes
		URL base;
		try {
			base = new URL(content.getBaseUrl());
		} catch (MalformedURLException e) {
			LOG.error("Nishant error not processing further!!");
			return new ParseStatus(e).getEmptyParseResult(content.getUrl(),
					getConf());
		}
		
		
	
		// get the right parser using the mime type as a clue
		Parser parser = tikaConfig.getParser(MediaType.parse(mimeType));
		byte[] raw = content.getContent();

		if (parser == null) {
			String message = "Can't retrieve Tika parser for mime-type "
					+ mimeType;
			LOG.error(message);
			return new ParseStatus(ParseStatus.FAILED, message)
			.getEmptyParseResult(content.getUrl(), getConf());
		}

		LOG.debug("Using Tika parser " + parser.getClass().getName()
				+ " for mime-type " + mimeType);
		LOG.error("Nishant Using Tika parser " + parser.getClass().getName()
				+ " for mime-type " + mimeType);
		
		
		Metadata tikamd = new Metadata();

		HTMLDocumentImpl doc = new HTMLDocumentImpl();
		doc.setErrorChecking(false);
		DocumentFragment root = doc.createDocumentFragment();
		DOMBuilder domhandler = new DOMBuilder(doc, root);
		ParseContext context = new ParseContext();
		try {
			parser.parse(new ByteArrayInputStream(raw), domhandler, tikamd,context);
		} catch (Exception e) {
			LOG.error("Error parsing "+content.getUrl(),e);
			return new ParseStatus(ParseStatus.FAILED, e.getMessage())
			.getEmptyParseResult(content.getUrl(), getConf());
		}


		System.out.println("Nishant metadata extracted");
		System.out.println(tikamd.toString());
		LOG.error("Metadata extracted as " + tikamd.toString());



		HTMLMetaTags metaTags = new HTMLMetaTags();
		String text = "";
		String title = "";
		Outlink[] outlinks = new Outlink[0];
		org.apache.nutch.metadata.Metadata nutchMetadata = new org.apache.nutch.metadata.Metadata();

		// we have converted the sax events generated by Tika into a DOM object
		// so we can now use the usual HTML resources from Nutch
		// get meta directives
		HTMLMetaProcessor.getMetaTags(metaTags, root, base);
		if (LOG.isTraceEnabled()) {
			LOG.trace("Meta tags for " + base + ": " + metaTags.toString());
		}

		// check meta directives
		if (!metaTags.getNoIndex()) { // okay to index
			StringBuffer sb = new StringBuffer();
			if (LOG.isTraceEnabled()) {
				LOG.trace("Getting text...");
			}
			utils.getText(sb, root); // extract text
			text = sb.toString();
			sb.setLength(0);
			if (LOG.isTraceEnabled()) {
				LOG.trace("Getting title...");
			}
			utils.getTitle(sb, root); // extract title
			title = sb.toString().trim();
		}

		if (!metaTags.getNoFollow()) { // okay to follow links
			ArrayList<Outlink> l = new ArrayList<Outlink>(); // extract outlinks
			URL baseTag = utils.getBase(root);
			if (LOG.isTraceEnabled()) {
				LOG.trace("Getting links...");
			}
			utils.getOutlinks(baseTag != null ? baseTag : base, l, root);
			outlinks = l.toArray(new Outlink[l.size()]);
			if (LOG.isTraceEnabled()) {
				LOG.trace("found " + outlinks.length + " outlinks in "
						+ content.getUrl());
			}
		}

		// populate Nutch metadata with Tika metadata
		String[] TikaMDNames = tikamd.names();
		for (String tikaMDName : TikaMDNames) {
			if (tikaMDName.equalsIgnoreCase(Metadata.TITLE))
				continue;
			// TODO what if multivalued?
			nutchMetadata.add(tikaMDName, tikamd.get(tikaMDName));
		}

		// no outlinks? try OutlinkExtractor e.g works for mime types where no
		// explicit markup for anchors

		if (outlinks.length == 0) {
			outlinks = OutlinkExtractor.getOutlinks(text, getConf());
		}

		ParseStatus status = new ParseStatus(ParseStatus.SUCCESS);
		if (metaTags.getRefresh()) {
			status.setMinorCode(ParseStatus.SUCCESS_REDIRECT);
			status.setArgs(new String[] { metaTags.getRefreshHref().toString(),
					Integer.toString(metaTags.getRefreshTime()) });
		}
		ParseData parseData = new ParseData(status, title, outlinks, content
				.getMetadata(), nutchMetadata);
		ParseResult parseResult = ParseResult.createParseResult(content
				.getUrl(), new ParseImpl(text, parseData));

		// run filters on parse
		ParseResult filteredParse = this.htmlParseFilters.filter(content,
				parseResult, metaTags, root);
		if (metaTags.getNoCache()) { // not okay to cache
			for (Map.Entry<org.apache.hadoop.io.Text, Parse> entry : filteredParse)
				entry.getValue().getData().getParseMeta().set(
						Nutch.CACHING_FORBIDDEN_KEY, cachingPolicy);
		}
		return filteredParse;
		*/
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		this.tikaConfig = null;

		// do we want a custom Tika configuration file
		// deprecated since Tika 0.7 which is based on 
		// a service provider based configuration
		String customConfFile = conf.get("tika.config.file");
		if (customConfFile != null) {
			try {
				// see if a Tika config file can be found in the job file
				URL customTikaConfig = conf.getResource(customConfFile);
				if (customTikaConfig != null)
					tikaConfig = new TikaConfig(customTikaConfig);
			} catch (Exception e1) {
				String message = "Problem loading custom Tika configuration from "
						+ customConfFile;
				LOG.error(message, e1);
			}
		} else {
			try {
				tikaConfig = new TikaConfig(this.getClass().getClassLoader());
			} catch (Exception e2) {
				String message = "Problem loading default Tika configuration";
				LOG.error(message, e2);
			}
		}

		this.htmlParseFilters = new HtmlParseFilters(getConf());
		this.utils = new DOMContentUtils(conf);
		this.cachingPolicy = getConf().get("parser.caching.forbidden.policy",
				Nutch.CACHING_FORBIDDEN_CONTENT);

	}

	public Configuration getConf() {
		return this.conf;
	}

}
