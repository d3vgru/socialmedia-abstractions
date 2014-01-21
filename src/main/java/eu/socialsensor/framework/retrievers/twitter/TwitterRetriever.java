package eu.socialsensor.framework.retrievers.twitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import eu.socialsensor.framework.abstractions.twitter.TwitterItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;

public class TwitterRetriever implements Retriever{
	private Logger  logger = Logger.getLogger(TwitterRetriever.class);
	
	private Twitter twitter = null;
	private TwitterFactory tf = null;
	
	private int maxResults;
	private int maxRequests;
	
	public TwitterRetriever(Configuration conf){
		
		this.tf = new TwitterFactory(conf);
		twitter = tf.getInstance();		
	}
	
	@Override
	public List<Item> retrieveUserFeeds(SourceFeed feed){
		return null;
	}
	
	public Map<String, String> retrieveUserPictures(long[] ids) throws TwitterException {
		Map<String, String> map = new HashMap<String, String>();
		ResponseList<User> users = twitter.lookupUsers(ids);
		for(User user : users) {
			String id = "Twitter#" + user.getId();
			String pic = user.getProfileImageURL();
			
			map.put(id, pic);
			
		}
		
		return map;
		
	}
	
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed) {
		int count = 100 , numberOfRequests = 0;
		String resultType = "recent";
		Date lastItemDate = feed.getLastItemDate();
		List<Item> items = new ArrayList<Item>();
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Twitter : No keywords feed");
			return items;
		}
		
		
		String tags = "";
		
		if(keyword != null){
		
			tags += keyword.getName().toLowerCase();
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
			return items;
		
		//Set the query
		Query query = new Query(tags);
		query.setCount(count);
		query.setResultType(resultType);
		query.setSince(lastItemDate.toString());
		
		while(true) {
			try {
				QueryResult response = twitter.search(query);
		
				numberOfRequests++;
				
				List<Status> statuses = response.getTweets();
				for(Status status : statuses) {
					if(status != null){
						TwitterItem item = new TwitterItem(status);
						items.add(item);
					}
				}
				
				if(!response.hasNext() || numberOfRequests>maxRequests || items.size()>maxResults)
					break;
				
				response.nextQuery();
				
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		return items;
	}
	
	@Override
	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		return null;
	}
	
	@Override
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
