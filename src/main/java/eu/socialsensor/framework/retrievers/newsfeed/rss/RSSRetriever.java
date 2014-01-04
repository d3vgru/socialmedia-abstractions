package eu.socialsensor.framework.retrievers.newsfeed.rss;

import java.io.IOException;
import java.net.URL;
import java.util.List;


import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eu.socialsensor.framework.abstractions.newsfeed.rss.RSSItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.feeds.URLFeed;
import eu.socialsensor.framework.retrievers.Retriever;
import eu.socialsensor.framework.streams.newsfeed.rss.RSSStream;


public class RSSRetriever implements Retriever{
	
	RSSStream rssStream;
	
	public RSSRetriever(RSSStream rssStream){
		
		this.rssStream = rssStream;
	}
	
	@Override
	public Integer retrieve(Feed feed){
		URLFeed ufeed = (URLFeed) feed;
		Integer totalRetrievedItems = 0;
		try {
			
			URL url  = new URL(ufeed.getURL());
			if(ufeed.getURL().equals(""))
				return totalRetrievedItems;
			
		    XmlReader reader = new XmlReader(url);

	        SyndFeed rssData = new SyndFeedInput().build(reader);
			
			@SuppressWarnings("unchecked")
			List<SyndEntry> rssEntries = rssData.getEntries();
			
			for (SyndEntry rss:rssEntries){
				RSSItem rssItem = new RSSItem(rss);
				
				rssStream.store(rssItem);
				
				totalRetrievedItems++;
			}
		 
	      
		} catch (IOException e) {
			// TODO Auto-generated catch block
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			
		} 
		return totalRetrievedItems;
	}

	
	@Override
	public void stop(){
	
	}
	

	
}
