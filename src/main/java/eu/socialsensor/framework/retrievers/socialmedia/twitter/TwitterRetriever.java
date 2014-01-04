package eu.socialsensor.framework.retrievers.socialmedia.twitter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.twitter.TwitterStream;

public class TwitterRetriever implements SocialMediaRetriever{
	private Logger  logger = Logger.getLogger(TwitterRetriever.class);
	
	private Twitter twitter = null;
	private TwitterFactory tf = null;
	
	private TwitterStream twStream;
	
	private int maxResults = 1;
	private int maxRequests = 1 ;
	
	public TwitterRetriever(Configuration conf,TwitterStream twStream){
		
		this.tf = new TwitterFactory(conf);
		twitter = tf.getInstance();
		
		this.twStream = twStream;
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		return 0;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed) {
		int count = 100 , numberOfRequests = 0;
		String resultType = "recent";
	
		Integer totalRetrievedItems = 0;
		
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
		query.setCount(count);
		query.setResultType(resultType);//do not set last item date-causes problems!
		
		while(true) {
			try {
				QueryResult response = twitter.search(query);
			
				numberOfRequests++;
				
				List<Status> statuses = response.getTweets();
				
				for(Status status : statuses) {
					if(status != null){
						TwitterItem twitterItem = new TwitterItem(status);
						twStream.store(twitterItem);
						totalRetrievedItems++;
					}
				}
				
				if(!response.hasNext() || numberOfRequests>maxRequests || totalRetrievedItems>maxResults)
					break;
				
				response.nextQuery();
				
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed){
		return 0;
	}
	
	@Override
	public Integer retrieve(Feed feed){
		
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
	
}
