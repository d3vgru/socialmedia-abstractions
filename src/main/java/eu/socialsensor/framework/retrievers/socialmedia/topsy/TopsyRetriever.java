package eu.socialsensor.framework.retrievers.socialmedia.topsy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.maruti.otterapi.Otter4JavaException;
import com.maruti.otterapi.TopsyConfig;
import com.maruti.otterapi.search.Post;
import com.maruti.otterapi.search.Search;
import com.maruti.otterapi.search.SearchCriteria;
import com.maruti.otterapi.search.SearchResponse;

import eu.socialsensor.framework.abstractions.socialmedia.topsy.TopsyItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.topsy.TopsyStream;

public class TopsyRetriever implements SocialMediaRetriever{
	
	private String apiKey = null;
	
	private TopsyStream topsyStream;
	
	private TopsyConfig topsyConfig;
	
	public TopsyRetriever(String apiKey,TopsyStream topsyStream){
		this.apiKey = apiKey;
		this.topsyStream = topsyStream;
		
		topsyConfig = new TopsyConfig();
		topsyConfig.setApiKey(apiKey);
		topsyConfig.setSetProxy(false);
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		return null;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed){
		int totalRetrievedItems = 0;
		Date dateToRetrieve = feed.getDateToRetrieve();
		
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		
		Search searchTopsy = new Search();
		searchTopsy.setTopsyConfig(topsyConfig);
		SearchResponse results = null;
		try {
			SearchCriteria criteria = new SearchCriteria();
			criteria.setQuery(feed.getKeyword().getName());
			criteria.setType("image");
			results = searchTopsy.search(criteria);
			List<Post> posts = results.getResult().getList();
			for(Post post : posts){
				String since = post.getFirstpost_date();
			
				if(since != null){
					
					Long publicationDate = Long.parseLong(since) * 1000;
				
					if(publicationDate > dateToRetrieve.getTime()){
						TopsyItem topsyItem = new TopsyItem(post);
						topsyStream.store(topsyItem);
						totalRetrievedItems++;
					}
					
				}
				
			}
				
			
		} catch (Otter4JavaException e) {
			e.printStackTrace();
		}

		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed){
		return null;
	}
	
	@Override
	public Integer retrieve (Feed feed) {
		
		switch(feed.getFeedtype()) {
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				return retrieveUserFeeds(userFeed);
				
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
				
			case LOCATION:
				LocationFeed locationFeed = (LocationFeed) feed;
				
				return retrieveLocationFeeds(locationFeed);
				
			
		}
	
		return 0;
	}
	
	@Override
	public void stop(){

	}

	@Override
	public MediaItem getMediaItem(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		// TODO Auto-generated method stub
		return null;
	}

}
