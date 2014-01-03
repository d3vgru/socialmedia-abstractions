package eu.socialsensor.framework.monitors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.retrievers.socialmedia.SocialMediaRetriever;

/**
 * A feed task that supports a retrieval process for one input feed. 
 * @author manosetro
 * @email manosetro@iti.gr
 */
public class FeedFetchTask implements Runnable {

	private ScheduledFuture<?> taskHandle;
	private SocialMediaRetriever retriever;
	
	public TimeUnit timeUnit = MILLISECONDS;
	private long rate = 30 * 60 * 1000; 			// Request rate: 30 minutes by default  
//	private long updatesRate = 60 * 60 * 1000;		// Updates rate: 1 hour by default  
	
//	private Date requestSince = new Date();			// Current date by default
//	private Date updateSince = new Date();			// Current date by default
	
//	private long lastUpdateTime = 0;
//	private long lastRequestTime = 0;
	private Feed feed;
	private List<Item> retrievedItems = new ArrayList<Item>();
	
	private boolean completed = false;
	private boolean needToPause = false;

	public FeedFetchTask(Feed feed, SocialMediaRetriever retriever) {
		
		this.feed = feed;
		this.retriever = retriever;
	}
	
	@Override
	public void run() {
		System.out.println("Feed Task that handles Feed : "+feed.getId()+" starts retrieving");
		retrievedItems = retriever.retrieve(feed);
		System.out.println("Feed Task that handles Feed : "+feed.getId()+" done retrieving");
		completed = true;
		
	}
	
	public List<Item> getRetrievedItems(){
		return retrievedItems;
	}
	
	public boolean isCompleted(){
		return completed;
	}
	
	public boolean getPaused(){
		return needToPause;
	}

	public long getScheduleRate() {
		return rate;
	}

	public long getRate() {
		return rate;
	}
	public Feed getFeed() {
		return feed;
	}
	public void setRate(long rate) {
		this.rate = rate;
	}
	
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
	public void addHandle(ScheduledFuture<?> taskHandle) {
		this.taskHandle = taskHandle;
	}

	public boolean stop() {
		System.out.println("Feed Task that handles Feed : "+feed.getId()+" is destroyed");
		return taskHandle.cancel(true);
	}

	public String getFeedId() {
		return feed.getId();
	}
	
}
