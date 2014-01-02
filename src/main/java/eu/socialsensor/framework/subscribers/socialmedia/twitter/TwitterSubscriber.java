package eu.socialsensor.framework.subscribers.socialmedia.twitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;

import eu.socialsensor.framework.abstractions.socialmedia.twitter.TwitterItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.Feed.FeedType;
import eu.socialsensor.framework.common.domain.Item.Operation;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.streams.StreamException;
import eu.socialsensor.framework.streams.socialmedia.twitter.TwitterStream;
import eu.socialsensor.framework.subscribers.socialmedia.Subscriber;

public class TwitterSubscriber implements Subscriber{
	private Logger  logger = Logger.getLogger(TwitterSubscriber.class);
	
	public static long FILTER_EDIT_WAIT_TIME = 10000;
	
	private long lastFilterInitTime = System.currentTimeMillis();
	
	private TwitterStream twitStream;
	
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
	
	
	private AccessLevel accessLevel = AccessLevel.PUBLIC;
	private StatusListener listener = null;
	private twitter4j.TwitterStream twitterStream  = null;
	
	
	public TwitterSubscriber(Configuration conf,TwitterStream twitStream){
		
		if (twitterStream != null) {
			logger.error("#Twitter : Stream is already opened");
			try {
				throw new StreamException("Stream is already opened", null);
			} catch (StreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.twitStream = twitStream;
		
		listener = getListener();
		twitterStream = new TwitterStreamFactory(conf).getInstance();	
		twitterStream.addListener(listener);	
		
	}
	
	@Override
	public synchronized void subscribe(List<Feed> feeds) throws StreamException {
		
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
		
	}
	
	@Override
	public void stop(){
		if (listener != null) {
			if(twitterStream!=null)
				twitterStream.shutdown();
			
			listener = null;
			twitterStream  = null;
			
		}
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
							twitStream.store(new TwitterItem(retweetedStatus));
						}
						
						// store
						twitStream.store(new TwitterItem(status));
					
					}
				}
			}
			
			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				
					String id = Long.toString(statusDeletionNotice.getStatusId());
					TwitterItem update = new TwitterItem(id, Operation.DELETED);
					twitStream.delete(update);
				
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

	
	private boolean ensureFilterLimits(String[] keywords, long[] follows, double[][] locations) {
		if (keywords != null && keywords.length > accessLevel.getFilterMaxKeywords()) return false;
		if (follows != null && follows.length > accessLevel.getFilterMaxFollows()) return false;
		if (locations != null && (locations.length/2) > accessLevel.getFilterMaxLocations()) return false;
		return true;
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
}
