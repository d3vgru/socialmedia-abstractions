package eu.socialsensor.framework.retrievers.twitter;

import java.util.List;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;

public class TwitterRetriever implements Retriever{
	
	
	public List<Item> retrieveUserFeeds(SourceFeed feed){
		return null;
	}
	
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed) {
		return null;
	}
	
	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		return null;
	}
	
	
	public List<Item> retrieve(Feed feed){
		
		switch(feed.getFeedtype()){
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				return retrieveUserFeeds(userFeed);
				
			
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
				
			case LOCATION:
				LocationFeed locFeed = (LocationFeed) feed;
				
				return retrieveLocationFeeds(locFeed);
			
		}
		return null;
	}
	
	
}
