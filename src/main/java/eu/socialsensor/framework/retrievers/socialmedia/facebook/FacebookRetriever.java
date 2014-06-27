package eu.socialsensor.framework.retrievers.socialmedia.facebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookResponseStatusException;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.Page;
import com.restfb.types.Photo;
import com.restfb.types.Post;
import com.restfb.types.Post.Comments;
import com.restfb.types.User;

import eu.socialsensor.framework.abstractions.socialmedia.facebook.FacebookItem;
import eu.socialsensor.framework.abstractions.socialmedia.facebook.FacebookStreamUser;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.ListFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.monitors.RateLimitsMonitor;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.facebook.FacebookStream;

/**
 * Class responsible for retrieving facebook content based on keywords or facebook users/facebook pages
 * The retrieval process takes place through facebook graph API.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 * 
 */
public class FacebookRetriever implements SocialMediaRetriever {
	
	private RateLimitsMonitor rateLimitsMonitor;
			
	private FacebookClient facebookClient;
	private FacebookStream fbStream;
	
	private int maxResults;
	private int maxRequests;
	
	private long maxRunningTime;
	private long currRunningTime = 0L;
	
	private Logger  logger = Logger.getLogger(FacebookRetriever.class);
	
	public FacebookRetriever(String  facebookAccessToken) {
		this.facebookClient = new DefaultFacebookClient(facebookAccessToken);
	}
	
	public FacebookRetriever(FacebookClient facebookClient, int maxRequests, long minInterval,Integer maxResults, long maxRunningTime,FacebookStream fbStream) {
		this.facebookClient = facebookClient;		
		this.fbStream = fbStream;
		this.rateLimitsMonitor = new RateLimitsMonitor(maxRequests, minInterval);
		this.maxResults = maxResults;
		this.maxRequests = maxRequests;
		this.maxRunningTime = maxRunningTime;
	}
	

	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		Integer totalRetrievedItems = 0;
		Integer totalRequests = 0;
		
		Date lastItemDate = feed.getDateToRetrieve();
		String label = feed.getLabel();
		
		boolean isFinished = false;
		
		Source source = feed.getSource();
		
		String userName = source.getName();
		if(userName == null){
			logger.info("#Facebook : No source feed");
			return totalRetrievedItems;
		}
		String userFeed = source.getName()+"/feed";
		
		Connection<Post> connection = facebookClient.fetchConnection(userFeed , Post.class);
		User page = facebookClient.fetchObject(userName, User.class);
		
		FacebookStreamUser facebookUser = new FacebookStreamUser(page);
		System.out.println(facebookUser);
		
		for(List<Post> connectionPage : connection) {
			rateLimitsMonitor.check();
			totalRequests++;
			for(Post post : connectionPage) {	
				
				Date publicationDate = post.getCreatedTime();
				
				if(publicationDate.after(lastItemDate) && post!=null && post.getId() != null) {
					FacebookItem facebookUpdate = new FacebookItem(post, facebookUser);
					facebookUpdate.setList(label);
					
					if(fbStream != null)
						fbStream.store(facebookUpdate);
					
				    totalRetrievedItems++;
				    
				    Comments comments = post.getComments();
				    if(comments == null)
			    		continue;

			    	for(Comment comment : comments.getData()) {
			    		FacebookItem facebookComment = new FacebookItem(comment, post, null);
			    		facebookComment.setList(label);
			    		
			    		if(fbStream != null)
			    			fbStream.store(facebookComment);
			    	} 
				
					/*
					// Test Code
				    Connection<Comments> commentsConn = facebookClient.fetchConnection(post.getId()+"/comments", Comments.class);
				    for(List<Comments> commentsList : commentsConn) {
				    	for (Comments comments : commentsList) {
				    	if(comments == null)
				    		continue;

				    	for(Comment comment : comments.getData()) {
				    			FacebookItem facebookComment = new FacebookItem(comment, post, null);
				    			fbStream.store(facebookComment);
				    		}
				    	}
				    }
				    */
				 }
				
				if(publicationDate.before(lastItemDate) || totalRetrievedItems>maxResults || totalRequests>maxRequests){
					isFinished = true;
					break;
				}
			
			}
			if(isFinished)
				break;
			
		}

		// The next request will retrieve only items of the last day
		Date dateToRetrieve = new Date(System.currentTimeMillis() - (24*3600*1000));
		feed.setDateToRetrieve(dateToRetrieve);
		
		//logger.info("#Facebook : Done retrieving for this session");
//		logger.info("#Facebook : Handler fetched " + items.size() + " posts from " + uName + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed){
		Integer totalRetrievedItems = 0;
		
		currRunningTime = System.currentTimeMillis();
		
		Date lastItemDate = feed.getDateToRetrieve();
		String label = feed.getLabel();
		
		boolean isFinished = false;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Facebook : No keywords feed");
			return totalRetrievedItems;
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
			return totalRetrievedItems;
		
