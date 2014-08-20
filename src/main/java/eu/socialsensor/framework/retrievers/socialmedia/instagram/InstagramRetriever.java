package eu.socialsensor.framework.retrievers.socialmedia.instagram;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jinstagram.Instagram;
import org.jinstagram.InstagramOembed;
import org.jinstagram.exceptions.InstagramException;
import org.jinstagram.entity.common.Caption;
import org.jinstagram.entity.common.ImageData;
import org.jinstagram.entity.common.Images;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.common.User;
import org.jinstagram.entity.locations.LocationSearchFeed;
import org.jinstagram.entity.media.MediaInfoFeed;
import org.jinstagram.entity.oembed.OembedInformation;
import org.jinstagram.entity.tags.TagMediaFeed;
import org.jinstagram.entity.users.basicinfo.UserInfo;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.entity.users.feed.UserFeed;
import org.jinstagram.entity.users.feed.UserFeedData;
import org.jinstagram.auth.model.Token;

import eu.socialsensor.framework.abstractions.socialmedia.instagram.InstagramItem;
import eu.socialsensor.framework.abstractions.socialmedia.instagram.InstagramStreamUser;
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
import eu.socialsensor.framework.streams.StreamConfiguration;
import eu.socialsensor.framework.streams.socialmedia.instagram.InstagramStream;

