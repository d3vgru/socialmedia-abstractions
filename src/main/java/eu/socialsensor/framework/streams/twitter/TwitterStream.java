
package eu.socialsensor.framework.streams.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import eu.socialsensor.framework.abstractions.twitter.TwitterItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.Item.Operation;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.SocialNetworkSource;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.streams.Stream;
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.StreamException;

/**
 * The stream that handles the configuration of the twitter wrapper
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public class TwitterStream extends Stream {
	
	public static SocialNetworkSource SOURCE = SocialNetworkSource.Twitter;
	
	private Logger  logger = Logger.getLogger(TwitterStream.class);
	
	public static long FILTER_EDIT_WAIT_TIME = 10000;
	
	public enum AccessLevel {
		PUBLIC(400, 5000, 25);
		
		int filterMaxKeywords;
		int filterMaxFollows;
		int filterMaxLocations;
		
		private AccessLevel(int filterMaxKeywords,
						   int filterMaxFollows,
						   int filterMaxLocations) {
			this.filterMaxKeywords = filterMaxKeywords;
			this.filterMaxFollows = filterMaxFollows;
			this.filterMaxLocations = filterMaxLocations;
		}

		public int getFilterMaxKeywords() {
			return filterMaxKeywords;
		}

		public int getFilterMaxFollows() {
			return filterMaxFollows;
		}

		public int getFilterMaxLocations() {
			return filterMaxLocations;
		}

	}
	
	private StatusListener listener = null;
	private twitter4j.TwitterStream twitterStream  = null;
	private Twitter twitter = null;
	
	private long lastFilterInitTime = System.currentTimeMillis();
	private AccessLevel accessLevel = AccessLevel.PUBLIC;
	
	@Override
	public synchronized void open(StreamConfiguration config) throws StreamException {
		if (twitterStream != null) {
			logger.error("#Twitter : Stream is already opened");
			throw new StreamException("Stream is already opened", null);
		}
		
		logger.info("#Twitter : Open stream");
		
		if (config == null) {
			logger.error("#Twitter : Config file is null.");
			return;
		}
		
		String oAuthConsumerKey 		= 	config.getParameter(KEY);
		String oAuthConsumerSecret 		= 	config.getParameter(SECRET);
		String oAuthAccessToken 		= 	config.getParameter(ACCESS_TOKEN);
		String oAuthAccessTokenSecret 	= 	config.getParameter(ACCESS_TOKEN_SECRET);
		
		if (oAuthConsumerKey == null || oAuthConsumerSecret == null ||
				oAuthAccessToken == null || oAuthAccessTokenSecret == null) {
			logger.error("#Twitter : Stream requires authentication");
			throw new StreamException("Stream requires authentication");
		}
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setJSONStoreEnabled(true)
			.setOAuthConsumerKey(oAuthConsumerKey)
			.setOAuthConsumerSecret(oAuthConsumerSecret)
			.setOAuthAccessToken(oAuthAccessToken)
			.setOAuthAccessTokenSecret(oAuthAccessTokenSecret);
		Configuration conf = cb.build();
			
		logger.info("#Twitter : Set handler");
		//handler = config.getHandler();
		
		TwitterFactory tf = new TwitterFactory(conf);
		twitter = tf.getInstance();		
		
		listener = getListener();
		twitterStream = new TwitterStreamFactory(conf).getInstance();	
		twitterStream.addListener(listener);	
		
		
		//logger.info("Start filtering.");
		//filter(filter);
		
		//List<Keyword> keywords = new ArrayList<Keyword>();
		//for(String keyword : filter.keywords())
		//	keywords.add(new Keyword(keyword, 0));
		
		//crawlerSpecsDAO.setKeywords(keywords , Source.Type.Twitter);
		//Thread updater = new Thread(new CrawlerSpecsUpdate(crawlerSpecsDAO));
		//updater.start();
		
	}

	@Override
	public synchronized void close() {
		if (listener != null) {
			if(twitterStream!=null)
				twitterStream.shutdown();
			if(twitter!=null)
				twitter.shutdown();
			listener = null;
			handler = null;
			twitterStream  = null;
			twitter=null;
		}
		logger.info("#Twitter : Close stream");
	}

	@Override
	public synchronized Integer search(List<Feed> feeds) throws StreamException {
		logger.info("#Twitter : Set stream");
		
		if (twitterStream == null) {
			logger.error("Stream is closed");
			throw new StreamException("Stream is closed", null);
		} 
		else {
			
			List<String> keys = new ArrayList<String>();
			List<String> users = new ArrayList<String>();
			List<double[]> locs = new ArrayList<double[]>();
			
			for(Feed feed : feeds){
				if(feed.getFeedtype().equals(FeedType.KEYWORDS)) {
					if(((KeywordsFeed) feed).getKeyword() != null)
						keys.add(((KeywordsFeed) feed).getKeyword().getName());
					else{
						for(Keyword keyword : ((KeywordsFeed) feed).getKeywords())
							keys.add(keyword.getName());
					}
						
				}
				else if(feed.getFeedtype().equals(FeedType.SOURCE)) {
					Source source = ((SourceFeed) feed).getSource();					
					users.add(source.getId());
				}
				else if(feed.getFeedtype().equals(FeedType.LOCATION)){
					double[] location = new double[2];
					
					location[0] = ((LocationFeed) feed).getLocation().getLatitude();
					location[1] = ((LocationFeed) feed).getLocation().getLongitude();
					locs.add(location);
				}
			}
			String[] keywords = new String[keys.size()];
			long[] follows = new long[users.size()];
			double[][] locations = new double[locs.size()][2];
			
			for(int i=0;i<keys.size();i++)
				keywords[i] = keys.get(i);
			
			for(int i=0;i<users.size();i++)
				follows[i] = Long.parseLong(users.get(i));
			
			for(int i=0;i<locs.size();i++)
				locations[i] = locs.get(i);
			
			if (!ensureFilterLimits(keywords, follows, locations)) {
				logger.error("Filter exceeds Twitter's public access level limits");
				throw new StreamException("Filter exceeds Twitter's public access level limits", null);
			}

			FilterQuery fq = getFilterQuery(keywords, follows, locations);
			if (fq != null) {
				if (System.currentTimeMillis() - lastFilterInitTime < FILTER_EDIT_WAIT_TIME){
                     try {
                    	 logger.info("Wait for " + FILTER_EDIT_WAIT_TIME + " msecs to edit filter");
                    	 wait(FILTER_EDIT_WAIT_TIME);
					} catch (InterruptedException e) {}
				}
				lastFilterInitTime = System.currentTimeMillis();
				
				logger.info("Start tracking from twitter stream");
				twitterStream.shutdown();
				twitterStream.filter(fq);
			
			}
			else {
				logger.info("Start sampling from twitter stream");
				twitterStream.sample();
			}
		}
		
		return 0;
	}

	private FilterQuery getFilterQuery(String[] keywords, long[] follows, double[][] locations) {
		FilterQuery query = new FilterQuery();
		boolean empty = true;
		if (keywords != null && keywords.length > 0) {
			query = query.track(keywords);
			empty = false;
		}
		
		if (follows != null && follows.length > 0) {
			query = query.follow(follows);
			empty = false;
		}
		
		if (locations != null && locations.length > 0) {
			query = query.locations(locations);
			empty = false;
		}
		
		if (empty) 
			return null;
		else 
			return query;
	}
	
	public long[] getUserIds(String[] followsUsernames) {
	
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
	
	
	private boolean ensureFilterLimits(String[] keywords, long[] follows, double[][] locations) {
		if (keywords != null && keywords.length > accessLevel.getFilterMaxKeywords()) return false;
		if (follows != null && follows.length > accessLevel.getFilterMaxFollows()) return false;
		if (locations != null && (locations.length/2) > accessLevel.getFilterMaxLocations()) return false;
		return true;
	}
	
	private StatusListener getListener() { 
		return new StatusListener() {
			@Override
			public void onStatus(Status status) {
				synchronized(this) {
					if(status != null){
						
						// Update original tweet in case of retweets
						Status retweetedStatus = status.getRetweetedStatus();
						if(retweetedStatus != null) {
							store(new TwitterItem(retweetedStatus));
						}
						
						// store
						store(new TwitterItem(status));
					
					}
				}
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				
					String id = Long.toString(statusDeletionNotice.getStatusId());
					TwitterItem update = new TwitterItem(id, Operation.DELETED);
					delete(update);
				
			}
			
			@Override
			public void onTrackLimitationNotice(int numOfLimitedStatuses) {
				synchronized(this) {
					logger.error("Rate limit: " + numOfLimitedStatuses);
				}
			}
			
			@Override
			public void onException(Exception ex) {
				synchronized(this){
					logger.error("Internal stream error occured: " + ex.getMessage());
				}
			}
			@Override
			public void onScrubGeo(long userid, long arg1) {
				logger.info("Remove appropriate geolocation information for user " +
						userid + " up to tweet with id " + arg1);
			}

			@Override
			public void onStallWarning(StallWarning warn) {	
				logger.error("Stall Warning " + warn.getMessage() + "(" +
						+ warn.getPercentFull() + ")");
			}
		};
	}

//	public class CrawlerSpecsUpdate implements Runnable {
//		
//		private Logger  LOG = Logger.getLogger(CrawlerSpecsUpdate.class);
//		
//		CrawlerSpecsDAO crawlerSpecs = null;
//		long update_period = 3000;
//		
//		public CrawlerSpecsUpdate(CrawlerSpecsDAO crawlerSpecs) {
//			this.crawlerSpecs = crawlerSpecs;
//		}
//		
//		@Override
//		public void run() {
//			while(true) {
//				try {
//					Thread.sleep(update_period);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//					continue;
//				}
//				
//				List<Keyword> keywords = crawlerSpecs.getTopKeywords(10, SOURCE);
//				System.out.println("=======UPDATE CRAWLER SPECS=====");
//				for(Keyword keyword : keywords) {
//					System.out.println(keyword.toJSONString());
//				}
//				System.out.println("================================");
//
//				List<String> filter_keywords = new ArrayList<String>(keywords.size());
//				for(Keyword keyword : keywords) {
//					filter_keywords.add(keyword.getName());
//				}
////				
////				List<Long> follows = new ArrayList<Long>(topKeywords.size());
////				for(String source : topSources) {
////					follows.add(Long.parseLong(source));
////				}
//				
//			
//			}
//		}
//		
//	}

	@Override
	public void search(Dysco dysco) throws StreamException {

		List<String> keywords = dysco.getKeywords();
		String queryStr = "";
		for(int i = 0; i < keywords.size() ; i++){
			if(i == keywords.size())
				queryStr += keywords.get(i);
			else
				queryStr += keywords.get(i) + ",";
		}
		
		try {	
			Query query = new Query(queryStr);
			int items = 0;
			while(true) {
				QueryResult response = twitter.search(query);		
				
				List<Status> statuses = response.getTweets();
				for(Status status : statuses) {
					if(status != null){
						TwitterItem item = new TwitterItem(status);
					
						items++;
						handler.update(item);
					}
				}
				if(!response.hasNext())
					break;
				response.nextQuery();
			}
			System.out.println(items + " items found!");
		} catch (TwitterException e) {
			throw new StreamException(e);
		}
	}	
	
	
}

