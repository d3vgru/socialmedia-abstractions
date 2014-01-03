package eu.socialsensor.framework.retrievers.socialmedia;

import java.util.List;

import eu.socialsensor.framework.common.domain.Feed;
import eu.socialsensor.framework.common.domain.Item;
import eu.socialsensor.framework.common.domain.feeds.KeywordsFeed;
import eu.socialsensor.framework.common.domain.feeds.LocationFeed;
import eu.socialsensor.framework.common.domain.feeds.SourceFeed;

/**
 * The interface that represents the retriever for all wrappers
 * @author manosetro
 * @email  manosetro@iti.gr
 */
public interface SocialMediaRetriever{
	
	/**
	 * Retrieve a general feed that is inserted into the system
	 * @param feed
	 * @return
	 */
	public List<Item> retrieve(Feed feed);
	
	/**
	 * Retrieves a keywords feed that contains certain keywords
	 * in order to retrieve relevant multimedia content
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public List<Item> retrieveKeywordsFeeds(KeywordsFeed feed) throws Exception;
	
	/**
	 * Retrieves a user feed that contains the user/users in 
	 * order to retrieve multimedia content posted by them
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public List<Item> retrieveUserFeeds(SourceFeed feed) throws Exception;
	
	/**
	 * Retrieves a location feed that contains the coordinates of the location
	 * that the retrieved multimedia content must come from.
	 * @param feed
	 * @return
	 * @throws Exception
	 */
	public List<Item> retrieveLocationFeeds(LocationFeed feed) throws Exception;

	public void stop();
}