		Connection<Post> connection = null;
		try{
			
			
			connection = facebookClient.fetchConnection("search",Post.class,
					Parameter.with("q",tags),Parameter.with("type","post"));
			
			
		}catch(FacebookResponseStatusException e1){
			
			return totalRetrievedItems;
		}
		catch(Exception e2){
			return totalRetrievedItems;
		}
		try{
			for(List<Post> connectionPage : connection) {
				
				for(Post post : connectionPage) {	
					
					Date publicationDate = post.getCreatedTime();
					try{
						if(publicationDate.after(lastItemDate) && post!=null && post.getId()!=null){
							//Get the user of the post
							CategorizedFacebookType c_user = post.getFrom();
							User user = facebookClient.fetchObject(c_user.getId(), User.class);
							FacebookStreamUser facebookUser = new FacebookStreamUser(user);
							
							FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
							facebookUpdate.setList(label);
							
							if(fbStream != null)
								fbStream.store(facebookUpdate);
							totalRetrievedItems++;
						}
					}
					catch(Exception e){
						break;
					}
					
					if(publicationDate.before(lastItemDate) || totalRetrievedItems>maxResults || (System.currentTimeMillis() - currRunningTime) > maxRunningTime){
						isFinished = true;
						break;
					}
					
				}
				if(isFinished)
					break;
				
			}
		}
		catch(FacebookNetworkException e1){
			return totalRetrievedItems;
		}
		
//		logger.info("#Facebook : Done retrieving for this session");
//		logger.info("#Facebook : Handler fetched " + items.size() + " posts from " + tags + 
//			" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		// The next request will retrieve only items of the last day
		Date dateToRetrieve = new Date(System.currentTimeMillis() - (24*3600*1000));
		feed.setDateToRetrieve(dateToRetrieve);
		
		return totalRetrievedItems;
	}
	
	
	public Integer retrieveLocationFeeds(LocationFeed feed){
		return 0;
	}
	/**
	 * Retrieve from certain facebook pages after a specific date
	 * @param date
	 */
	public void retrieveFromPages(List<String> retrievedPages,Date date) {
		Integer totalRetrievedItems = 0;
		boolean isFinished = true;
		
		for(String page : retrievedPages){
			Connection<Post> connection = facebookClient.fetchConnection(page+"/posts" , Post.class);
			
			for(List<Post> connectionPage : connection) {
					
				for(Post post : connectionPage) {	
					
					Date publicationDate = post.getCreatedTime();
					try{
						if(publicationDate.after(date) && post!=null && post.getId()!=null){
							//Get the user of the post
							CategorizedFacebookType c_user = post.getFrom();
							User user = facebookClient.fetchObject(c_user.getId(), User.class);
							FacebookStreamUser facebookUser = new FacebookStreamUser(user);
							
							FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
							
							if(fbStream != null)
								fbStream.store(facebookUpdate);
							
							totalRetrievedItems++;
						}
					}
					catch(Exception e){
						break;
					}
					
					if(publicationDate.before(date) || totalRetrievedItems>maxResults){
						isFinished = true;
						break;
					}
					
				}
				if(isFinished)
					break;
				
			}
			
		}
	}
	
	@Override
	public Integer retrieveListsFeeds(ListFeed feed) {
		return 0;
	}
	
	@Override
	public Integer retrieve (Feed feed) {
		
		switch(feed.getFeedtype()){
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				if(!userFeed.getSource().getNetwork().equals("Facebook"))
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
		if(facebookClient != null)
			facebookClient = null;
	}

	@Override
	public MediaItem getMediaItem(String mediaId) {
		Photo photo = facebookClient.fetchObject(mediaId, Photo.class);
		
		if(photo == null)
			return null;

		MediaItem mediaItem = null;
		try {
			String src = photo.getSource();
			mediaItem = new MediaItem(new URL(src));
			mediaItem.setId("Facebook#" + photo.getId());
			
			mediaItem.setPageUrl(photo.getLink());
			mediaItem.setThumbnail(photo.getPicture());
			
			mediaItem.setStreamId("Facebook");
			mediaItem.setType("image");
			
			mediaItem.setTitle(photo.getName());
			
			Date date = photo.getCreatedTime();
			mediaItem.setPublicationTime(date.getTime());
			
			mediaItem.setSize(photo.getWidth(), photo.getHeight());
			mediaItem.setLikes((long) photo.getLikes().size());
			
			CategorizedFacebookType from = photo.getFrom();
			if(from != null) {
				StreamUser streamUser = new FacebookStreamUser(from);
				mediaItem.setUser(streamUser);
				mediaItem.setUserId(streamUser.getUserid());
			}
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return mediaItem;
	}

	@Override
	public StreamUser getStreamUser(String uid) {
		try {
			Page page = facebookClient.fetchObject(uid, Page.class);
			StreamUser facebookUser = new FacebookStreamUser(page);
			return facebookUser;
		}
		catch(Exception e) {
			return null;
		}
	}
	
	public static void main(String...args) {
		
	}
}
