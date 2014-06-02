package eu.socialsensor.framework.retrievers.socialmedia.twitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import eu.socialsensor.framework.abstractions.socialmedia.twitter.TwitterItem;
import eu.socialsensor.framework.abstractions.socialmedia.twitter.TwitterStreamUser;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.ListFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.twitter.TwitterStream;

public class TwitterRetriever implements SocialMediaRetriever {
	
	private Logger  logger = Logger.getLogger(TwitterRetriever.class);
	
	private Twitter twitter = null;
	private TwitterFactory tf = null;
	
	private TwitterStream twStream;
	
	private int maxResults = 100;
	private int maxRequests = 1;
	
	private long maxRunningTime;
	private long currRunningTime = 0l;
	
	public TwitterRetriever(Configuration conf,Integer maxRequests,Integer maxResults,Long maxRunningTime,TwitterStream twStream){
		
		this.tf = new TwitterFactory(conf);
		twitter = tf.getInstance();
		
		this.twStream = twStream;
		if(maxResults != null)
			this.maxResults = maxResults;
		if(maxRequests != null)
			this.maxRequests = maxRequests;
		
		this.maxRunningTime = maxRunningTime;
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		return 0;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed) {
		int count = 50 , numberOfRequests = 0;
		String resultType = "recent";
	
		Integer totalRetrievedItems = 0;
		
		long currRunningTime = System.currentTimeMillis();
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Twitter : No keywords feed");
			return totalRetrievedItems;
		}
		
		
		String tags = "";
		
		if(keyword != null){
		
			tags += keyword.getName().toLowerCase();
			tags = tags.trim();
		}
		else if(keywords != null){
			for(Keyword key : keywords){
				String [] words = key.getName().split(" ");
				for(String word : words)
					if(!tags.contains(word) && word.length()>1)
						tags += word.toLowerCase()+" ";
			}
		}
		
		if(tags.equals(""))
			return totalRetrievedItems;
		
		//Set the query
		Query query = new Query(tags);
	
		query.count(count);
		query.setResultType(resultType);//do not set last item date-causes problems!
		
		
		try {
			QueryResult response = twitter.search(query);
			
			while(response != null){
				numberOfRequests++;
				
				List<Status> statuses = response.getTweets();
				
				for(Status status : statuses) {
					if(status != null){
						TwitterItem twitterItem = new TwitterItem(status);
						twStream.store(twitterItem);
						totalRetrievedItems++;
					}
					
					
				}
				if(!response.hasNext() || totalRetrievedItems > maxResults || numberOfRequests > maxRequests || (System.currentTimeMillis()-currRunningTime)>maxRunningTime)
					break;
				
				query = response.nextQuery();
				if(query == null)
					break;
				response = twitter.search(response.nextQuery());
			}
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}	
	
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed) {
		
		Integer totalRetrievedItems = 0, numberOfRequests = 0;
		
		Location location = feed.getLocation();
		if(location == null)
			return totalRetrievedItems;
		
		long currRunningTime = System.currentTimeMillis();
		
		//Set the query
		Query query = new Query();
		Double radius = location.getRadius();
		if(radius==null)
			radius = 1.5; // default radius 1.5 Km 
		
		GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
		query.setGeoCode(geoLocation, radius, Query.KILOMETERS);
		query.count(100);
				
		while(true) {
			try {
				numberOfRequests++;
				QueryResult response = twitter.search(query);
				
				
				List<Status> statuses = response.getTweets();
				for(Status status : statuses) {
					if(status != null) {
						TwitterItem twitterItem = new TwitterItem(status);
						twStream.store(twitterItem);
						totalRetrievedItems++;
					}
				}
				
				if(!response.hasNext() || totalRetrievedItems > maxResults || numberOfRequests > maxRequests || (System.currentTimeMillis() - currRunningTime)>maxRunningTime)
					break;
				
				query = response.nextQuery();
				if(query == null)
					break;
			} catch (TwitterException e) {
				e.printStackTrace();
				logger.error(e);
				break;
			}
		}
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveListsFeeds(ListFeed feed) {
		
		Integer totalRetrievedItems = 0, numberOfRequests = 0;
		long currRunningTime = System.currentTimeMillis();

			
		String ownerScreenName = feed.getListOwner();
		String slug = feed.getListSlug();
			
			
		int page = 1;
		Paging paging = new Paging(page, 100);
		while(true) {
			try {
				numberOfRequests++;
				ResponseList<Status> response = twitter.getUserListStatuses(ownerScreenName, slug, paging);
				for(Status status : response) {
					if(status != null) {
						TwitterItem twitterItem = new TwitterItem(status);
						twStream.store(twitterItem);
						totalRetrievedItems++;
					}
				}
					
				if(totalRetrievedItems > maxResults || numberOfRequests > maxRequests 
						|| (System.currentTimeMillis() - currRunningTime)>maxRunningTime) {
					break;
				}
					
				paging.setPage(++page);
			} catch (TwitterException e) {
				e.printStackTrace();
				logger.error(e);	
			}
		}
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieve(Feed feed) {
		
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
		return 0;
	}
	
	@Override
	public void stop(){
		if(twitter!=null)
			twitter.shutdown();
		
		twitter=null;
	}
	
	private long[] getUserIds(String[] followsUsernames) {
		
		List<Long> ids = new ArrayList<Long>();
		List<String> usernames = new ArrayList<String>(followsUsernames.length);
		for(String username : followsUsernames) {
			usernames.add(username);
		}
		
		int size = usernames.size();
		int start = 0;
		int end = Math.min(start+100, size);
		
		while(start < size) {
			List<String> sublist = usernames.subList(start, end);
			String[] _usernames = sublist.toArray(new String[sublist.size()]);
			try {
				System.out.println("Request for " + _usernames.length + " users ");
				ResponseList<User> users = twitter.lookupUsers(_usernames);
				System.out.println(users.size() + " users ");
				for(User user : users) {
					long id = user.getId();
					ids.add(id);
				}
			} catch (TwitterException e) {
				logger.error("Error while getting user ids from twitter...");
				logger.error("Exception",e
						);
				break;
			}
			
			start = end + 1;
			end = Math.min(start+100, size);
		}
		
		long[] retIds = new long[ids.size()];
		for(int i=0;i<ids.size();i++)
			retIds[i] = ids.get(i);
		
		return retIds;
	}

	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		try {
			long userId = Long.parseLong(uid);
			User user = twitter.showUser(userId);
			
			StreamUser streamUser = new TwitterStreamUser(user);
			return streamUser;
		}
		catch(Exception e) {
			return null;
		}
	}
	
}
