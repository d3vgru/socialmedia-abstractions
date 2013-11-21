package eu.socialsensor.framework.retrievers.instagram;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import org.jinstagram.Instagram;
import org.jinstagram.exceptions.InstagramException;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.locations.LocationSearchFeed;
import org.jinstagram.entity.tags.TagMediaFeed;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeed;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.auth.model.Token;

import eu.socialsensor.framework.abstractions.instagram.InstagramItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Location;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;
import eu.socialsensor.framework.streams.StreamConfiguration;

/**
 * The retriever that implements the Instagram wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramRetriever implements Retriever {
	
	private Logger logger = Logger.getLogger(InstagramRetriever.class);
	
	StreamConfiguration instagramConfStorage;

	private String instagramKey;
	private String instagramSecret;
	private Token instagramToken;
	
	private Instagram instagram = null;

	private int results_threshold;
	private int request_threshold;
	
	List<Feed> feeds = new ArrayList<Feed>();
	
	MediaFeed mediaFeed = new MediaFeed();
	TagMediaFeed tagFeed = new TagMediaFeed();
	
	
	public UserInfo userInfo = new UserInfo();
	
	public String getKey() { 
		return instagramKey;
	}
	public String getSecret() {
		return instagramSecret;
	}
	public Token getToken() {
		return instagramToken;
	}
	
	public InstagramRetriever(String secret, String token, int maxResults,int maxRequests) {
		Token instagramToken = new Token(token,secret); 
		this.instagram = new Instagram(instagramToken);
		this.results_threshold = maxResults;
		this.request_threshold = maxRequests;
	}
	
	@Override
	public List<Item> retrieveUserFeeds(SourceFeed feed) {
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		
		int numberOfRequests = 0;
		
		boolean isFinished = false;
	
		Source source = feed.getSource();
		String uName = source.getName();
		
		if(uName == null){
			logger.info("#Instagram : No source feed");
			return null;
		}
			
		logger.info("#Instagram : Retrieving User Feed : "+uName);
		
		List<UserFeedData>revUsers = null; 
		try{
			UserFeed userf= instagram.searchUser(uName);
			revUsers = userf.getUserList();
		}
		catch(InstagramException e){
			logger.error("#Instagram Exception : "+e);
			return items;
		}
		
		//logger.info("#Instagram : "+revUsers.size()+" relevant users found");
		
		for(UserFeedData revUser : revUsers){

			try{
				mediaFeed = instagram.getRecentMediaFeed(revUser.getId(),-1,null,null,null,null);
				if(mediaFeed != null){
					//logger.info("#Instagram : "+mediaFeed.getData().size()+" photos of "+it+" relevant user found to retrieve");
					
					for(MediaFeedData mfeed : mediaFeed.getData()){
						int createdTime = Integer.parseInt(mfeed.getCreatedTime());
						Date publicationDate = new Date((long) createdTime * 1000);
						
						if(lastItemDate.after(publicationDate) || items.size()>results_threshold 
								|| numberOfRequests>request_threshold){
    						isFinished = true;
							break;
    					}
						if(mfeed != null && mfeed.getId() != null){
							InstagramItem instagramUpdate = new InstagramItem(mfeed,feed);
							
							items.add(instagramUpdate);	
						}
						
					}
				}
				
			}
			catch(InstagramException e){
				logger.error("#Instagram Exception : "+e);
				return items;
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				logger.error("#Instagram Exception : "+e1);
				e1.printStackTrace();
				return items;
			}
		}	
		
		//logger.info("#Instagram : Done retrieving for this session");
		logger.info("#Instagram : Handler fetched " + items.size() + " photos from " + uName + 
				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
	}
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		
		boolean isFinished = false;
		
		int numberOfRequests = 0;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keywords == null){
			logger.info("#Instagram : No keywords feed");
			return items;
		}
		
		String tags = "";
		if(keyword != null){
			for(String key : keyword.getName().split(" ")) 
				if(key.length()>1)
					tags += key.toLowerCase();
		}
		else if(keywords != null){
			for(Keyword key : keywords){
				String [] words = key.getName().split(" ");
				for(String word : words)
					if(!tags.contains(word) && word.length()>1)
						tags += word.toLowerCase();
			}
		}
		tags = tags.replaceAll(" ", "");
		//logger.info("#Instagram : Retrieving Keywords Feed : "+tags);
		
		if(tags.equals(""))
			return items;
		
		//retrieve first page
		try{
			tagFeed = instagram.getRecentMediaTags(tags);
			numberOfRequests++;
		}
		catch(InstagramException e){
			/*logger.error("#First Instagram Exception : "+e);*/
			
			return items;
		}
		
		Pagination pagination = tagFeed.getPagination();
		if(tagFeed.getData() != null){
			
			for(MediaFeedData mfeed : tagFeed.getData()) {
				int createdTime = Integer.parseInt(mfeed.getCreatedTime());
				Date publicationDate = new Date((long) createdTime * 1000);
				
				if(publicationDate.before(lastItemDate) || items.size()>results_threshold || numberOfRequests>request_threshold){
					isFinished = true;
					break;
				}
				
				if(mfeed != null && mfeed.getId() != null){
					InstagramItem instagramItem;
					try {
						instagramItem = new InstagramItem(mfeed,feed);
					} catch (MalformedURLException e) {
						
						return items;
					}
					instagramItem.setDyscoId(feed.getDyscoId());
					
					items.add(instagramItem);
					
				}
		
			}
				
			//continue retrieving other pages
			if(!isFinished){
				while(pagination.hasNextPage()){
					
					try{
						if(numberOfRequests>=request_threshold)
							break;
						
						tagFeed = instagram.getTagMediaInfoNextPage(pagination);
						numberOfRequests++;
						pagination = tagFeed.getPagination();
						if(tagFeed.getData() != null){
							
							for(MediaFeedData mfeed : tagFeed.getData()) {
								int createdTime = Integer.parseInt(mfeed.getCreatedTime());
								Date publicationDate = new Date((long) createdTime * 1000);
								if(publicationDate.before(lastItemDate) || items.size()>results_threshold
										|| numberOfRequests>request_threshold){
									isFinished = true;
									break;
								}
								
								if(mfeed != null && mfeed.getId() != null){
									InstagramItem instagramItem = new InstagramItem(mfeed,feed);
									instagramItem.setDyscoId(feed.getDyscoId());
									
									items.add(instagramItem);
								}
	
							}
							if(isFinished)
								break;
						}
					}catch(InstagramException e){
						
						logger.error("#Second Instagram Exception : "+e);
						return items;
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						/*logger.error("#Second Instagram Exception : "+e1);
						e1.printStackTrace();*/
						return items;
					}

				}
			}
			
		}

