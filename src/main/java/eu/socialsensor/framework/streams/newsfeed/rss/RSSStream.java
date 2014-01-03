package eu.socialsensor.framework.streams.newsfeed.rss;

import java.text.SimpleDateFormat;
import java.util.Date;

import eu.socialsensor.framework.common.domain.NewsFeedSource;
import eu.socialsensor.framework.retrievers.newsfeed.rss.RSSRetriever;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;


public class RSSStream extends Stream{
	
	public static NewsFeedSource SOURCE = NewsFeedSource.RSS;

	public void open(StreamConfiguration config){

		retriever = new RSSRetriever(this);
		
		
	}
	
	
}
