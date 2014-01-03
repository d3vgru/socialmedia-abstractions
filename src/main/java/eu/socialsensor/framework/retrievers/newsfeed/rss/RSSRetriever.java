package eu.socialsensor.framework.retrievers.newsfeed.rss;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.rometools.fetcher.FetcherEvent;
import org.rometools.fetcher.FetcherListener;
import org.rometools.fetcher.impl.FeedFetcherCache;
import org.rometools.fetcher.impl.HashMapFeedInfoCache;
import org.rometools.fetcher.impl.HttpURLFeedFetcher;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

import eu.socialsensor.framework.abstractions.newsfeed.rss.RSSItem;
import eu.socialsensor.framework.retrievers.newsfeed.NewsFeedRetriever;
import eu.socialsensor.framework.streams.newsfeed.rss.RSSStream;


public class RSSRetriever implements NewsFeedRetriever{
	
	private FeedFetcherCache rssFeedsCache;
	private FetcherEventListenerImpl listener;
	private HttpURLFeedFetcher fetcher;
	private XmlReader reader = null;
	
	private Date dateToRetrieve = null;
	
	private RSSStream rssStream;
	
	public RSSRetriever(Date date,RSSStream rssStream){
		
		
		dateToRetrieve = date;
		
		this.rssStream = rssStream;
		
		rssFeedsCache = HashMapFeedInfoCache.getInstance();
		listener = new FetcherEventListenerImpl();
		fetcher = new HttpURLFeedFetcher();
		fetcher.setFeedInfoCache(rssFeedsCache);
		fetcher.addFetcherEventListener(listener);
		
		
	}
	
	@Override
	public void retrieve(String url){
		
		URL feedURL = null;
		try {
			feedURL = new URL(url);
		} catch (MalformedURLException e1) {
			
		}
		
		try {
			reader = new XmlReader(feedURL);
			SyndFeed rssData = new SyndFeedInput().build(reader);
			
			@SuppressWarnings("unchecked")
			List<SyndEntry> rssEntries = rssData.getEntries();
			
			for (SyndEntry rss:rssEntries){
				if(rss.getPublishedDate().after(dateToRetrieve)){
					RSSItem rssItem = new RSSItem(rss);
					
				}
				
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
		
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			
		}
	}

	
	@Override
	public void stop(){
		rssFeedsCache.clear();
		rssFeedsCache = null;
		listener = null;
		fetcher = null;
	}
	
	/**
	 * @brief Method for rometools fetcher for event handling in rss feed retrieval
	 * @author ailiakop
	 *
	 */
	static class FetcherEventListenerImpl implements FetcherListener {
		/**
		 * @see com.sun.syndication.fetcher.FetcherListener#fetcherEvent(com.sun.syndication.fetcher.FetcherEvent)
		 */
		public void fetcherEvent(FetcherEvent event) {
			String eventType = event.getEventType();
			if (FetcherEvent.EVENT_TYPE_FEED_POLLED.equals(eventType)) {
				//System.out.println("\tEVENT: Feed Polled.");
			} else if (FetcherEvent.EVENT_TYPE_FEED_RETRIEVED.equals(eventType)) {
				//System.out.println("\tEVENT: Feed Retrieved.");
			} else if (FetcherEvent.EVENT_TYPE_FEED_UNCHANGED.equals(eventType)) {
				//System.out.println("\tEVENT: Feed Unchanged.");
			}
		}
	}
}