//		logger.info("#Instagram : Done retrieving for this session");
//		logger.info("#Instagram : Handler fetched " + items.size() + " posts from " + tags + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		feed.setTotalNumberOfItems(items.size());
		return items;
	}
	@Override
	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		Date currentDate = new Date(System.currentTimeMillis());
		DateUtil dateUtil = new DateUtil();
		
		int it = 0 ;
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		Location loc = feed.getLocation();
		
    	if(loc == null){ 
    		logger.info("#Instagram : No Location feed");
    		return null;
    	}
		
		List<org.jinstagram.entity.common.Location> locations = null;
		
    	double latitude = loc.getLatitude();
    	double longtitude = loc.getLongitude();

    	logger.info("#Instagram : Retrieving Location Feed : ("+latitude+","+longtitude+")");
    	
    	try{
    		LocationSearchFeed locs = instagram.searchLocation(latitude , longtitude,5000);
    		locations = locs.getLocationList();
    	}
    	catch(InstagramException e){
    		logger.error("#Instagram Exception : "+e);
    		return items;
    	}
    	
    	for (org.jinstagram.entity.common.Location location : locations){
    		
    		Date upDate = currentDate;
    		Date downDate = dateUtil.addDays(upDate, -1);
    		
    		logger.info("#Instagram : Retrieving for location : "+location.getName());	
    		
    		while(downDate.after(lastItemDate) || downDate.equals(lastItemDate)){
    	
    			it++;
    			try{
        			mediaFeed = instagram.getRecentMediaByLocation(location.getId(),0,0,upDate,downDate);
        			numberOfRequests++;
        			if(mediaFeed != null){
        				logger.info("#Instagram : Retrieving page "+it+" that contains "+mediaFeed.getData().size()+" posts");	
            			
                		for(MediaFeedData mfeed : mediaFeed.getData()){
        					int createdTime = Integer.parseInt(mfeed.getCreatedTime());
        					Date publicationDate = new Date((long) createdTime * 1000);
        					if(lastItemDate.after(publicationDate) || items.size()>results_threshold 
        							|| numberOfRequests>request_threshold){
        						isFinished = true;
								break;
        					}
        					
        					if((mfeed != null && mfeed.getId() != null)){
        						InstagramItem instagramUpdate = new InstagramItem(mfeed,feed);
        						items.add(instagramUpdate);
        					}
        					
        				}
        			}
        		}
        		catch(InstagramException e){
        			logger.error("#Instagram Exception : "+e);
        			return items;
        		} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
        			logger.error("#Instagram Exception : "+e1);
					e1.printStackTrace();
				}
    			
    			if(isFinished)
    				break;
    				
    			upDate = downDate;
    			downDate = dateUtil.addDays(upDate, -1);
    		}
    		
    	}
    	
