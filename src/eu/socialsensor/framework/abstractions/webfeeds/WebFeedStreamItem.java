///////////////////////////////////////////////////////////////////////////
// 
// IBM Confidential
// OCO Source Materials
// (c) Copyright IBM Corp. 2012
// 
// The source code for this program is not published or otherwise divested of 
// its trade secrets, irrespective of what has been deposited with
// the U.S. Copyright Office.
// 
///////////////////////////////////////////////////////////////////////////

package eu.socialsensor.framework.abstractions.webfeeds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;

import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.WebPage;
import eu.socialsensor.framework.streams.webfeeds.WebFeedsStream;
/**
 * Class that holds the information regarding the web feed item
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class WebFeedStreamItem extends Item {
    
	@SuppressWarnings("all")
	public WebFeedStreamItem(SyndEntry entry) {
		super(WebFeedsStream.SOURCE.toString(), Operation.NEW_UPDATE);
		
		
		
		if (entry == null) return;
		
		String link = entry.getLink();
		
		Date pubDate = entry.getPublishedDate();
		publicationTime = pubDate.getTime();
		
		id = WebFeedsStream.SOURCE + "::" + Integer.toString(link.hashCode() & 0x7FFFFFFF)
				+ publicationTime;
		
		author = entry.getAuthor();
		List cats = entry.getCategories();
		if (cats != null){
			categories = new String[cats.size()];
			Iterator it = cats.iterator();
			int i = 0;
			while (it.hasNext()){
				SyndCategory category = (SyndCategory)it.next();
				categories[i++] = category.getName();
			}
		}
		
		SyndContent desc = entry.getDescription();
		if (desc != null){
			description = desc.getValue();
		}
		
		title = entry.getTitle();
		
		List<String> entryLinks = entry.getLinks();
		List<URL> linksList = new ArrayList<URL>();
		webPages = new ArrayList<WebPage>();
		try {
			linksList.add(new URL(link));
			
			WebPage webPage = new WebPage(link, id);
			webPage.setStreamId(streamId);
			webPages.add(webPage);
		} catch (MalformedURLException e1) {
		}
		for(String entryLink : entryLinks) {
			try {
				linksList.add(new URL(entryLink));
				
				WebPage webPage = new WebPage(entryLink, id);
				webPage.setStreamId(streamId);
				webPages.add(webPage);
			} catch (MalformedURLException e) {
				continue;
			}
		}
		links = linksList.toArray(new URL[linksList.size()]);
	}

}
