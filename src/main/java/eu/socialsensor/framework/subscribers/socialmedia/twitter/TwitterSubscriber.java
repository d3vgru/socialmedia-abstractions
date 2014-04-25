package eu.socialsensor.framework.subscribers.socialmedia.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.Paging;
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

	private Twitter twitterApi;
	
	
	public TwitterSubscriber(Configuration conf,TwitterStream twitStream) {
		
		if (twitterStream != null) {
			logger.error("#Twitter : Stream is already opened");
			try {
				throw new StreamException("Stream is already opened", null);
			} catch (StreamException e) {
				e.printStackTrace();
			}
		}
		
		this.twitStream = twitStream;
		
		listener = getListener();
		twitterStream = new TwitterStreamFactory(conf).getInstance();	
		twitterStream.addListener(listener);	
		
		this.twitterApi = new TwitterFactory(conf).getInstance();
		
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
			List<Long> userids = new ArrayList<Long>();
			List<double[]> locs = new ArrayList<double[]>();
			
			for(Feed feed : feeds) {
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
					if(source.getId() == null) {
						try {
							users.add(source.getName());
						}
						catch(Exception e) {
							continue;
						}
					}
					else {
						userids.add(Long.parseLong(source.getId()));
					}
				}
				else if(feed.getFeedtype().equals(FeedType.LOCATION)) {
					double[] location = new double[2];
					
					location[0] = ((LocationFeed) feed).getLocation().getLatitude();
					location[1] = ((LocationFeed) feed).getLocation().getLongitude();
					locs.add(location);
				}
			}
			
			Set<Long> temp = getUserIds(users);
			userids.addAll(temp);
			
			String[] keywords = new String[keys.size()];
			long[] follows = new long[userids.size()];
			double[][] locations = new double[locs.size()][2];
			
			for(int i=0;i<keys.size();i++)
				keywords[i] = keys.get(i);
			
			for(int i=0;i<userids.size();i++)
				follows[i] = userids.get(i);
			
			for(int i=0;i<locs.size();i++)
				locations[i] = locs.get(i);
			
			if (!ensureFilterLimits(keywords, follows, locations)) {
				logger.error("Filter exceeds Twitter's public access level limits");
				throw new StreamException("Filter exceeds Twitter's public access level limits", null);
			}

			FilterQuery fq = getFilterQuery(keywords, follows, locations);
			if (fq != null) {
				
				//getPastTweets(keywords, follows);
				
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
	
	public void getPastTweets(String[] keywords, long[] userids) {
		
		int totalRequests = 0;
		
		List<Status> tweets = new ArrayList<Status>();
		if(keywords != null && keywords.length>0) {
			Query query = new Query();
			query.setQuery(StringUtils.join(keywords, " OR "));
			query.setCount(100);
			do {
				System.out.println(query.toString());
				QueryResult resp;
				try {
					totalRequests++;
					resp = twitterApi.search(query);
					tweets.addAll(resp.getTweets());
					query = resp.nextQuery();
				} catch (TwitterException e) { 
					e.printStackTrace();
					break;
				}

			} while(query != null && totalRequests < 180);
		}

		if(userids != null) {
			int mapPagesPerUser = 180 / userids.length;
			for(long userid : userids) {
				try {
					int page = 1, count = 100;
					Paging paging = new Paging(page, count);
					while(true) {
						totalRequests++;
						ResponseList<Status> timeline = twitterApi.getUserTimeline(userid, paging);
						tweets.addAll(timeline);

						System.out.println(paging.toString() + " => " + tweets.size());
						paging.setPage(++page);
						paging.setCount(100);
						
						if(timeline.size()<count || page>mapPagesPerUser 
								|| totalRequests>180) {
							break;
						}

					}

				} catch (TwitterException e) {
					logger.error(e);
					break;
				}
			}
			for(Status status : tweets) {
				listener.onStatus(status);
			}
		}
	}
	
	private Set<Long> getUserIds(List<String> followsUsernames) {
		
		Set<Long> ids = new HashSet<Long>();
		
		List<String> usernames = new ArrayList<String>(followsUsernames.size());
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
				ResponseList<User> users = twitterApi.lookupUsers(_usernames);
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
		
		return ids;
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