/**
 * Class responsible for retrieving Instagram content based on keywords or instagram users or locations
 * The retrieval process takes place through Instagram API
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class InstagramRetriever implements SocialMediaRetriever {
	
	private Logger logger = Logger.getLogger(InstagramRetriever.class);
	
	StreamConfiguration instagramConfStorage;
	
	private Instagram instagram = null;
	private InstagramStream igStream;

	private int maxResults;
	private int maxRequests;
	
	private long maxRunningTime;
	
	private MediaFeed mediaFeed = new MediaFeed();
	private TagMediaFeed tagFeed = new TagMediaFeed();

	private InstagramOembed instagramOembed;
	
	public InstagramRetriever(String clientId) {
		this.instagram = new Instagram(clientId);
		this.instagramOembed = new InstagramOembed();
	}
	
	public InstagramRetriever(String secret, String token) {
		Token instagramToken = new Token(token, secret); 
		this.instagram = new Instagram(instagramToken);
		this.instagramOembed = new InstagramOembed();
	}
	
	public InstagramRetriever(String secret, String token, int maxResults,int maxRequests,long maxRunningTime,InstagramStream igStream) {
		this(secret, token);
		
		this.maxResults = maxResults;
		this.maxRequests = maxRequests;
		this.maxRunningTime = maxRunningTime;
		
		this.igStream = igStream;
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed) {
		Integer totalRetrievedItems = 0;
		
		Date lastItemDate = feed.getDateToRetrieve();
		String label = feed.getLabel();
		
		int numberOfRequests = 0;
	
		Source source = feed.getSource();
		String uName = source.getName();
		
		if(uName == null){
			logger.info("#Instagram : No source feed");
			return totalRetrievedItems;
		}
			
		logger.info("#Instagram : Retrieving User Feed : "+uName);
		
		List<UserFeedData>revUsers = null; 
		try{
			UserFeed userf= instagram.searchUser(uName);
			revUsers = userf.getUserList();
		}
		catch(InstagramException e){
			logger.error("#Instagram Exception : "+e);
			return totalRetrievedItems;
		}
		
		for(UserFeedData revUser : revUsers){

			try{
				mediaFeed = instagram.getRecentMediaFeed(revUser.getId(),-1,null,null,null,null);
				if(mediaFeed != null){
					
					for(MediaFeedData mfeed : mediaFeed.getData()){
						int createdTime = Integer.parseInt(mfeed.getCreatedTime());
						Date publicationDate = new Date((long) createdTime * 1000);
						
						if(lastItemDate.after(publicationDate) || totalRetrievedItems>maxResults 
								|| numberOfRequests>maxRequests){
							break;
    					}
						if(mfeed != null && mfeed.getId() != null){
							InstagramItem instagramUpdate = new InstagramItem(mfeed);
							instagramUpdate.setList(label);
							
							if(igStream != null) {
								igStream.store(instagramUpdate);
							}
							totalRetrievedItems++;
						}
					}
				}
				
			}
			catch(InstagramException e){
				logger.error("#Instagram Exception:", e);
				return totalRetrievedItems;
			} catch (MalformedURLException e) {
				logger.error("#Instagram Exception: ", e);
				return totalRetrievedItems;
			}
		}	
		// The next request will retrieve only items of the last day
		Date dateToRetrieve = new Date(System.currentTimeMillis() - (24*3600*1000));
		feed.setDateToRetrieve(dateToRetrieve);
		
		logger.info("#Instagram : Handler fetched " + totalRetrievedItems + " photos from " + uName + 
				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return totalRetrievedItems;
	}
	
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed){
		Integer totalRetrievedItems = 0;
		
		Date lastItemDate = feed.getDateToRetrieve();
		String label = feed.getLabel();
		
		boolean isFinished = false;
		
		int numberOfRequests = 0;
		
		long currRunningTime = System.currentTimeMillis();
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keywords == null){
			logger.info("#Instagram : No keywords feed");
			return totalRetrievedItems;
		}
		
		String tags = "";
		if(keyword != null) {
			for(String key : keyword.getName().split(" ")) {
				if(key.length()>1)
					tags += key.toLowerCase();
			}
		}
		else if(keywords != null) {
			for(Keyword key : keywords) {
				String [] words = key.getName().split(" ");
				for(String word : words) {
					if(!tags.contains(word) && word.length()>1)
						tags += word.toLowerCase();
				}
			}
		}
		tags = tags.replaceAll(" ", "");
	
		if(tags.equals(""))
			return totalRetrievedItems;
		
		//retrieve first page
		try{
			tagFeed = instagram.getRecentMediaTags(tags);
			numberOfRequests++;
		}
		catch(InstagramException e){	
			return totalRetrievedItems;
		}
		
		Pagination pagination = tagFeed.getPagination();
		if(tagFeed.getData() != null){
			
			for(MediaFeedData mfeed : tagFeed.getData()) {
				int createdTime = Integer.parseInt(mfeed.getCreatedTime());
				Date publicationDate = new Date((long) createdTime * 1000);
				
				if(publicationDate.before(lastItemDate)){
					logger.info("Since date reached: " + lastItemDate);
					isFinished = true;
					break;
				}
				if(totalRetrievedItems > maxResults) {
					logger.info("totalRetrievedItems: " + lastItemDate + " > " + maxResults);
					isFinished = true;
					break;
				}
				if(numberOfRequests > maxRequests) {
					logger.info("numberOfRequests: " + numberOfRequests + " > " + maxRequests);
					isFinished = true;
					break;
				}
				if((System.currentTimeMillis() - currRunningTime) > maxRunningTime) {
					logger.info("maxRunningTime reached.");
					isFinished = true;
					break;
				}
				
				if(mfeed != null && mfeed.getId() != null){
					InstagramItem instagramItem;
					try {
						instagramItem = new InstagramItem(mfeed);
						instagramItem.setList(label);
						
					} catch (MalformedURLException e) {
						logger.error("Instagram retriever exception: " + e.getMessage());
						return totalRetrievedItems;
					}

					if(igStream != null) {
						igStream.store(instagramItem);
					}
					totalRetrievedItems++;
					
				}
			}
				
			//continue retrieving other pages
			if(!isFinished) {
				while(pagination.hasNextPage()){
					
					try {
						if(numberOfRequests>=maxRequests)
							break;
						
						tagFeed = instagram.getTagMediaInfoNextPage(pagination);
						numberOfRequests++;
						pagination = tagFeed.getPagination();
						if(tagFeed.getData() != null){
							
							for(MediaFeedData mfeed : tagFeed.getData()) {
								int createdTime = Integer.parseInt(mfeed.getCreatedTime());
								Date publicationDate = new Date((long) createdTime * 1000);
								if(publicationDate.before(lastItemDate) || totalRetrievedItems>maxResults
										|| numberOfRequests>maxRequests){
									isFinished = true;
									break;
								}
								
								if(mfeed != null && mfeed.getId() != null){
									InstagramItem instagramItem = new InstagramItem(mfeed);
									instagramItem.setList(label);
									
									if(igStream != null) {
										igStream.store(instagramItem);
									}
									
									totalRetrievedItems++;
								}
	
							}
							if(isFinished)
								break;
						}
					}
					catch(InstagramException e) {	
						logger.error("#Second Instagram Exception: " + e.getMessage());
						return totalRetrievedItems;
					} catch (MalformedURLException e1) {
						return totalRetrievedItems;
					}

				}
			}
			
		}

		logger.info("#Instagram : Handler fetched " + totalRetrievedItems + " posts from " + tags + 
				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		// The next request will retrieve only items of the last day
		Date dateToRetrieve = new Date(System.currentTimeMillis() - (24*3600*1000));
		feed.setDateToRetrieve(dateToRetrieve);
		
		return totalRetrievedItems;
	}
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed) {
		Integer totalRetrievedItems = 0;
		
		Date lastItemDate = feed.getDateToRetrieve();
		Date currentDate = new Date(System.currentTimeMillis());
		DateUtil dateUtil = new DateUtil();
		
		int it = 0 ;
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		Location loc = feed.getLocation();
		
    	if(loc == null){ 
    		logger.info("#Instagram : No Location feed");
    		return totalRetrievedItems;
    	}
		
		List<org.jinstagram.entity.common.Location> locations = null;
		
    	double latitude = loc.getLatitude();
    	double longtitude = loc.getLongitude();
    	
    	try{
    		LocationSearchFeed locs = instagram.searchLocation(latitude , longtitude,5000);
    		locations = locs.getLocationList();
    	}
    	catch(InstagramException e){
    		logger.error("#Instagram Exception : "+e);
    		return totalRetrievedItems;
    	}
    	
    	for (org.jinstagram.entity.common.Location location : locations){
    		
    		Date upDate = currentDate;
    		Date downDate = dateUtil.addDays(upDate, -1);
    		
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
        					if(lastItemDate.after(publicationDate) || totalRetrievedItems>maxResults 
        							|| numberOfRequests>maxRequests){
        						isFinished = true;
								break;
        					}
        					
        					if((mfeed != null && mfeed.getId() != null)){
        						InstagramItem instagramUpdate = new InstagramItem(mfeed);
        						
        						if(igStream != null) {
        							igStream.store(instagramUpdate);
        						}
        						
        						totalRetrievedItems++;
        					}
        					
        				}
        			}
        		}
        		catch(InstagramException e){
        			
        			return totalRetrievedItems;
        		} catch (MalformedURLException e1) {
        			return totalRetrievedItems;
					
				}
    			
    			if(isFinished)
    				break;
    				
    			upDate = downDate;
    			downDate = dateUtil.addDays(upDate, -1);
    		}
    		
    	}
    	
    	//logger.info("#Instagram : Done retrieving for this session");
		//logger.info("#Instagram : Handler fetched " + totalRetrievedItems + " posts from (" + latitude+","+longtitude+")" + 
		//		" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		// The next request will retrieve only items of the last day
		Date dateToRetrieve = new Date(System.currentTimeMillis() - (24*3600*1000));
		feed.setDateToRetrieve(dateToRetrieve);
		
    	return totalRetrievedItems;
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
				if(!userFeed.getSource().getNetwork().equals("Instagram"))
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
		if(instagram != null){
			instagram = null;
		}
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

	@Override
	public MediaItem getMediaItem(String shortId) {
		try {
			String id = getMediaId("http://instagram.com/p/"+shortId);
			if(id == null)
				return null;
			
			MediaInfoFeed mediaInfo = instagram.getMediaInfo(id);
			if(mediaInfo != null) {
				MediaFeedData mediaData = mediaInfo.getData();
				Images images = mediaData.getImages();
				
				ImageData standardUrl = images.getStandardResolution();
				String url = standardUrl.getImageUrl();
				
				MediaItem mediaItem = new MediaItem(new URL(url));
				
				ImageData thumb = images.getThumbnail();
				String thumbnail = thumb.getImageUrl();
				
				String mediaId = "Instagram#" + mediaData.getId();
				List<String> tags = mediaData.getTags();
				
				String title = null;
				Caption caption = mediaData.getCaption();
				if(caption !=  null) {
					title = caption.getText();
				}
				
				Long publicationTime = new Long(1000*Long.parseLong(mediaData.getCreatedTime()));
				
				//id
				mediaItem.setId(mediaId);
				//SocialNetwork Name
				mediaItem.setStreamId("Instagram");
				//Reference
				mediaItem.setRef(id);
				//Type 
				mediaItem.setType("image");
				//Time of publication
				mediaItem.setPublicationTime(publicationTime);
				//PageUrl
				mediaItem.setPageUrl(url);
				//Thumbnail
				mediaItem.setThumbnail(thumbnail);
				//Title
				mediaItem.setTitle(title);
				//Tags
				mediaItem.setTags(tags.toArray(new String[tags.size()]));
				//Popularity
				mediaItem.setLikes(new Long(mediaData.getLikes().getCount()));
				mediaItem.setComments(new Long(mediaData.getComments().getCount()));
				//Location
				org.jinstagram.entity.common.Location geoLocation = mediaData.getLocation();
				if(geoLocation != null) {
					double latitude = geoLocation.getLatitude();
					double longitude = geoLocation.getLongitude();
					
					Location location = new Location(latitude, longitude);
					location.setName(geoLocation.getName());
					mediaItem.setLocation(location);
				}
				//Size
				ImageData standard = images.getStandardResolution();
				if(standard!=null) {
					int height = standard.getImageHeight();
					int width = standard.getImageWidth();
					mediaItem.setSize(width, height);
				}
				
				User user = mediaData.getUser();
				if(user != null) {
					StreamUser streamUser = new InstagramStreamUser(user);
					mediaItem.setUser(streamUser);
					mediaItem.setUserId(streamUser.getId());
				}
				
				return mediaItem;
			}
		} catch (Exception e) {
			logger.error(e);
		} 
		
		return null;
	}
	
	@Override
	public StreamUser getStreamUser(String uid) {
		try {
			long userId = Long.parseLong(uid);
			UserInfo userInfo = instagram.getUserInfo(userId);
			
			StreamUser user = new InstagramStreamUser(userInfo.getData());
			return user;
		}
		catch(Exception e) {
			logger.error("Exception for user " + uid, e);
			return null;
		}
	}
	
	private String getMediaId(String url) {
		try {
			OembedInformation info = instagramOembed.getOembedInformation(url);
			if(info == null) 
				return null;
			return info.getMediaId();
		} catch (Exception e) {
			logger.error("Failed to get id for " + url, e);
		}
		return null;
	}

}