//    	logger.info("#Instagram : Done retrieving for this session");
//		logger.info("#Instagram : Handler fetched " + items.size() + " posts from (" + latitude+","+longtitude+")" + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
    	return items;
    }
	
	public void printItemData(InstagramItem item){
		
		System.out.println("------ Print Media Feed Data ------");
		
		System.out.println("------ Media Feed Item #"+item.getId()+" ------");
		
		System.out.println("Source : "+item.getSource());
		System.out.println("Title : "+item.getTitle());
		System.out.println("Tags : ");
		if(item.getTags()!=null)
			for(int i=0;i<item.getTags().length;i++){
				System.out.println("# "+i+" "+item.getTags()[i]);
			}
		System.out.println("Author : "+item.getStreamUser().getUsername());
		System.out.println("Publication Time : "+item.getPublicationTime());
		System.out.println("Comments : ");
		if(item.getComments()!=null)
			for(int i=0;i<item.getComments().length;i++){
				System.out.println("# "+i+" "+item.getComments()[i]);
			}
		if(item.getLocation()!=null)
			System.out.println("Location : "+item.getLocation().getLatitude()+","+item.getLocation().getLongitude());
		if(item.getPopularity()!=null){
			System.out.println("Popularity on Comments : "+item.getPopularity().get("comments"));
			System.out.println("Popularity on Likes : "+item.getPopularity().get("likes"));
		}
		System.out.println("URL : "+item.getMediaLinks().get(0).getMediaLink());
		
	
	}
	
	public List<Item> retrieve (Feed feed) {
	
		switch(feed.getFeedtype()){
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				
				return retrieveUserFeeds(userFeed);
				
			case KEYWORDS:
				KeywordsFeed keyFeed = (KeywordsFeed) feed;
				
				return retrieveKeywordsFeeds(keyFeed);
			
			case LOCATION:
				LocationFeed locationFeed = (LocationFeed) feed;
				
				return retrieveLocationFeeds(locationFeed);
				
		}
		 
		return null;
	}

	public class DateUtil
	{
	    public Date addDays(Date date, int days)
	    {
	        Calendar cal = Calendar.getInstance();
	        cal.setTime(date);
	        cal.add(Calendar.DATE, days); //minus number decrements the days
	        return cal.getTime();
	    }
	}
	
}
