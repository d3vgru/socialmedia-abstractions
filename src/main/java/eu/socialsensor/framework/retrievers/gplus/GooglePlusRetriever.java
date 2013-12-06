package eu.socialsensor.framework.retrievers.gplus;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.PlusRequestInitializer;
import com.google.api.services.plus.model.Activity;
import com.google.api.services.plus.model.ActivityFeed;
import com.google.api.services.plus.model.PeopleFeed;
import com.google.api.services.plus.model.Person;

import eu.socialsensor.framework.abstractions.gplus.GooglePlusItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;

/**
 * The retriever that implements the Google Plus wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class GooglePlusRetriever implements Retriever{
	private Logger logger = Logger.getLogger(GooglePlusRetriever.class);
	
	private static final HttpTransport transport = new NetHttpTransport();
	private static final JsonFactory jsonFactory = new JacksonFactory();
	
	private Plus plusSrv;
	private String userPrefix = "https://plus.google.com/+";
	private String GooglePlusKey;
	
	private int pageLimit = 10;
	private int maxResults;
	private int maxRequests;
	
	public String getKey() { 
		return GooglePlusKey;
	}
	public String getSecret() {
		return null;
	}

	public GooglePlusRetriever(String key,Integer maxResults,Integer maxRequests) {
		GooglePlusKey = key;
		GoogleCredential credential = new GoogleCredential();
		plusSrv = new Plus.Builder(transport, jsonFactory, credential)
						.setApplicationName("SocialSensor")
						.setHttpRequestInitializer(credential)
						.setPlusRequestInitializer(new PlusRequestInitializer(GooglePlusKey)).build();
		
		this.maxResults = maxResults;
		this.maxRequests = maxRequests;
	}

	@Override
	public List<Item> retrieveUserFeeds(SourceFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date lastItemDate = feed.getLastItemDate();
		
		int numberOfRequests = 0;
		
		boolean isFinished = false;
		
		Source source = feed.getSource();
		String uName = source.getName();
		
		if(uName == null){
			logger.info("#GooglePlus : No source feed");
			return null;
		}
				
		//logger.info("#GooglePlus : Retrieving User Feed : "+uName);
		
		//Retrieve userID from Google+
		String userID = null;
		Plus.People.Search searchPeople = null;
		List<Person> people;
		List<Activity> pageOfActivities;
		ActivityFeed activityFeed;
		Plus.Activities.List userActivities;
		
		try {
			searchPeople = plusSrv.people().search(uName);
			searchPeople.setMaxResults(20L);
			PeopleFeed peopleFeed = searchPeople.execute();
			people = peopleFeed.getItems();
			for(Person person : people){
				if(person.getUrl().compareTo(userPrefix+uName) == 0){
					userID = person.getId();
					break;
				}
			}
			
			//Retrieve activity with userID
			userActivities = plusSrv.activities().list(userID,"public");
			userActivities.setMaxResults(20L);
			activityFeed = userActivities.execute();
			pageOfActivities = activityFeed.getItems();
			numberOfRequests ++;
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return items;
		}
		
		while(pageOfActivities != null && !pageOfActivities.isEmpty()){
			
			try {
				
				for (Activity activity : pageOfActivities) {
				
					DateTime publicationTime = activity.getPublished();
					String PublicationTimeStr = publicationTime.toString();
					String newPublicationTimeStr = PublicationTimeStr.replace("T", " ").replace("Z", " ");
					
					Date publicationDate = null;
					try {
						publicationDate = (Date) formatter.parse(newPublicationTimeStr);
						
					} catch (ParseException e) {
						logger.error("#GooglePlus - ParseException: "+e);
						return items;
					}
					
					if(publicationDate.after(lastItemDate) && activity != null && activity.getId() != null){
						GooglePlusItem googlePlusUpdate = new GooglePlusItem(activity,feed);
						
						items.add(googlePlusUpdate);
					}
					
					if(items.size()>maxResults){
						isFinished = true;
						break;
					}
					
				}
				 numberOfRequests++;
				 if(numberOfRequests>maxRequests || (activityFeed.getNextPageToken() == null))
					 break;
				 
				 userActivities.setPageToken(activityFeed.getNextPageToken());
				 activityFeed = userActivities.execute();
				 pageOfActivities = activityFeed.getItems();
				
				
			} catch (IOException e) {
				logger.error("#GooglePlus Exception : "+e);
				return items;
			}
			
			if(isFinished){
				break;
			}
		}

		//logger.info("#GooglePlus : Done retrieving for this session");
//		logger.info("#GooglePlus : Handler fetched " + items.size() + " posts from " + uName + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
//		
		return items;
	}
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date lastItemDate = feed.getLastItemDate();
		
		int totalRequests = 0;
		
		boolean isFinished = false;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#GooglePlus : No keywords feed");
			return items;
		}
		
		String tags = "";
		
		if(keyword!=null){
			for(String key : keyword.getName().split(" "))
				tags+=key.toLowerCase()+" ";
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
		
		//logger.info("#GooglePlus : Retrieving Keywords Feed :"+tags);
		
		Plus.Activities.Search searchActivities;
		ActivityFeed activityFeed;
		List<Activity> pageOfActivities;
		try {
			searchActivities = plusSrv.activities().search(tags);
			searchActivities.setMaxResults(20L);
			searchActivities.setOrderBy("best");
			activityFeed = searchActivities.execute();
			pageOfActivities = activityFeed.getItems();
			totalRequests++;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return items;
		}
		
		while(pageOfActivities != null && !pageOfActivities.isEmpty()){
			
			for (Activity activity : pageOfActivities) {
				
				if(activity.getObject().getAttachments() != null){
					
					DateTime publicationTime = activity.getPublished();
					String PublicationTimeStr = publicationTime.toString();
					String newPublicationTimeStr = PublicationTimeStr.replace("T", " ").replace("Z", " ");
					
					Date publicationDate = null;
					try {
						publicationDate = (Date) formatter.parse(newPublicationTimeStr);
						
					} catch (ParseException e) {
						logger.error("#GooglePlus - ParseException: "+e);
						return items;
					}
					
					if(publicationDate.after(lastItemDate) && activity != null && activity.getId() != null){
						GooglePlusItem googlePlusUpdate = new GooglePlusItem(activity,feed);
						items.add(googlePlusUpdate);
						
					}
					
					if(items.size()>maxResults){
						isFinished = true;
						break;
					}
						
				}
			
			 }
			
			 totalRequests++;
			 if(totalRequests>maxRequests || isFinished || (activityFeed.getNextPageToken() == null))
				 break;
			 
			 searchActivities.setPageToken(activityFeed.getNextPageToken());
			 try {
				activityFeed = searchActivities.execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 pageOfActivities = activityFeed.getItems();
		
		}
		
//		logger.info("#GooglePlus : KeywordsFeed "+feed.getId()+" is done retrieving for this session");
//		logger.info("#GooglePlus : Handler fetched " + items.size() + " posts from " + tags + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
		
	}
	@Override
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
				logger.error("#GooglePlus : Location Feed cannot be retreived from GooglePlus");
				return null;
			
		}
		
		return null;
	}
	
	@Override
	public void stop(){
		if(plusSrv != null){
			plusSrv = null;
		}
	}
	
}
