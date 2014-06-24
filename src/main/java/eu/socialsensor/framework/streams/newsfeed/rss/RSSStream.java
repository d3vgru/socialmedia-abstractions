package eu.socialsensor.framework.streams.newsfeed.rss;

import eu.socialsensor.framework.common.domain.NewsFeedSource;
import eu.socialsensor.framework.retrievers.newsfeed.rss.RSSRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;

/**
 * Class responsible for setting up the connection for retrieving RSS feeds.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class RSSStream extends Stream{
	
	public static NewsFeedSource SOURCE = NewsFeedSource.RSS;
	

	@Override
	public void open(StreamConfiguration config){
		
	
		retriever = new RSSRetriever(this);
		
		
	}

}
