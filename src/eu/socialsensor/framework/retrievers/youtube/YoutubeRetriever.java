package eu.socialsensor.framework.retrievers.youtube;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;



import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;

import eu.socialsensor.framework.abstractions.youtube.YoutubeItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;
/**
 * The retriever that implements the Youtube wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class YoutubeRetriever implements Retriever {

	private final String activityFeedUserUrlPrefix = "http://gdata.youtube.com/feeds/api/users/";
	private final String activityFeedVideoUrlPrefix = "http://gdata.youtube.com/feeds/api/videos";
	private final String uploadsActivityFeedUrlSuffix = "/uploads";
	
	private Logger logger = Logger.getLogger(YoutubeRetriever.class);
	
	private YouTubeService service;
	
	private int results_threshold;
	private int request_threshold;
	
	public YoutubeRetriever(String clientId, String developerKey,Integer maxResults,Integer maxRequests) {	
	
		this.service = new YouTubeService(clientId, developerKey);
		this.results_threshold = maxResults;
		this.request_threshold = maxRequests;
	}

	public List<Item> retrieveUserFeeds(SourceFeed feed){
		List<Item> items = new ArrayList<Item>();
		Date lastItemDate = feed.getLastItemDate();
		
		boolean isFinished = false;
		
		Source source = feed.getSource();
		String uName = source.getName();
		
		int numberOfRequests = 0;
		
		if(uName == null){
			logger.info("#YouTube : No source feed");
			return null;
		}
				
		logger.info("#YouTube : Retrieving User Feed : "+uName);
		
		URL channelUrl = null;
		try {
			channelUrl = getChannelUrl(uName);
		} catch (MalformedURLException e) {
			logger.error("#YouTube Exception : "+e);
			return null;
		}
		
		while(channelUrl != null) {
			
			try {
				VideoFeed videoFeed = service.getFeed(channelUrl, VideoFeed.class);
				
				numberOfRequests ++ ;
				
				for(VideoEntry  video : videoFeed.getEntries()) {
					
					com.google.gdata.data.DateTime publishedTime = video.getPublished();
					DateTime publishedDateTime = new DateTime(publishedTime.toString());
					Date publicationDate = publishedDateTime.toDate();
					
					if(publicationDate.after(lastItemDate)){
						YoutubeItem videoItem = new YoutubeItem(video,feed);
						
						items.add(videoItem);
					}
					
					if(items.size()>results_threshold || numberOfRequests > request_threshold){
						isFinished = true;
						break;
					}
						
				}
				
				if(isFinished)
					break;
				
				Link nextLink = videoFeed.getNextLink();
				channelUrl = nextLink==null ? null : new URL(nextLink.getHref());
				
			} catch (Exception e) {
				logger.error("#YouTube Exception : "+e);
				return items;
			} 
		
		}
		
		//logger.info("#YouTube : Done retrieving for this session");
		logger.info("#YouTube : Handler fetched " + items.size() + " videos from " + uName + 
				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
	}
	
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		
		int startIndex = 1;
		int maxResults = 25;
		int currResults = 0;
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword != null){
			logger.info("#YouTube : No keywords feed");
			return items;
		}
	
		String tags = "";
		
		if(keyword != null){
			for(String key : keyword.getName().split(" ")) 
				if(key.length()>1)
					tags += key.toLowerCase()+" ";
		}
		else if(keywords != null){
			for(Keyword key : keywords){
				String [] words = key.getName().split(" ");
				for(String word : words)
					if(!tags.contains(word) && word.length()>1)
						tags += word.toLowerCase()+" ";
			}
		}
		//one call - 25 results
		if(tags.equals(""))
			return items;
		//logger.info("#YouTube : Retrieving Keywords Feed : "+tags);
		YouTubeQuery query;
		try {
			query = new YouTubeQuery(new URL(activityFeedVideoUrlPrefix));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			logger.error("#YouTube Exception : "+e1);
			e1.printStackTrace();
			return items;
		}
		
		query.setOrderBy(YouTubeQuery.OrderBy.PUBLISHED);
		query.setFullTextQuery(tags);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		query.setMaxResults(maxResults);
		
		VideoFeed videoFeed = new VideoFeed();
		
		while(true){
		
			try{
				query.setStartIndex(startIndex);
				videoFeed = service.query(query, VideoFeed.class);
				
				numberOfRequests++;
				
				currResults = videoFeed.getEntries().size();
				startIndex +=currResults;
				
				for(VideoEntry  video : videoFeed.getEntries()) {
					com.google.gdata.data.DateTime publishedTime = video.getPublished();
					DateTime publishedDateTime = new DateTime(publishedTime.toString());
					Date publicationDate = publishedDateTime.toDate();
					
					if(publicationDate.after(lastItemDate)){
						YoutubeItem videoItem = new YoutubeItem(video,feed);
						videoItem.setDyscoId(feed.getDyscoId());
						
						items.add(videoItem);
					}
					
					if(items.size()>results_threshold || numberOfRequests >= request_threshold){
						isFinished = true;
						break;
					}
				}
			
			}
			catch(Exception e){
				logger.error("#YouTube Exception : "+e);
				return items;
			}
			
			if(maxResults>currResults || isFinished)	
				break;
		
		}
	
//		logger.info("#YouTube : Done retrieving for this session");
//		logger.info("#YouTube : Handler fetched " + items.size() + " videos from " + tags + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		feed.setTotalNumberOfItems(items.size());
		return items;
	}
	

	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		//DOES NOT WORK WITH LOCATION
		/*Date lastItemDate = feed.getLastItemDate();
		Date minDate = lastItemDate;
		int startIndex=1;
		int maxResults = 25;
		int currResults = 0;
		Location location = feed.getLocation();
		
		if(location == null) 
			return minDate;
			
		logger.info("Retrieving Location Feed : " + location.getLatitude()+","+location.getLongitude());
		
		YouTubeQuery query = new YouTubeQuery(new URL(activityFeedVideoUrlPrefix));
		
		query.setOrderBy(YouTubeQuery.OrderBy.PUBLISHED);
		GeoRssWhere searchLocation = new GeoRssWhere();
		searchLocation.setGeoLocation(location.getLatitude(), location.getLongitude());
		query.setLocation(searchLocation);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		VideoFeed videoFeed = new VideoFeed();
		query.setMaxResults(maxResults);
		
		while(true){
			boolean isFinished = false;
			try{
				query.setStartIndex(startIndex);
				videoFeed = service.query(query, VideoFeed.class);
				currResults = videoFeed.getEntries().size();
				startIndex +=currResults;
			
				logger.info("Retrieved "+videoFeed.getEntries().size()+" videos");
				
				for(VideoEntry  video : videoFeed.getEntries()) {
				
					com.google.gdata.data.DateTime publishedTime = video.getPublished();
					DateTime publishedDateTime = new DateTime(publishedTime.toString());
					Date publishedDate = publishedDateTime.toDate();
					logger.info("Date of publication :"+publishedDate);
					if(publishedDate.before(lastItemDate)){
						logger.info("Reached maximum date limit");
						isFinished = true;
						break;
					}
					
					YoutubeItem videoItem = new YoutubeItem(video);
					
					handler.update(videoItem);
					
					if(publishedDate.before(minDate)){
						minDate = publishedDate;
					}
				}
				
			}
			catch(Exception e){
				logger.error("Exception retrieving YouTube feeds: "+e.getMessage());
			}

			if(maxResults>currResults || isFinished)
				break;
		}
		
		return minDate;*/
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
				logger.error("#YouTube : Location Feed cannot be retreived from YouTube");
				
				return null;
			
		}
		 
		return null;
	}

	
	private URL getChannelUrl(String channel) throws MalformedURLException {
		StringBuffer urlStr = new StringBuffer(activityFeedUserUrlPrefix);
		urlStr.append(channel).append(uploadsActivityFeedUrlSuffix);
		
		return new URL(urlStr.toString());
	}
		

}
