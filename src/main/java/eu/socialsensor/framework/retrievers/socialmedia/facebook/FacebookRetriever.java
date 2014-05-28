package eu.socialsensor.framework.retrievers.socialmedia.facebook;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
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
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.monitors.RateLimitsMonitor;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.facebook.FacebookStream;



/**
 * The retriever that implements the Facebook wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 * 
 */
public class FacebookRetriever implements SocialMediaRetriever {
	
	private RateLimitsMonitor rateLimitsMonitor;
			
	private FacebookClient facebookClient;
	private FacebookStream fbStream;
	
	private List<String> retrievedPages = new ArrayList<String>();
	
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
	
	public List<String> getRetrievedFbPages(){
		return retrievedPages;
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		Integer totalRetrievedItems = 0;
		
		Date lastItemDate = feed.getDateToRetrieve();
		
		boolean isFinished = false;
		
		Source source = feed.getSource();
		
		String userName = source.getName();
		if(userName == null){
			logger.info("#Facebook : No source feed");
			return totalRetrievedItems;
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
				
				if(publicationDate.after(lastItemDate) && post!=null && post.getId() != null) {
					FacebookItem facebookUpdate = new FacebookItem(post, facebookUser);
				    fbStream.store(facebookUpdate);
				    totalRetrievedItems++;
				    
				    Comments comments = post.getComments();
				    if(comments == null)
			    		continue;

			    	for(Comment comment : comments.getData()) {
			    		FacebookItem facebookComment = new FacebookItem(comment, post, null);
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
				
				if(publicationDate.before(lastItemDate) || totalRetrievedItems>maxResults){
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
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed){
		Integer totalRetrievedItems = 0;
		
		currRunningTime = System.currentTimeMillis();
		
		Date lastItemDate = feed.getDateToRetrieve();
		
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
		
		//logger.info("#Facebook : Retrieving Keywords Feed : "+tags);
		Connection<Post> connection = null;
		try{
			
			
			connection = facebookClient.fetchConnection("search",Post.class,
					Parameter.with("q",tags),Parameter.with("type","post"));
			
			//Connection<Page> page_connection = facebookClient.fetchConnection("search",Page.class,
					//Parameter.with("q",tags),Parameter.with("type","page"));
			
			/*for(List<Page> pageConnection : page_connection)
				for(Page page : pageConnection){
					System.out.println("page : "+page.getName());
					retrievedPages.add(page.getName());
				}*/
			
		}catch(FacebookResponseStatusException e1){
			
			return totalRetrievedItems;
		}
		for(List<Post> connectionPage : connection) {
						
			//logger.info("#Facebook : Retrieving page "+it+" that contains "+connectionPage.size()+" posts");
			
			for(Post post : connectionPage) {	
				
				Date publicationDate = post.getCreatedTime();
				try{
					if(publicationDate.after(lastItemDate) && post!=null && post.getId()!=null){
						//Get the user of the post
						CategorizedFacebookType c_user = post.getFrom();
						User user = facebookClient.fetchObject(c_user.getId(), User.class);
						FacebookStreamUser facebookUser = new FacebookStreamUser(user);
						
						FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
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
		
//		logger.info("#Facebook : Done retrieving for this session");
//		logger.info("#Facebook : Handler fetched " + items.size() + " posts from " + tags + 
//			" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		
	
		return totalRetrievedItems;
	}
	
	
	public Integer retrieveLocationFeeds(LocationFeed feed){
		return 0;
	}
	
	public void retrieveFromRetrievedPages(Date date) {
		Integer totalRetrievedItems = 0;
		boolean isFinished = true;
		
		for(String page : retrievedPages){
			Connection<Post> connection = facebookClient.fetchConnection(page+"/posts" , Post.class);
			
			for(List<Post> connectionPage : connection) {
				
				//logger.info("#Facebook : Retrieving page "+it+" that contains "+connectionPage.size()+" posts");
				
				for(Post post : connectionPage) {	
					
					Date publicationDate = post.getCreatedTime();
					try{
						if(publicationDate.after(date) && post!=null && post.getId()!=null){
							//Get the user of the post
							CategorizedFacebookType c_user = post.getFrom();
							User user = facebookClient.fetchObject(c_user.getId(), User.class);
							FacebookStreamUser facebookUser = new FacebookStreamUser(user);
							
							FacebookItem facebookUpdate = new FacebookItem(post,facebookUser);
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
	public Integer retrieve (Feed feed) {
		
		switch(feed.getFeedtype()){
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				return retrieveUserFeeds(userFeed);
				
			
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
				
			case LOCATION:
				logger.error("#Facebook : Location Feed cannot be retreived from Facebook");
				
				return 0;
			
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
