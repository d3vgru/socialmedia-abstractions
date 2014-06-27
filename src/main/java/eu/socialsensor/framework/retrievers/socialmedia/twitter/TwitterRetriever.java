package eu.socialsensor.framework.retrievers.socialmedia.twitter;

import java.util.Date;
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
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.ListFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.twitter.TwitterStream;

/**
 * Class responsible for retrieving Twitter content based on keywords, twitter users or locations
 * The retrieval process takes place through Twitter API (twitter4j)
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TwitterRetriever implements SocialMediaRetriever {
	
	private Logger  logger = Logger.getLogger(TwitterRetriever.class);
	
	private Twitter twitter = null;
	private TwitterFactory tf = null;
	
	private TwitterStream twStream;
	
	private int maxResults = 500;
	private int maxRequests = 1;
	
	private long maxRunningTime = 0l;
	
	public TwitterRetriever(Configuration conf, Integer maxRequests,Integer maxResults, Long maxRunningTime, TwitterStream twStream) {
		
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
	public Integer retrieveUserFeeds(SourceFeed feed) {
		
		int count = 200;
		long currRunningTime = System.currentTimeMillis();
		
		Integer totalRetrievedItems = 0, numberOfRequests = 0;
		
		Source source = feed.getSource();
		if(source == null)
			return totalRetrievedItems;
		
		Date sinceDate = feed.getDateToRetrieve();
		Date newSinceDate = sinceDate;
		
		String label = feed.getLabel();
		
		String userId = source.getId();
		String screenName = source.getName();
		
		int page = 1;
		Paging paging = new Paging(page, count);
		boolean sinceDateReached = false;
		while(true) {
			try {
				ResponseList<Status> response = null;
				if(userId != null) {
					response = twitter.getUserTimeline(Integer.parseInt(userId), paging);
				}
				else if(screenName != null) {
					logger.info("Retrieve timeline for " + screenName + ". Page: " + paging);
					response = twitter.getUserTimeline(screenName, paging);
				}
				else {
					break;
				}
				numberOfRequests++;
				
				for(Status status : response) {
					if(status != null) {
						
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(newSinceDate.before(createdAt)) {
								newSinceDate = new Date(createdAt.getTime());
							}
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						TwitterItem twitterItem = new TwitterItem(status);
						twitterItem.setList(label);

						if(twStream != null) {
							twStream.store(twitterItem);
						}
						
						totalRetrievedItems++;
					}
				}
				
				if(totalRetrievedItems > maxResults) {
					logger.info("totalRetrievedItems: " + totalRetrievedItems + " > " + maxResults);
					break;
				}
				if(numberOfRequests >= maxRequests) {
					logger.info("numberOfRequests: " + numberOfRequests + " > " + maxRequests);
					break;
				}
				if((System.currentTimeMillis() - currRunningTime)>maxRunningTime) {
					logger.info("Max running time reached. " + (
							System.currentTimeMillis() - currRunningTime) + ">" + maxRunningTime);
					break;
				}
				if(sinceDateReached) {
					logger.info("Since date reached: " + sinceDate);
					break;
				}
				
				paging.setPage(++page);
			} catch (TwitterException e) {
				e.printStackTrace();
				break;
			}
		}

		feed.setDateToRetrieve(newSinceDate);
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed) {
		int count = 100;
		int numberOfRequests = 0;
	
		Integer totalRetrievedItems = 0;
		Date sinceDate = feed.getDateToRetrieve();
		Date newSinceDate = sinceDate;
		
		String label = feed.getLabel();
		
		long currRunningTime = System.currentTimeMillis();
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Twitter : No keywords feed");
			return totalRetrievedItems;
		}
		
		
		String tags = "";
		
		if(keyword != null) {
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
		logger.info("Query String: " + tags);
		Query query = new Query(tags);
	
		query.count(count);
		query.setResultType(Query.RECENT); //do not set last item date-causes problems!

		boolean sinceDateReached = false;
		try {
			logger.info("Request for " + query);
			QueryResult response = twitter.search(query);
			
			while(response != null) {
				numberOfRequests++;
				
				List<Status> statuses = response.getTweets();
				
				if(statuses == null || statuses.isEmpty()) {
					logger.info("No more results.");	
					break;
				}
				
				logger.info(statuses.size() + " statuses retrieved.");	
				
				for(Status status : statuses) {
					if(status != null) {
						
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(newSinceDate.before(createdAt)) {
								newSinceDate = new Date(createdAt.getTime());
							}
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						TwitterItem twitterItem = new TwitterItem(status);
						twitterItem.setList(label);
						
						if(twStream != null) {
							twStream.store(twitterItem);
						}
						
						totalRetrievedItems++;
					}

				}
				
				if(totalRetrievedItems > maxResults) {
					logger.info("totalRetrievedItems: " + totalRetrievedItems + " > " + maxResults);
					break;
				}
				if(numberOfRequests >= maxRequests) {
					logger.info("numberOfRequests: " + numberOfRequests + " > " + maxRequests);
					break;
				}
				if((System.currentTimeMillis() - currRunningTime)>maxRunningTime) {
					logger.info("Max running time reached. " + (System.currentTimeMillis() - currRunningTime) + ">" + maxRunningTime);
					break;
				}
				if(sinceDateReached) {
					logger.info("Since date reached: " + sinceDate);
					break;
				}
			
				query = response.nextQuery();
				if(query == null)
					break;
				
				logger.info("Request for " + query);
				response = twitter.search(query);
			}
			
		} catch (TwitterException e) {
			e.printStackTrace();
		}	
	
		feed.setDateToRetrieve(newSinceDate);
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed) {
		
		int count = 100;
		
		Integer totalRetrievedItems = 0, numberOfRequests = 0;
		Date sinceDate = feed.getDateToRetrieve();
		
		Location location = feed.getLocation();
		if(location == null)
			return totalRetrievedItems;
		
		long currRunningTime = System.currentTimeMillis();
		
		//Set the query
		Query query = new Query();
		Double radius = location.getRadius();
		if(radius == null) {
			radius = 1.5; // default radius 1.5 Km 
		}
		
		GeoLocation geoLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
		query.setGeoCode(geoLocation, radius, Query.KILOMETERS);
		query.count(count);
				
		boolean sinceDateReached = false;
		while(true) {
			try {
				numberOfRequests++;
				QueryResult response = twitter.search(query);
				
				
				List<Status> statuses = response.getTweets();
				for(Status status : statuses) {
					if(status != null) {
						
						if(sinceDate != null) {
							Date createdAt = status.getCreatedAt();
							if(sinceDate.after(createdAt)) {
								sinceDateReached = true;
								break;
							}
						}
						
						TwitterItem twitterItem = new TwitterItem(status);
						twStream.store(twitterItem);
						totalRetrievedItems++;
					}
				}
				
				if(!response.hasNext()) {
					logger.info("There is not next query.");
					break;
				}
				if(totalRetrievedItems > maxResults) {
					logger.info("totalRetrievedItems: " + totalRetrievedItems + " > " + maxResults);
					break;
				}
				if(numberOfRequests > maxRequests) {
					logger.info("numberOfRequests: " + numberOfRequests + " > " + maxRequests);
					break;
				}
				if((System.currentTimeMillis() - currRunningTime)>maxRunningTime) {
					logger.info("Max running time reached. " + (System.currentTimeMillis() - currRunningTime) + ">" + maxRunningTime);
					break;
				}
				if(sinceDateReached) {
					logger.info("Since date reached: " + sinceDate);
					break;
				}
				
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

		String label = feed.getLabel();
			
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
						twitterItem.setList(label);
						
						if(twStream != null) {
							twStream.store(twitterItem);
						}
						
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
				if(!userFeed.getSource().getNetwork().equals("Twitter"))
					return 0;
				
				return retrieveUserFeeds(userFeed);
				
			
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
				
			case LOCATION:
				LocationFeed locationFeed = (LocationFeed) feed;
				
				return retrieveLocationFeeds(locationFeed);
			
			case LIST:
				ListFeed listFeed = (ListFeed) feed;
				
				return retrieveListsFeeds(listFeed);
			default:
				logger.error("Unkonwn Feed Type: " + feed.toJSONString());
				break;	
			
		}
		return 0;
	}
	
	@Override
	public void stop(){
		if(twitter != null)
			twitter.shutdown();
		
		twitter = null;
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
