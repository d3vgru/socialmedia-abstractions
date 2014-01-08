package eu.socialsensor.framework.retrievers.flickr;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Extras;

import eu.socialsensor.framework.abstractions.flickr.FlickrItem;
import eu.socialsensor.framework.abstractions.flickr.FlickrStreamUser;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;

/**
 * The retriever that implements the Flickr wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FlickrRetriever implements Retriever {

	private Logger logger = Logger.getLogger(FlickrRetriever.class);

	private static final int PER_PAGE = 50;
	
	private String flickrKey;
	private String flickrSecret;

	private int maxRequests = 10;
	private Integer maxResults= 10000;
	
	private Flickr flickr;

	private HashMap<String, FlickrStreamUser> userMap;

	private PeopleInterface peopleInteface;



	public String getKey() { 
		return flickrKey;
	}
	public String getSecret() {
		return flickrSecret;
	}

	public FlickrRetriever(String flickrKey, String flickrSecret,Integer maxResults,Integer maxRequests) {
		
		this.flickrKey = flickrKey;
		this.flickrSecret = flickrSecret;
		
		this.maxRequests = maxRequests;
		this.maxResults = maxResults;
		
		userMap = new HashMap<String, FlickrStreamUser>();
		
		this.flickr = new Flickr(flickrKey, flickrSecret, new REST());
		this.peopleInteface = flickr.getPeopleInterface();
		
	}

	
	@Override
	public List<Item> retrieveUserFeeds(SourceFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		
		int page=1, pages=1; //pagination
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		//Here we search the user by the userId given (NSID) - however we can get NSID via flickrAPI given user's username
		Source source = feed.getSource();
		String userID = source.getId();
		
		if(userID == null){
			logger.info("#Flickr : No source feed");
			return null;
		}
		

		
		//logger.info("#Flickr : Done retrieving for this session");
//		logger.info("#Flickr : Handler fetched " + items.size() + " photos from " + userID + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
	}
	
	
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed) {
		List<Item> items = new ArrayList<Item>();
		
		Date lastItemDate = feed.getLastItemDate();
		
		int page=1, pages=1;
		int numberOfRequests = 0;
		int numberOfResults = 0;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null) {
			logger.info("#Flickr : No keywords feed");
			return items;
		}
		
		List<String> tags = new ArrayList<String>();
		String text = "";
		
		if(keyword != null) {
			for(String key : keyword.getName().split(" ")){
				if(key.length()>1){
					tags.add(key.toLowerCase());
					text+=key.toLowerCase()+" ";
				}
			}	
		}
		else if(keywords != null){
			for(Keyword key : keywords){
				String [] words = key.getName().split(" ");
				for(String word : words)
					if(!tags.contains(word) && word.length()>1){
						tags.add(word);
						text+=word+" ";
					}
						
			}
		}
		if(tags.equals(""))
			return items;
		
		PhotosInterface photosInteface = flickr.getPhotosInterface();
		SearchParameters params = new SearchParameters();
		params.setText(text);
		params.setMinUploadDate(lastItemDate);
		params.setExtras(Extras.ALL_EXTRAS);
		try {
			while(page<=pages && numberOfRequests<=maxRequests && numberOfResults<=maxResults) {
				
				PhotoList<Photo> photos = photosInteface.search(params , PER_PAGE, page++);
				pages = photos.getPages();
				numberOfResults += photos.size();
				
				if(photos.isEmpty()) {
					break;
				}
				
				for(Photo photo : photos) {
				
					String userid = photo.getOwner().getId();
					FlickrStreamUser streamUser = userMap.get(userid);
					if(streamUser == null) {
						User user = retrieveUser(userid);
						streamUser = new FlickrStreamUser(user);
					
						userMap.put(userid, streamUser);
					}
					
					FlickrItem flickrUpdate = new FlickrItem(photo, streamUser, feed);
				
					items.add(flickrUpdate);
				}
			}
			
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		
		return items;
	}

	@Override
	public List<Item> retrieveLocationFeeds(LocationFeed feed){
		List<Item> items = new ArrayList<Item>();
//		
//		Date lastItemDate = feed.getLastItemDate();
//		
//		Double[][] bbox = feed.getLocation().getbbox();
//		
//		int page=1, pages=1;
//		
//		boolean isFinished = false;
//		
//		//logger.info("#Flickr : Retrieving Location Feed ");
//		
//		Response response = null;
//		
//		while(true){
//			List<Parameter> parameters = getSearchParameters(lastItemDate);
//			parameters.add(new Parameter("bbox", bbox));
//			parameters.add(new Parameter("page", page));	
//			String signature = AuthUtilities.getSignature(flickrSecret, parameters);
//			parameters.add(new Parameter("api_sig",signature));
//			
//			try {
//				response = flickrTransport.get(flickrTransport.getPath(), parameters);
//				
//			} catch (Exception e) {
//				logger.error("#Flickr Exception: "+e);
//				return items;
//			}
//			if (response.isError()) {
//				logger.error("#Flickr : Wrong response "+response.getErrorCode());
//				return items;
//			}
//			
//			Element photosElement = response.getPayload();
//			NodeList photoNodes = photosElement.getElementsByTagName("photo");
//			
//			//logger.info("#Flickr : Retrieving page "+page+" that contains "+photoNodes.getLength()+" photos");
//			
//			for (int i = 0; i < photoNodes.getLength(); i++) {
//				Element photoElement = (Element) photoNodes.item(i);
//				Photo photo = PhotoUtils.createPhoto(photoElement);
//			
//				FlickrItem flickrUpdate = new FlickrItem(photo,feed);
//				
//				items.add(flickrUpdate);
//				
//				if(items.size()>maxResults){
//					isFinished = true;
//					break;
//				}
//				
//			}
//			
//			pages = Integer.parseInt(photosElement.getAttribute("pages"));
//			
//			if(page == pages || pages==0 || isFinished) 
//				break;
//			
//			page++;
//		}
//		
//		//logger.info("#Flickr : Done retrieving for this session");
//		logger.info("#Flickr : Handler fetched " + items.size() + " photos "+ 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
//		
		return items;
    }
	
	@Override
	public List<Item> retrieve (Feed feed) {
		
		switch(feed.getFeedtype()) {
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
	
	@Override
	public void stop() {
		
	}
	
	public User retrieveUser(String userid) {
		User user = null;
		try {
			user = peopleInteface.getInfo(userid);
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return user;
	}
	
}
