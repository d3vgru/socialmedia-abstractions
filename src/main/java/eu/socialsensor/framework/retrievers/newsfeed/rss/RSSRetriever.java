package eu.socialsensor.framework.retrievers.newsfeed.rss;

import java.util.List;

import org.rometools.fetcher.FetcherEvent;
import org.rometools.fetcher.FetcherListener;
import org.rometools.fetcher.impl.FeedFetcherCache;
import org.rometools.fetcher.impl.HashMapFeedInfoCache;
import org.rometools.fetcher.impl.HttpURLFeedFetcher;

import com.sun.syndication.io.XmlReader;

import eu.socialsensor.framework.common.domain.Document;
import eu.socialsensor.framework.retrievers.newsfeed.NewsFeedRetriever;


public class RSSRetriever implements NewsFeedRetriever{
	
	private FeedFetcherCache rssFeedsCache;
	private FetcherEventListenerImpl listener;
	private HttpURLFeedFetcher fetcher;
	private XmlReader reader = null;
	
	
	public RSSRetriever(){
		
		rssFeedsCache = HashMapFeedInfoCache.getInstance();
		listener = new FetcherEventListenerImpl();
		fetcher = new HttpURLFeedFetcher();
		fetcher.setFeedInfoCache(rssFeedsCache);
		fetcher.addFetcherEventListener(listener);
		
	}
	
	@Override
	public List<Document> retrieve(String url){
		
		return null;
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
