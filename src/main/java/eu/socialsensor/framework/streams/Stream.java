

package eu.socialsensor.framework.streams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.dysco.Dysco;
import eu.socialsensor.framework.monitors.FeedsMonitor;
import eu.socialsensor.framework.retrievers.Retriever;



/**
 * Class handles the stream of information regarding a social network.
 * It is responsible for its configuration, its wrapper's initialization
 * and its retrieval process.
 * @author manosetro
 * @email  manosetro@iti.gr
 * @author ailiakop
 * @email  ailiakop@iti.gr
 *
 */
public abstract class Stream implements Runnable {

	protected static final String KEY = "Key";
	protected static final String SECRET = "Secret";
	protected static final String ACCESS_TOKEN = "AccessToken";
	protected static final String ACCESS_TOKEN_SECRET = "AccessTokenSecret";
	protected static final String CLIENT_ID = "ClientId";
	protected static final String MAX_RESULTS = "maxResults";
	protected static final String MAX_REQUESTS = "maxRequests";
	
	protected FeedsMonitor monitor;
	protected BlockingQueue<Feed> feedsQueue;
	protected Retriever retriever;
	protected StreamHandler handler;
	
	private Map<String, Set<String>> usersToLists;
	
	/**
	 * Open a stream for updates delivery
	 * @param config
	 *      Stream configuration parameters
	 * @throws StreamException
	 *      In any case of error during stream open
	 */
	public abstract void open(StreamConfiguration config) throws StreamException;
	
	/**
	 * Close a stream 
	 * @throws StreamException
	 *      In any case of error during stream close
	 */
	public abstract void close() throws StreamException;
		
	/**
	 * Search for a certain dysco
	 * @param dysco
	 * @throws StreamException
	 */
	public void search(Dysco dysco) throws StreamException {
		
	}
	
	
	/**
	 * Set the handler that is responsible for the handling 
	 * of the retrieved items
	 * @param handler
	 */
	public void setHandler(StreamHandler handler){
		this.handler = handler;
	}
	
	/**
	 * Sets the feeds monitor for the stream
	 * @return
	 */
	public boolean setMonitor(){
		if(retriever == null)
			return false;
		
		monitor = new FeedsMonitor(retriever);
		return true;
	}
	
	public void setUserLists(Map<String, Set<String>> usersToLists) {
		this.usersToLists = usersToLists;
	}
	
	public void subscribe(List<Feed> feed) throws StreamException{
		
	}
	
	/**
	 * Searches with the wrapper of the stream for a particular
	 * set of feeds (feeds can be keywordsFeeds, userFeeds or locationFeeds)
	 * @param feeds
	 * @return the total number of retrieved items for the stream
	 * @throws StreamException
	 */
	public Integer poll(List<Feed> feeds) throws StreamException {
		List<Item> items = new ArrayList<Item>();
		
		if(retriever != null) {
		
			if(feeds == null)
				return null ;
				
			for(Feed feed : feeds){
				
				List<Item> retrievedItems = retriever.retrieve(feed);
				items.addAll(retrievedItems);
				
			}
			
			if(items != null){
				store(items);
			}	
			
			System.out.println("Retrieved items for "+this.getClass().getName()+ " are : "+items.size());
		}
		
		return items.size();
	
	}
	
	
	
	/**
	 * Store a set of items in the selected databases
	 * @param items
	 */
	public synchronized void store(List<Item>items) {
		for(Item item : items) {
			store(item);
		}
	}
	
	/**
	 * Store an item in the selected databases
	 * @param item
	 */
	public synchronized void store(Item item) {
		if(handler == null) {
			System.out.println("NULL Handler!");
			return;
		}
			
		
		//item.setList(getuserList(item));
		handler.update(item);
	}
	
	private String[] getuserList(Item item) {
		Set<String> lists = new HashSet<String>();
		
		Set<String> userLists = usersToLists.get(item.getUserId());
		if(userLists != null) {
			lists.addAll(userLists);
		}
		
		for(String mention : item.getMentions()) {
			userLists = usersToLists.get(mention);
			if(userLists != null) {
				lists.addAll(userLists);
			}
		}
		if(lists.size() > 0)
			return lists.toArray(new String[lists.size()]);
		else
			return null;
		
	}
	
	/**
	 * Deletes an item from the selected databases
	 * @param item
	 */
	public void delete(Item item){
		handler.delete(item);
	}
	
	/**
	 * Adds a feed to the stream for future searching
	 * @param feed
	 * @return
	 */
	public boolean addFeed(Feed feed) {
		if(feedsQueue == null)
			return false;
		
		return feedsQueue.offer(feed);
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				Feed feed = feedsQueue.take();
				monitor.addFeed(feed);
				monitor.startMonitor(feed);
				
			} catch (InterruptedException e) {
				return;
			}
		}
	}
}
