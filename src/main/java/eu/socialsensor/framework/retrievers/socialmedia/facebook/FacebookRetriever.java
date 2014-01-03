package eu.socialsensor.framework.retrievers.socialmedia.facebook;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.restfb.Connection;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookResponseStatusException;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Page;
import com.restfb.types.Post;
import com.restfb.types.User;


import eu.socialsensor.framework.abstractions.socialmedia.facebook.FacebookItem;
import eu.socialsensor.framework.abstractions.socialmedia.facebook.FacebookStreamUser;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.monitors.RateLimitsMonitor;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;



/**
 * The retriever that implements the Facebook wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 * 
 */
public class FacebookRetriever implements SocialMediaRetriever {
	
	private RateLimitsMonitor rateLimitsMonitor;
			
	private FacebookClient facebookClient;
	
	private int maxResults;
	private int maxRequests;
	
	private Logger  logger = Logger.getLogger(FacebookRetriever.class);
	
	public FacebookRetriever(FacebookClient facebookClient, int maxRequests, long minInterval,Integer maxResults) {
		this.facebookClient = facebookClient;		
		this.rateLimitsMonitor = new RateLimitsMonitor(maxRequests, minInterval);
		this.maxResults = maxResults;
		this.maxRequests = maxRequests;
	}
	
	@Override
	public List<Item> retrieveUserFeeds(SourceFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getDateToRetrieve();
		
		boolean isFinished = false;
		
		Source source = feed.getSource();
		
		String userName = source.getName();
		if(userName == null){
			logger.info("#Facebook : No source feed");
			return null;
		}
		String userFeed = source.getName()+"/feed";
		
		//logger.info("#Facebook : Retrieving User Feed : "+userName);
		
		Connection<Post> connection = facebookClient.fetchConnection(userFeed , Post.class);
		Page page = facebookClient.fetchObject(userName, Page.class);
		FacebookStreamUser facebookUser = new FacebookStreamUser(page);
		
		for(List<Post> connectionPage : connection) {
			rateLimitsMonitor.check();
			
			//logger.info("#Facebook : Retrieving page "+it+" that contains "+connectionPage.size()+" posts");
			
			for(Post post : connectionPage) {	
				
				Date publicationDate = post.getCreatedTime();
				
				if(publicationDate.after(lastItemDate) && post!=null && post.getId()!=null){
					FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
				    items.add(facebookUpdate);	
				}
				
				if(publicationDate.before(lastItemDate) || items.size()>maxResults){
					isFinished = true;
					break;
				}
			
			}
			if(isFinished)
				break;
			
		}

		//logger.info("#Facebook : Done retrieving for this session");
//		logger.info("#Facebook : Handler fetched " + items.size() + " posts from " + uName + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
	}
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getDateToRetrieve();
		
		
		
		boolean isFinished = false;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Facebook : No keywords feed");
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
		
		//logger.info("#Facebook : Retrieving Keywords Feed : "+tags);
		Connection<Post> connection = null;
		try{
			connection = facebookClient.fetchConnection("search",Post.class,
					Parameter.with("q",tags),Parameter.with("type","post"));
			
		}catch(FacebookResponseStatusException e1){
			
			return items;
		}
		for(List<Post> connectionPage : connection) {
						
			//logger.info("#Facebook : Retrieving page "+it+" that contains "+connectionPage.size()+" posts");
			
			for(Post post : connectionPage) {	
				
				Date publicationDate = post.getCreatedTime();
				
				if(publicationDate.after(lastItemDate) && post!=null && post.getId()!=null){
					//Get the user of the post
					CategorizedFacebookType c_user = post.getFrom();
					User user = facebookClient.fetchObject(c_user.getId(), User.class);
					FacebookStreamUser facebookUser = new FacebookStreamUser(user);
					
					FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
					items.add(facebookUpdate);
				  
				}
				
				if(publicationDate.before(lastItemDate) || items.size()>maxResults){
					isFinished = true;
					break;
				}
				
			}
			if(isFinished)
				break;
			
		}
		
//		logger.info("#Facebook : Done retrieving for this session");
//		logger.info("#Facebook : Handler fetched " + items.size() + " posts from " + tags + 
//			" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
	
		return items;
	}
	
	
	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		return null;
	}
	
	@Override
	public List<Item> retrieve (Feed feed) {
		
		switch(feed.getFeedtype()){
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				return retrieveUserFeeds(userFeed);
				
			
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
				
			case LOCATION:
				logger.error("#Facebook : Location Feed cannot be retreived from Facebook");
				
				return null;
			
		}
		return null;
	}
	
	@Override
	public void stop(){
		if(facebookClient != null)
			facebookClient = null;
	}
}
