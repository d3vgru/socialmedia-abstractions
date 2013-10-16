package eu.socialsensor.framework.retrievers.tumblr;


import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.log4j.Logger;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.exceptions.JumblrException;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;

import eu.socialsensor.framework.abstractions.tumblr.TumblrItem;
import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.Keyword;
import eu.socialsensor.framework.common.domain.Source;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;
import eu.socialsensor.framework.retrievers.Retriever;
/**
 * The retriever that implements the Tumblr wrapper
 * @author ailiakop
 * @email  ailiakop@iti.gr
 */
public class TumblrRetriever implements Retriever{
	private Logger logger = Logger.getLogger(TumblrRetriever.class);
	
	private JumblrClient client;
	
	private String consumerKey;
	private String consumerSecret;
	
	private int results_threshold;
	private int requests_threshold;
	
	public String getKey() { 
		return null;
	}
	public String getSecret() {
		return null;
	}

	public TumblrRetriever(String consumerKey, String consumerSecret,Integer maxResults,Integer maxRequests) {
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
		this.results_threshold = maxResults;
		this.requests_threshold = maxRequests;
		
		client = new JumblrClient(consumerKey,consumerSecret);
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
			logger.info("#Tumblr : No source feed");
			return null;
		}
			
		//logger.info("#Tumblr : Retrieving User Feed : "+uName);
		
		Blog blog = client.blogInfo(uName);
		List<Post> posts;
		Map<String,String> options = new HashMap<String,String>();
		
		Integer offset = 0;
		Integer limit = 20;
		options.put("limit", limit.toString());
	
		while(true){
			
			options.put("offset", offset.toString());
			
			posts = blog.posts(options);
			if(posts == null || posts.isEmpty())
				break;
			
			numberOfRequests ++;
			//logger.info("#Tumblr : Retrieving page "+it+" that contains "+posts.size()+" posts");
			
			for(Post post : posts){
				
				if(post.getType().equals("photo") || post.getType().equals("video")){
					
					String retrievedDate = post.getDateGMT().replace(" GMT", "");
					retrievedDate+=".0";
					
					Date publicationDate = null;
					try {
						publicationDate = (Date) formatter.parse(retrievedDate);
						
					} catch (ParseException e) {
						logger.error("#Tumblr - ParseException: "+e);
					}
					
					if(publicationDate.after(lastItemDate)){
						TumblrItem tumblrItem = null;
						try {
							tumblrItem = new TumblrItem(post,feed);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							logger.error("#Tumblr Exception: "+e);
							e.printStackTrace();
							return items;
						}
						
						items.add(tumblrItem);
						
					}
				
				}
				if(items.size()>results_threshold || numberOfRequests>requests_threshold){
					isFinished = true;
					break;
				}
			}
			if(isFinished)
				break;
			
			offset+=limit;
		}

		//logger.info("#Tumblr : Done retrieving for this session");
		logger.info("#Tumblr : Handler fetched " + items.size() + " posts from " + uName + 
				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		
		return items;
	}
	@Override
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed){
		List<Item> items = new ArrayList<Item>();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date currentDate = new Date(System.currentTimeMillis());
		Date indexDate = currentDate;
		Date lastItemDate = feed.getLastItemDate();
		DateUtil dateUtil = new DateUtil();
		
		int numberOfRequests=0;
		
		boolean isFinished = false;
		
		Keyword keyword = feed.getKeyword();
		List<Keyword> keywords = feed.getKeywords();
		
		if(keywords == null && keyword == null){
			logger.info("#Tumblr : No keywords feed");
			return items;
		}
		
		int it=0;
		
		String tags = "";
		if(keyword != null){
			for(String key : keyword.getName().split(" "))
				if(key.length()>1)
					tags+=key.toLowerCase();
		}
		else if(keywords != null){
			for(Keyword key : keywords){
				String [] words = key.getName().split(" ");
				for(String word : words)
					if(!tags.contains(word) && word.length()>1)
						tags += word.toLowerCase();
			}
		}
		
		tags.replaceAll(" ", "");
		
		//logger.info("#Tumblr : Retrieving Keywords Feed : "+tags);
		if(tags.equals(""))
			return items;
		
		while(indexDate.after(lastItemDate) || indexDate.equals(lastItemDate)){
			
			Map<String,String> options = new HashMap<String,String>();
			Long checkTimestamp = indexDate.getTime();
			Integer check = checkTimestamp.intValue();
			options.put("featured_timestamp", check.toString());
			List<Post> posts;
			try{
				posts = client.tagged(tags);
			}catch(JumblrException e){
				return items;
			}
			
			it++;
			
			if(posts == null || posts.isEmpty())
				break;
			
			numberOfRequests ++;
			//logger.info("#Tumblr : Retrieving page "+it+" that contains "+posts.size()+" posts");
			
			for(Post post : posts){
				
				if(post.getType().equals("photo") || post.getType().equals("video")) {
					
					String retrievedDate = post.getDateGMT().replace(" GMT", "");
					retrievedDate+=".0";
					Date publicationDate = null;
					try {
						publicationDate = (Date) formatter.parse(retrievedDate);
						
					} catch (ParseException e) {
						logger.error("#Tumblr - ParseException: "+e);
						return items;
					}
					
					if(publicationDate.after(lastItemDate)){
						TumblrItem tumblrItem = null;
						try {
							tumblrItem = new TumblrItem(post,feed);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							logger.error("#Tumblr Exception: "+e);
							e.printStackTrace();
							return items;
						}
						tumblrItem.setDyscoId(feed.getDyscoId());
						
						items.add(tumblrItem);
						
					}
				
				}
				if(items.size()>results_threshold || numberOfRequests>=requests_threshold){
					isFinished = true;
					break;
				}
			}
			
			if(isFinished)
				break;
			
			indexDate = dateUtil.addDays(indexDate, -1);
				
		}
		
//		logger.info("#Tumblr : Done retrieving for this session");
//		logger.info("#Tumblr : Handler fetched " + items.size() + " posts from " + tags + 
//				" [ " + lastItemDate + " - " + new Date(System.currentTimeMillis()) + " ]");
		feed.setTotalNumberOfItems((long)items.size());
		return items;
		
	}
	@Override
	public List<Item> retrieveLocationFeeds(LocationFeed feed) throws JumblrException {
		logger.info("Retrieving Location Feed");
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
				logger.error("#Tumblr : Location Feed cannot be retreived from Tumblr");
				
				return null;
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
