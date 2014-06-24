package eu.socialsensor.framework.retrievers.socialmedia.flickr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.photos.Extras;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoUtils;

import eu.socialsensor.framework.abstractions.socialmedia.flickr.FlickrItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.MediaItem;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.StreamUser;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.ListFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;
import eu.socialsensor.framework.streams.socialmedia.flickr.FlickrStream;

/**
 * Class responsible for retrieving Flickr content based on keywords,users or location coordinates
 * The retrieval process takes place through Flickr API.
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class FlickrRetriever implements SocialMediaRetriever {

	private Logger logger = Logger.getLogger(FlickrRetriever.class);

	private static final String METHOD = "flickr.photos.search";
	private static final String PER_PAGE = "500";
	private static final String EXTRAS = "description, license, date_upload, " +
			"date_taken, owner_name, last_update, geo, " +
			"tags, machine_tags, o_dims, views, media, url_sq, " +
			"url_t, url_s, url_q, url_m, url_n, url_z, url_c, url_l, url_o";
	
	private String flickrKey;
	private String flickrSecret;
	
	private FlickrStream flStream;
	
	private int maxResults;
	private int maxRequests;
	
	private long maxRunningTime;

	private String flickrHost = "www.flickr.com";
	
	private Transport flickrTransport = null;
	
	public String getKey() { 
		return flickrKey;
	}
	public String getSecret() {
		return flickrSecret;
	}

	public FlickrRetriever(String flickrKey, String flickrSecret,Integer maxResults,Integer maxRequests,long maxRunningTime,FlickrStream flStream) {
		this.flStream = flStream;
		
		this.flickrKey = flickrKey;
		this.flickrSecret = flickrSecret;
		this.maxResults = maxResults;
		this.maxRequests = maxRequests;
		this.maxRunningTime = maxRunningTime;
		
		Flickr.debugStream = false;
		try {
			flickrTransport = new REST(flickrHost);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
	}


	public List<Parameter> getParameters(Date since) {
		List<Parameter> parameters = new ArrayList<Parameter>();
		parameters.add(new Parameter("min_upload_date", since.getTime()/1000));
		parameters.add(new Parameter("method", METHOD));
		parameters.add(new Parameter("per_page", PER_PAGE));
		parameters.add(new Parameter(Extras.KEY_EXTRAS, EXTRAS));
		parameters.add(new Parameter("api_key", flickrKey));
		
		return parameters;
	}
	
	@Override
	public Integer retrieveUserFeeds(SourceFeed feed){
		Integer totalRetrievedItems = 0;
		
		Date dateToRetrieve = feed.getDateToRetrieve();
		
		int page=1, pages=1; //pagination
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		//Here we search the user by the userId given (NSID) - however we can get NSID via flickrAPI given user's username
		Source source = feed.getSource();
		String userID = source.getId();
		
		if(userID == null){
			logger.info("#Flickr : No source feed");
			return totalRetrievedItems;
		}
		
		Response response = null;
		
		while(true){
			List<Parameter> parameters = getParameters(dateToRetrieve);
			parameters.add(new Parameter("user_id", userID));
			parameters.add(new Parameter("page", page));
			String signature = AuthUtilities.getSignature(flickrSecret, parameters);
			parameters.add(new Parameter("api_sig",signature));
			
			try {
				response = flickrTransport.get(flickrTransport.getPath(), parameters);
				numberOfRequests++;
			} catch (Exception e) {
				logger.error("#Flickr Exception: "+e);
				return totalRetrievedItems;
			}
			
			if (response.isError()){
				logger.error("#Flickr : Wrong response "+response.getErrorCode());
				return totalRetrievedItems;
			}
				
			Element photosElement = response.getPayload();
			NodeList photoNodes = photosElement.getElementsByTagName("photo");
			
			for (int i = 0; i < photoNodes.getLength(); i++) {
				Element photoElement = (Element) photoNodes.item(i);
				Photo photo = PhotoUtils.createPhoto(photoElement);
				
				if (photo != null &&  photo.getId() != null){
					FlickrItem flickrUpdate = new FlickrItem(photo);
				
					flStream.store(flickrUpdate);
					totalRetrievedItems++;
				}
				if(totalRetrievedItems>maxResults || numberOfRequests > maxRequests){
					isFinished = true;
					break;
				}
				
			}
			
			pages = Integer.parseInt(photosElement.getAttribute("pages"));
			
			if(page == pages || pages==0 || isFinished) 
				break;
			
			page++;
		}
		
		//logger.info("#Flickr : Done retrieving for this session");
//		logger.info("#Flickr : Handler fetched " + items.size() + " photos from " + userID + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return totalRetrievedItems;
	}
	@Override
	public Integer retrieveKeywordsFeeds(KeywordsFeed feed){
		Integer totalRetrievedItems = 0;
		Date dateToRetrieve = feed.getDateToRetrieve();
		
		int page=1, pages=1;
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		long currRunningTime = System.currentTimeMillis();
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Flickr : No keywords feed");
			return totalRetrievedItems;
		}
		
		List<String> tags = new ArrayList<String>();
		String text = "";
		
		if(keyword != null){
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
			return totalRetrievedItems;
		
		Response response = null;
		
		while(true){
			List<Parameter> parameters = getParameters(dateToRetrieve);
			parameters.add(new Parameter("page", page));	
			if(!tags.isEmpty())
				parameters.add(new Parameter("tags", tags));
			if(!text.equals(""))
				parameters.add(new Parameter("text", text));
			
			parameters.add(new Parameter("tag_mode","all"));
			
			String signature = AuthUtilities.getSignature(flickrSecret, parameters);
			
			parameters.add(new Parameter("api_sig",signature));
			
			try {
				response = flickrTransport.get(flickrTransport.getPath(), parameters);
				
				numberOfRequests++;
			} catch (Exception e) {
				logger.error("#Flickr Exception: "+e);
				return totalRetrievedItems;
			}
			if (response.isError()) {
				logger.error("#Flickr : Wrong response "+response.getErrorCode());
				return totalRetrievedItems;
			}
			
			Element photosElement = response.getPayload();
			NodeList photoNodes = photosElement.getElementsByTagName("photo");
			
			for (int i = 0; i < photoNodes.getLength(); i++) {
				Element photoElement = (Element) photoNodes.item(i);
				Photo photo = PhotoUtils.createPhoto(photoElement);
			
				FlickrItem flickrUpdate = new FlickrItem(photo);
				flStream.store(flickrUpdate);
				totalRetrievedItems++;
				
				
				if(totalRetrievedItems>maxResults || numberOfRequests >= maxRequests || (System.currentTimeMillis() - currRunningTime) > maxRunningTime){
					isFinished = true;
					break;
				}
				
			}
			
			pages = Integer.parseInt(photosElement.getAttribute("pages"));
			
			if(page == pages || pages==0 || isFinished) 
				break;
			
			page++;
		}
			
//		logger.info("#Flickr : Done retrieving for this session");
//		logger.info("#Flickr : Handler fetched " + items.size() + " photos from " + text + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return totalRetrievedItems;
	}
	@Override
	public Integer retrieveLocationFeeds(LocationFeed feed){
		Integer totalRetrievedItems = 0;
		
		Date dateToRetrieve = feed.getDateToRetrieve();
		
		Double[][] bbox = feed.getLocation().getbbox();
		
		if(bbox == null || bbox.length==0)
			return 0;
		
		int page=1, pages=1;
		
		boolean isFinished = false;
		
		Response response = null;
		
		while(true){
			List<Parameter> parameters = getParameters(dateToRetrieve);
			parameters.add(new Parameter("bbox", bbox));
			parameters.add(new Parameter("page", page));	
			String signature = AuthUtilities.getSignature(flickrSecret, parameters);
			parameters.add(new Parameter("api_sig",signature));
			
			try {
				response = flickrTransport.get(flickrTransport.getPath(), parameters);
				
			} catch (Exception e) {
				logger.error("#Flickr Exception: "+e);
				return totalRetrievedItems;
			}
			if (response.isError()) {
				logger.error("#Flickr : Wrong response "+response.getErrorCode());
				return totalRetrievedItems;
			}
			
			Element photosElement = response.getPayload();
			NodeList photoNodes = photosElement.getElementsByTagName("photo");
			
			for (int i = 0; i < photoNodes.getLength(); i++) {
				Element photoElement = (Element) photoNodes.item(i);
				Photo photo = PhotoUtils.createPhoto(photoElement);
			
				FlickrItem flickrUpdate = new FlickrItem(photo);
				
				flStream.store(flickrUpdate);
				totalRetrievedItems++;
				
				if(totalRetrievedItems>maxResults){
					isFinished = true;
					break;
				}
				
			}
			
			pages = Integer.parseInt(photosElement.getAttribute("pages"));
			
			if(page == pages || pages==0 || isFinished) 
				break;
			
			page++;
		}
		
		logger.info("#Flickr : Handler fetched " + totalRetrievedItems + " photos "+ 
				" [ " + dateToRetrieve + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return totalRetrievedItems;
    }
	
	@Override
	public Integer retrieveListsFeeds(ListFeed feed) {
		return 0;
	}
	
	@Override
	public Integer retrieve (Feed feed) {
		
		switch(feed.getFeedtype()) {
			case SOURCE:
				SourceFeed userFeed = (SourceFeed) feed;
				if(!userFeed.getSource().getNetwork().equals("Flickr"))
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
		if(flickrTransport != null)
			flickrTransport = null;
	}
	
	@Override
	public MediaItem getMediaItem(String id) {
		return null;
	}
	
	@Override
	public StreamUser getStreamUser(String uid) {
		return null;
	}
	
}
